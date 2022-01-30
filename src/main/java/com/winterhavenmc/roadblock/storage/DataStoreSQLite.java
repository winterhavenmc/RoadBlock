/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.roadblock.storage;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


final class DataStoreSQLite extends DataStoreAbstract implements DataStore, Listener {

	// reference to main class
	private final JavaPlugin plugin;

	// block cache
	private final BlockRecordCache blockCache;

	// chunk cache
	private final Collection<Location> chunkCache;

	// database connection object
	private Connection connection;

	// file path for datastore file
	private final String dataFilePath;

	// schema version
	private int schemaVersion;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	DataStoreSQLite(final JavaPlugin plugin) {

		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set file path for datastore file
		this.dataFilePath = plugin.getDataFolder() + File.separator + type.getStorageName();

		// create empty block cache
		this.blockCache = BlockRecordCache.getInstance();

		// create empty chunk location cache
		this.chunkCache = new HashSet<>();

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

	}


	/**
	 * Initialize SQLite datastore
	 */
	@Override
	public void initialize() throws SQLException, ClassNotFoundException {

		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			plugin.getLogger().info(this + " datastore already initialized.");
			return;
		}

		// register the driver 
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		final String dbUrl = "jdbc:sqlite" + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		// update database schema if necessary
		updateSchema();

		// set initialized true
		setInitialized(true);
		plugin.getLogger().info(this + " datastore initialized.");
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

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0) {
			int count;
			ResultSet rs = statement.executeQuery(Queries.getQuery("SelectBlockTable"));
			if (rs.next()) {
				Collection<BlockRecord> existingRecords = selectAllRecords();
				statement.executeUpdate(Queries.getQuery("DropBlockTable"));
				statement.executeUpdate(Queries.getQuery("DropChunkIndex"));
				statement.executeUpdate(Queries.getQuery("CreateBlockTable"));
				statement.executeUpdate(Queries.getQuery("CreateChunkIndex"));
				count = insertRecords(existingRecords);
				plugin.getLogger().info(count + " block records migrated to schema v1");
			}

			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = 1");

			// update schema version field
			schemaVersion = 1;
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
	public void sync() {
		// no action necessary for this storage type
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public void close() {

		try {
			connection.close();
			plugin.getLogger().info(this + " datastore connection closed.");
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while closing the " + this + " datastore.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}
		setInitialized(false);
	}


	/**
	 * Delete the SQLite datastore file
	 */
	@Override
	public boolean delete() {

		// get reference to dataStore file in file system
		File dataStoreFile = new File(dataFilePath);

		// if file exists, delete file
		boolean result = false;
		if (dataStoreFile.exists()) {
			result = dataStoreFile.delete();
		}

		// return result
		return result;
	}


	/**
	 * Check if a location is a protected road block
	 *
	 * @param location the location key to check for protected status
	 * @return {@code true} if the location is protected, {@code false} if it is not
	 */
	@Override
	public boolean isProtected(final Location location) {

		// get LocationRecord for location
		BlockRecord blockRecord = new BlockRecord(location);

		// check cache first
		if (isChunkCached(location)) {
			if (blockCache.containsKey(blockRecord)) {
				return blockCache.get(blockRecord).equals(CacheStatus.RESIDENT)
						|| blockCache.get(blockRecord).equals(CacheStatus.PENDING_INSERT);
			}
			return false;
		}

		// add chunk to cache
		cacheChunk(location.getChunk());

		// check cache again
		if (blockCache.containsKey(blockRecord)) {
			return blockCache.get(blockRecord).equals(CacheStatus.RESIDENT)
					|| blockCache.get(blockRecord).equals(CacheStatus.PENDING_INSERT);
		}
		return false;
	}


	/**
	 * Insert records into the SQLite datastore
	 *
	 * @param blockRecords Collection of records to insert
	 */
	@Override
	synchronized public int insertRecords(final Collection<BlockRecord> blockRecords) {

		// set cache for all records in list to pending insert
		int count = 0;
		for (BlockRecord blockRecord : blockRecords) {
			blockCache.put(blockRecord, CacheStatus.PENDING_INSERT);
			count++;
		}
		if (plugin.getConfig().getBoolean("debug")) {
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

					for (BlockRecord blockRecord : blockRecords) {

						// if location is null, skip to next location
						if (blockRecord == null) {
							continue;
						}

						// test that world in location is valid, otherwise skip to next location
						if (plugin.getServer().getWorld(blockRecord.getWorldUid()) == null) {
							plugin.getLogger().warning("An error occured while inserting"
									+ " a record in the " + this + " datastore. World invalid!");
							blockCache.remove(blockRecord);
							continue;
						}

						try {
							// synchronize on database connection
							synchronized (this) {

								// create prepared statement
								PreparedStatement preparedStatement =
										connection.prepareStatement(Queries.getQuery("InsertOrIgnoreBlock"));

								preparedStatement.setString(1, blockRecord.getWorldName());
								preparedStatement.setLong(2, blockRecord.getWorldUid().getMostSignificantBits());
								preparedStatement.setLong(3, blockRecord.getWorldUid().getLeastSignificantBits());
								preparedStatement.setInt(4, blockRecord.getBlockX());
								preparedStatement.setInt(5, blockRecord.getBlockY());
								preparedStatement.setInt(6, blockRecord.getBlockZ());
								preparedStatement.setInt(7, blockRecord.getChunkX());
								preparedStatement.setInt(8, blockRecord.getChunkZ());

								// execute prepared statement
								preparedStatement.executeUpdate();
							}
						}
						catch (SQLException e) {

							// output simple error message
							plugin.getLogger().warning("An error occurred while inserting a location "
									+ "into the " + this + " datastore.");
							plugin.getLogger().warning(e.getLocalizedMessage());

							// if debugging is enabled, output stack trace
							if (plugin.getConfig().getBoolean("debug")) {
								e.printStackTrace();
							}
							continue;
						}
						count++;
						blockCache.put(blockRecord, CacheStatus.RESIDENT);
					}
					connection.commit();
					connection.setAutoCommit(true);
				}
				catch (SQLException e) {
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "insert a block in the " + this + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.getConfig().getBoolean("debug")) {
						e.printStackTrace();
					}
				}

				long elapsedTime = (System.nanoTime() - startTime);
				if (plugin.getConfig().getBoolean("profile")) {
					if (count > 0) {
						plugin.getLogger().info(count + " blocks inserted into " + this + " datastore in "
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
	 * @param blockRecords Collection of locations
	 */
	@Override
	synchronized public int deleteRecords(final Collection<BlockRecord> blockRecords) {

		// set cache for all records in list to pending delete
		int count = 0;
		for (BlockRecord blockRecord : blockRecords) {
			blockCache.put(blockRecord, CacheStatus.PENDING_DELETE);
			count++;
		}
		if (plugin.getConfig().getBoolean("debug")) {
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

					for (final BlockRecord blockRecord : blockRecords) {

						// if key is null return, skip to next location
						if (blockRecord == null) {
							continue;
						}

						try {
							// synchronize on database connection
							synchronized (this) {

								// create prepared statement
								PreparedStatement preparedStatement =
										connection.prepareStatement(Queries.getQuery("DeleteBlock"));

								preparedStatement.setLong(1, blockRecord.getWorldUid().getMostSignificantBits());
								preparedStatement.setLong(2, blockRecord.getWorldUid().getLeastSignificantBits());
								preparedStatement.setInt(3, blockRecord.getBlockX());
								preparedStatement.setInt(4, blockRecord.getBlockY());
								preparedStatement.setInt(5, blockRecord.getBlockZ());

								// execute prepared statement
								rowsAffected = preparedStatement.executeUpdate();
							}
						}
						catch (SQLException e) {

							// output simple error message
							plugin.getLogger().warning("An error occurred while attempting to "
									+ "delete a block from the " + this + " datastore.");
							plugin.getLogger().warning(e.getLocalizedMessage());

							// if debugging is enabled, output stack trace
							if (plugin.getConfig().getBoolean("debug")) {
								e.printStackTrace();
							}
						}
						blockCache.remove(blockRecord);
						count = count + rowsAffected;
					}
					connection.commit();
					connection.setAutoCommit(true);
				}
				catch (SQLException e) {
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "delete a block from the " + this + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.getConfig().getBoolean("debug")) {
						e.printStackTrace();
					}
				}

				long elapsedTime = (System.nanoTime() - startTime);
				if (plugin.getConfig().getBoolean("profile")) {
					if (count > 0) {
						plugin.getLogger().info(count + " blocks removed from " + this + " datastore in "
								+ TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds.");
					}
				}
			}
		}.runTaskAsynchronously(plugin);
		return count;
	}


	/**
	 * Retrieve all road block location records from SQLite datastore
	 *
	 * @return List of location records
	 */
	synchronized public Collection<BlockRecord> selectAllRecords() {

		final Collection<BlockRecord> returnSet = new HashSet<>();

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
				final int blockX = rs.getInt("x");
				final int blockY = rs.getInt("y");
				final int blockZ = rs.getInt("z");
				final int chunkX = rs.getInt("chunk_x");
				final int chunkZ = rs.getInt("chunk_z");

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

				// create block record object from retrieved record
				BlockRecord blockRecord = new BlockRecord(world.getName(), world.getUID(),
						blockX, blockY, blockZ, chunkX, chunkZ);

				// add block record to return set
				returnSet.add(blockRecord);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch all records from the " + this + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// return results in an unmodifiable set
		return returnSet;
	}


	/**
	 * Retrieve all road block locations in chunk from the SQLite datastore
	 *
	 * @param chunk the chunk for which to retrieve all road block locations from the datastore
	 * @return Collection of locations
	 */
	@Override
	synchronized public Collection<BlockRecord> selectRecordsInChunk(final Chunk chunk) {

		// create new set for results
		final Collection<BlockRecord> returnSet = new HashSet<>();

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
				int blockX = rs.getInt("x");
				int blockY = rs.getInt("y");
				int blockZ = rs.getInt("z");
				int chunkX = rs.getInt("chunk_x");
				int chunkZ = rs.getInt("chunk_z");

				// reconstitute world uid from components
				UUID worldUid = new UUID(worldUidMSB,worldUidLSB);

				// get world by uid
				World world = plugin.getServer().getWorld(worldUid);

				// if world is null, skip to next record
				if (world == null) {
					plugin.getLogger().warning("Stored location has invalid world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				// get current world name
				worldName = world.getName();

				// create block record from stored location
				BlockRecord record = new BlockRecord(worldName, worldUid, blockX, blockY, blockZ, chunkX, chunkZ);
				returnSet.add(record);
				count++;
			}

			if (plugin.getConfig().getBoolean("profile")) {
				plugin.getLogger().info("Fetched " + count + " blocks in chunk in "
						+ TimeUnit.NANOSECONDS.toMicros(elapsedTime) + " microseconds.");
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch records from the " + this + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// return result set
		return returnSet;
	}


	@Override
	public Collection<Location> selectNearbyBlocks(final Location location, final int distance) {

		// if passed location is null, return empty set
		if (location == null) {
			return Collections.emptySet();
		}

		// get world for location
		World world = location.getWorld();

		// if world is null, return empty set
		if (world == null) {
			return Collections.emptySet();
		}

		final int minX = location.getBlockX() - distance;
		final int maxX = location.getBlockX() + distance;
		final int minZ = location.getBlockZ() - distance;
		final int maxZ = location.getBlockZ() + distance;

		Collection<Location> resultSet = new HashSet<>();

		try {
			PreparedStatement preparedStatement =
					connection.prepareStatement(Queries.getQuery("SelectNearbyBlocks"));

			preparedStatement.setLong(1, world.getUID().getMostSignificantBits());
			preparedStatement.setLong(2, world.getUID().getLeastSignificantBits());
			preparedStatement.setInt(3, minX);
			preparedStatement.setInt(4, maxX);
			preparedStatement.setInt(5, minZ);
			preparedStatement.setInt(6, maxZ);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				final double x = rs.getDouble("x");
				final double y = rs.getDouble("y");
				final double z = rs.getDouble("z");

				// get location for stored record
				Location newLocation = new Location(world, x, y, z);

				// add location to result set
				resultSet.add(newLocation);
			}

		}
		catch (final SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "select nearby block records from the " + this + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		return resultSet;
	}


	/**
	 * Add all road block locations within chunk to cache
	 *
	 * @param chunk the chunk for which to load all road block locations into cache
	 */
	private void cacheChunk(final Chunk chunk) {

		final Collection<BlockRecord> blockSet = selectRecordsInChunk(chunk);

		int count = 0;

		for (BlockRecord blockRecord : blockSet) {
			blockCache.put(blockRecord, CacheStatus.RESIDENT);
			count++;
		}

		chunkCache.add(chunk.getBlock(0, 0, 0).getLocation());

		if (plugin.getConfig().getBoolean("debug")) {
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
		for (BlockRecord blockRecord : blockCache.keySet()) {
			if (blockRecord.getWorldUid().equals(chunk.getWorld().getUID())
					&& blockRecord.getChunkX() == chunk.getX()
					&& blockRecord.getChunkZ() == chunk.getZ()) {
				blockCache.remove(blockRecord);
				count++;
			}
		}

		chunkCache.remove(chunk.getBlock(0, 0, 0).getLocation());

		long elapsedTime = (System.nanoTime() - startTime);
		if (plugin.getConfig().getBoolean("profile")) {
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
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().info("Chunk is cached.");
			}
			return true;
		}
		return false;
	}


	@Override
	synchronized public int getTotalBlocks() {

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
		catch (final SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "count all records from the " + this + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// return result
		return total;
	}


	/**
	 * Event listener for chunk unload event
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onChunkUnload(final ChunkUnloadEvent event) {
		flushCache(event.getChunk());
	}

}
