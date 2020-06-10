package com.winterhaven_mc.roadblock.storage;

import com.winterhaven_mc.roadblock.PluginMain;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@SuppressWarnings({"SynchronizeOnNonFinalField", "ConstantConditions"})
final class DataStoreSQLite extends DataStore implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;

	// block cache
	private final Map<LocationRecord, CacheStatus> blockCache;

	// chunk cache
	private final Set<Location> chunkCache;

	// schema version
	private int schemaVersion;

	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	DataStoreSQLite(PluginMain plugin) {

		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set datastore filename
		this.filename = "roadblocks.db";

		// create empty block cache
		this.blockCache = new ConcurrentHashMap<>();

		// create empty chunk location cache
		this.chunkCache = new HashSet<>();

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

	}


	/**
	 * Initialize SQLite datastore
	 */
	@Override
	final void initialize() throws SQLException, ClassNotFoundException {

		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			plugin.getLogger().info(this.getDisplayName() + " datastore already initialized.");
			return;
		}

		// register the driver 
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String destinationsDb = plugin.getDataFolder() + File.separator + filename;
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + destinationsDb;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		// update database schema if necessary
		updateSchema();

		// set initialized true
		setInitialized(true);
		plugin.getLogger().info(getDisplayName() + " datastore initialized.");
	}


	private int getSchemaVersion() {

		int version = -1;

		try {
			final Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			while (rs.next()) {
				version = rs.getInt(1);
			}
		}
		catch (SQLException e) {
			plugin.getLogger().warning("Could not get schema version!");
		}
		return version;
	}


	private void updateSchema() throws SQLException {

		schemaVersion = getSchemaVersion();
		plugin.getLogger().info("SQLite schema v" + schemaVersion);

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0) {
			Set<LocationRecord> records = selectAllRecords();
			statement.executeUpdate("DROP TABLE IF EXISTS blocks");
			statement.executeUpdate("DROP INDEX IF EXISTS chunks");
			statement.executeUpdate(Queries.getQuery("CreateBlockTable"));
			statement.executeUpdate(Queries.getQuery("CreateChunkIndex"));
			int count = insertRecords(records);
			plugin.getLogger().info(count + " block records migrated to schema v1");
			statement.executeUpdate("PRAGMA user_version = 1");
			schemaVersion = 1;
			return;
		}

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("CreateBlockTable"));

		// execute index creation statement
		statement.executeUpdate(Queries.getQuery("CreateChunkIndex"));
	}


	/**
	 * Sync in memory datastore to disk<br>
	 * (unused for SQLite datastore)
	 */
	@Override
	final void sync() {

		// no action necessary for this storage type

	}


	/**
	 * Delete the SQLite datastore file
	 */
	@Override
	final boolean delete() {

		// get reference to dataStore file in file system
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());

		// if file exists, delete file
		boolean result = false;
		if (dataStoreFile.exists()) {
			result = dataStoreFile.delete();
		}

		// return result
		return result;
	}


	/**
	 * Check that SQLite datastore file exists on disk
	 */
	@Override
	final boolean exists() {

		// get path name to data store file
		final File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public final void close() {

		try {
			connection.close();
			plugin.getLogger().info(getDisplayName() + " datastore connection closed.");
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occured while closing the " + getDisplayName() + " datastore.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}
		setInitialized(false);
	}


	/**
	 * Check if a location is a protected road block
	 *
	 * @param location the location key to check for protected status
	 * @return {@code true} if the location is protected, {@code false} if it is not
	 */
	@Override
	final boolean isProtected(final Location location) {

		LocationRecord locationRecord = new LocationRecord(location);

		// check cache first
		if (isChunkCached(location)) {
			if (blockCache.containsKey(locationRecord)) {
				return blockCache.get(locationRecord).equals(CacheStatus.TRUE)
						|| blockCache.get(locationRecord).equals(CacheStatus.PENDING_INSERT);
			}
			return false;
		}

		// add chunk to cache
		cacheChunk(location.getChunk());

		// check cache again
		if (blockCache.containsKey(locationRecord)) {
			return blockCache.get(locationRecord).equals(CacheStatus.TRUE)
					|| blockCache.get(locationRecord).equals(CacheStatus.PENDING_INSERT);
		}
		return false;
	}


	/**
	 * Insert records into the SQLite datastore
	 *
	 * @param locationRecords HashSet of records to insert
	 */
	@Override
	synchronized final int insertRecords(final Collection<LocationRecord> locationRecords) {

		// set cache for all records in list to pending insert
		int count = 0;
		for (LocationRecord locationRecord : locationRecords) {
			blockCache.put(locationRecord, CacheStatus.PENDING_INSERT);
			count++;
		}
		if (plugin.debug) {
			plugin.getLogger().info(count + " blocks marked PENDING_INSERT in cache.");
		}

		// asynchronously insert all locations in hash set
		new BukkitRunnable() {
			@Override
			public void run() {

				int count = 0;
				long startTime = System.nanoTime();

				try {

					// set connection to transaction mode
					connection.setAutoCommit(false);

					for (LocationRecord locationRecord : locationRecords) {

						// if location is null, skip to next location
						if (locationRecord == null) {
							continue;
						}

						// test that world in location is valid, otherwise skip to next location
						if (plugin.getServer().getWorld(locationRecord.getWorldUid()) == null) {
							plugin.getLogger().warning("An error occured while inserting"
									+ " a record in the " + getDisplayName() + " datastore. World invalid!");
							blockCache.remove(locationRecord);
							continue;
						}

						try {
							// synchronize on database connection
							synchronized (connection) {

								// create prepared statement
								PreparedStatement preparedStatement =
										connection.prepareStatement(Queries.getQuery("InsertOrIgnoreBlock"));

								preparedStatement.setString(1, locationRecord.getWorldName());
								preparedStatement.setLong(2, locationRecord.getWorldUid().getMostSignificantBits());
								preparedStatement.setLong(3, locationRecord.getWorldUid().getLeastSignificantBits());
								preparedStatement.setDouble(4, locationRecord.getBlockX());
								preparedStatement.setDouble(5, locationRecord.getBlockY());
								preparedStatement.setDouble(6, locationRecord.getBlockZ());
								preparedStatement.setFloat(7, locationRecord.getChunkX());
								preparedStatement.setFloat(8, locationRecord.getChunkZ());

								// execute prepared statement
								preparedStatement.executeUpdate();
							}
						}
						catch (Exception e) {

							// output simple error message
							plugin.getLogger().warning("An error occurred while inserting a location "
									+ "into the " + getDisplayName() + " datastore.");
							plugin.getLogger().warning(e.getLocalizedMessage());

							// if debugging is enabled, output stack trace
							if (plugin.debug) {
								e.printStackTrace();
							}
							continue;
						}
						count++;
						blockCache.put(locationRecord, CacheStatus.TRUE);
					}
					connection.commit();
					connection.setAutoCommit(true);
				}
				catch (SQLException e) {
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "insert a block in the " + getDisplayName() + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}

				long elapsedTime = (System.nanoTime() - startTime);
				if (plugin.profile) {
					if (count > 0) {
						plugin.getLogger().info(count + " blocks inserted into " + getDisplayName() + " datastore in "
								+ TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds.");
					}
				}
			}
		}.runTaskAsynchronously(plugin);
		return count;
	}


	/**
	 * Delete a list of locations from the SQLite datastore
	 *
	 * @param locationRecords HashSet of locations
	 */
	@Override
	synchronized final int deleteRecords(final Collection<LocationRecord> locationRecords) {

		// set cache for all records in list to pending delete
		int count = 0;
		for (LocationRecord locationRecord : locationRecords) {
			blockCache.put(locationRecord, CacheStatus.PENDING_DELETE);
			count++;
		}
		if (plugin.debug) {
			plugin.getLogger().info(count + " blocks marked PENDING_DELETE in cache.");
		}

		// asynchronously delete all locations
		new BukkitRunnable() {
			@Override
			public void run() {

				int count = 0;
				long startTime = System.nanoTime();

				try {
					connection.setAutoCommit(false);

					int rowsAffected = 0;

					for (final LocationRecord locationRecord : locationRecords) {

						// if key is null return, skip to next location
						if (locationRecord == null) {
							continue;
						}

						try {
							// synchronize on database connection
							synchronized (connection) {

								// create prepared statement
								PreparedStatement preparedStatement =
										connection.prepareStatement(Queries.getQuery("DeleteBlock"));

								preparedStatement.setLong(1, locationRecord.getWorldUid().getMostSignificantBits());
								preparedStatement.setLong(2, locationRecord.getWorldUid().getLeastSignificantBits());
								preparedStatement.setInt(3, locationRecord.getBlockX());
								preparedStatement.setInt(4, locationRecord.getBlockY());
								preparedStatement.setInt(5, locationRecord.getBlockZ());

								// execute prepared statement
								rowsAffected = preparedStatement.executeUpdate();
							}
						}
						catch (Exception e) {

							// output simple error message
							plugin.getLogger().warning("An error occurred while attempting to "
									+ "delete a block from the " + getDisplayName() + " datastore.");
							plugin.getLogger().warning(e.getLocalizedMessage());

							// if debugging is enabled, output stack trace
							if (plugin.debug) {
								e.getStackTrace();
							}
						}
						blockCache.remove(locationRecord);
						count = count + rowsAffected;
					}
					connection.commit();
					connection.setAutoCommit(true);
				}
				catch (SQLException e) {
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "delete a block from the " + getDisplayName() + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}

				long elapsedTime = (System.nanoTime() - startTime);
				if (plugin.profile) {
					if (count > 0) {
						plugin.getLogger().info(count + " blocks removed from " + getDisplayName() + " datastore in "
								+ TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds.");
					}
				}
			}
		}.runTaskAsynchronously(plugin);
		return count;
	}


	/**
	 * Retrieve all road block locations in chunk from the SQLite datastore
	 *
	 * @param chunk the chunk for which to retrieve all road block locations from the datastore
	 * @return Set of locations
	 */
	@Override
	synchronized final Set<Location> selectBlockLocationsInChunk(final Chunk chunk) {

		// create new set for results
		final Set<Location> returnSet = new HashSet<>();

		try {
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectBlocksInChunk"));

			long worldUidMsb = chunk.getWorld().getUID().getMostSignificantBits();
			long worldUidLsb = chunk.getWorld().getUID().getLeastSignificantBits();

			preparedStatement.setLong(1, worldUidMsb);
			preparedStatement.setLong(2, worldUidLsb);
			preparedStatement.setInt(3, chunk.getX());
			preparedStatement.setInt(4, chunk.getZ());

			// execute sql query
			long startTime = System.nanoTime();
			ResultSet rs = preparedStatement.executeQuery();

			long elapsedTime = System.nanoTime() - startTime;

			int count = 0;

			while (rs.next()) {

				final long worldUidMSB = rs.getLong("worlduidmsb");
				final long worldUidLSB = rs.getLong("worlduidlsb");
				String worldName = rs.getString("worldname");
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");

				UUID worldUid = new UUID(worldUidMSB,worldUidLSB);
				World world;

				try {
					world = plugin.getServer().getWorld(worldUid);
				}
				catch (Exception e) {
					plugin.getLogger().warning("Stored destination has unloaded world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				Location location = new Location(world, x, y, z);
				returnSet.add(location);
				count++;
			}

			if (plugin.profile) {
				plugin.getLogger().info("Fetched " + count + " blocks in chunk in "
						+ TimeUnit.NANOSECONDS.toMicros(elapsedTime) + " microseconds.");
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch records from the " + getDisplayName() + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.printStackTrace();
			}
		}

		// return results in an unmodifiable set
		return Collections.unmodifiableSet(returnSet);
	}


	/**
	 * Retrieve all road block location records from SQLite datastore
	 *
	 * @return List of location records
	 */
	synchronized final Set<LocationRecord> selectAllRecords() {

		final Set<LocationRecord> returnSet = new HashSet<>();

		try {
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectAllBlocks"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				final World world;
				final long worldUidMsb;
				final long worldUidLsb;

				final String worldName = rs.getString("worldname");
				final double x = rs.getDouble("x");
				final double y = rs.getDouble("y");
				final double z = rs.getDouble("z");

				// if schema version 0, get world object from stored world name
				if (schemaVersion == 0) {
					world = plugin.getServer().getWorld(worldName);
				}
				// else get world object from stored world uuid
				else {
					worldUidMsb = rs.getLong("worlduidmsb");
					worldUidLsb = rs.getLong("worlduidlsb");
					UUID worldUid = new UUID(worldUidMsb, worldUidLsb);
					world = plugin.getServer().getWorld(worldUid);
				}

				// if world is null, skip adding record to return set
				if (world == null) {
					plugin.getLogger().warning("Stored block has unloaded world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				Location location = new Location(world, x, y, z);
				LocationRecord locationRecord = new LocationRecord(location);
				returnSet.add(locationRecord);
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch all records from the " + getDisplayName() + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		// return results in an unmodifiable set
		return Collections.unmodifiableSet(returnSet);
	}


	/**
	 * Add all road block locations within chunk to cache
	 *
	 * @param chunk the chunk for which to load all road block locations into cache
	 */
	private void cacheChunk(final Chunk chunk) {

		final Set<Location> blockSet = selectBlockLocationsInChunk(chunk);

		int count = 0;

		for (Location location : blockSet) {
			blockCache.put(new LocationRecord(location), CacheStatus.TRUE);
			count++;
		}

		chunkCache.add(chunk.getBlock(0, 0, 0).getLocation());

		if (plugin.debug) {
			if (count > 0) {
				plugin.getLogger().info(count + " blocks added to cache.");
			}
		}
	}


	/**
	 * Remove all road block locations within chunk from cache<br>
	 * called on chunk unload event
	 *
	 * @param chunk the chunk for which to remove all road block locations from cache
	 */
	private void flushCache(final Chunk chunk) {

		int count = 0;
		long startTime = System.nanoTime();
		for (LocationRecord locationRecord : blockCache.keySet()) {
			if (locationRecord.getLocation().getChunk().equals(chunk)) {
				blockCache.remove(locationRecord);
				count++;
			}
		}
		chunkCache.remove(chunk.getBlock(0, 0, 0).getLocation());

		long elapsedTime = (System.nanoTime() - startTime);
		if (plugin.profile) {
			if (count > 0) {
				plugin.getLogger().info(count + " blocks removed from cache in "
						+ TimeUnit.NANOSECONDS.toMicros(elapsedTime) + " microseconds.");
			}
		}
	}


	/**
	 * Check if road block locations for a chunk are loaded in the cache
	 *
	 * @param location the location to test to determine if all chunk road blocks are cached
	 * @return {@code true} if chunk is cached, {@code false} if not
	 */
	private boolean isChunkCached(final Location location) {

		final Location chunkLoc = location.getChunk().getBlock(0, 0, 0).getLocation();

		if (chunkCache.contains(chunkLoc)) {
			if (plugin.debug) {
				plugin.getLogger().info("Chunk is cached.");
			}
			return true;
		}
		return false;
	}


	@Override
	synchronized int getTotalBlocks() {

		int total = 0;

		try {
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("CountAllBlocks"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				total = rs.getInt("rowcount");
			}

		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "count all records from the " + getDisplayName() + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		// return result
		return total;
	}

	@Override
	Set<Location> selectNearbyBlocks(final Location location, final int distance) {

		if (location == null) {
			return Collections.emptySet();
		}

		final int minX = location.getBlockX() - distance;
		final int maxX = location.getBlockX() + distance;
		final int minZ = location.getBlockZ() - distance;
		final int maxZ = location.getBlockZ() + distance;

		Set<Location> resultSet = new HashSet<>();

		try {
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectNearbyBlocks"));

			preparedStatement.setLong(1, location.getWorld().getUID().getMostSignificantBits());
			preparedStatement.setLong(2, location.getWorld().getUID().getLeastSignificantBits());
			preparedStatement.setInt(3, minX);
			preparedStatement.setInt(4, maxX);
			preparedStatement.setInt(5, minZ);
			preparedStatement.setInt(6, maxZ);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				final long worldUidMsb = rs.getLong("worlduidmsb");
				final long worldUidLsb = rs.getLong("worlduidlsb");
				final double x = rs.getDouble("x");
				final double y = rs.getDouble("y");
				final double z = rs.getDouble("z");

				UUID worldUid = new UUID(worldUidMsb,worldUidLsb);
				World world = plugin.getServer().getWorld(worldUid);

				Location newLocation = new Location(world, x, y, z);
				resultSet.add(newLocation);
			}

		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select nearby block records from the " + getDisplayName() + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		return resultSet;
	}

	/**
	 * Event listener for chunk unload event
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	final void onChunkUnload(final ChunkUnloadEvent event) {
		flushCache(event.getChunk());
	}

}
