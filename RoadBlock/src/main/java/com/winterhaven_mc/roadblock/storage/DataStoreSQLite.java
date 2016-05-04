package com.winterhaven_mc.roadblock.storage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.winterhaven_mc.roadblock.PluginMain;


public class DataStoreSQLite extends DataStore implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;

	// block cache
	private Map<Location,CacheStatus> blockCache;
	
	// chunk cache
	private Set<Location> chunkCache;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	DataStoreSQLite (PluginMain plugin) {

		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set datastore filename
		this.filename = "roadblocks.db";
		
		// create empty block cache
		this.blockCache = new ConcurrentHashMap<Location,CacheStatus>();
		
		// crate empty chunk location cache
		this.chunkCache = new HashSet<Location>();

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

	}


	/**
	 * Initialize SQLite datastore
	 */
	@Override
	void initialize() throws SQLException, ClassNotFoundException {

		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			plugin.getLogger().info(this.getName() + " datastore already initialized.");
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
		Statement statement = connection.createStatement();

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("CreateBlockTable"));
		
		// execute index creation statement
		statement.executeUpdate(Queries.getQuery("CreateChunkIndex"));

		// set initialized true
		setInitialized(true);
		if (plugin.debug) {
			plugin.getLogger().info(this.getName() + " datastore initialized.");
		}
	}

	
	/**
	 * Sync in memory datastore to disk<br>
	 * (unused for SQLite datastore)
	 */
	@Override
	void sync() {
	
		// no action necessary for this storage type
	
	}


	/**
	 * Delete the SQLite datastore file
	 */
	@Override
	void delete() {
	
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			dataStoreFile.delete();
		}
	}


	/**
	 * Check that SQLite datastore file exists on disk
	 */
	@Override
	boolean exists() {
	
		// get path name to data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public void close() {
	
		try {
			connection.close();
			plugin.getLogger().info("SQLite datastore connection closed.");
		}
		catch (Exception e) {
	
			// output simple error message
			plugin.getLogger().warning("An error occured while closing the SQLite datastore.");
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
	 * @param location
	 * @return boolean
	 */
	@Override
	boolean isProtected(final Location location) {
		
		// check cache first
		if (isChunkCached(location)) {
			if (blockCache.containsKey(location)) {
				if (blockCache.get(location).equals(CacheStatus.TRUE)
						|| blockCache.get(location).equals(CacheStatus.PENDING_INSERT)) {
					return true;
				}
			}
			return false;
		}
		
		// add chunk to cache
		addCache(location.getChunk());
		
		// check cache again
		if (blockCache.containsKey(location)) {
			if (blockCache.get(location).equals(CacheStatus.TRUE)
					|| blockCache.get(location).equals(CacheStatus.PENDING_INSERT)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Insert multiple records into the SQLite datastore
	 * @param locations HashSet of records to insert
	 */
	@Override
	void insertRecords(final Collection<Location> locations) {
		
		// set cache for all records in list to pending insert
		int count = 0;
		for (Location location : locations) {
			blockCache.put(location, CacheStatus.PENDING_INSERT);
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
				Long startTime = System.nanoTime();

				try {
					
					// set connection to transaction mode
					connection.setAutoCommit(false);

					for (Location location : locations) {
						
						// if location is null, skip to next location
						if (location == null) {
							continue;
						}
						
						String testWorldName = null;

						// test that world in location is valid, otherwise skip to next location
						try {
							testWorldName = location.getWorld().getName();
						} catch (Exception e) {
							plugin.getLogger().warning("An error occured while inserting"
									+ " a record in the " + getName() + " datastore. World invalid!");
							blockCache.remove(location);
							continue;
						}
						final String worldName = testWorldName;

						try {
							// synchronize on database connection
							synchronized(connection) {

								// create prepared statement
								PreparedStatement preparedStatement = 
										connection.prepareStatement(Queries.getQuery("InsertOrIgnoreBlock"));

								preparedStatement.setString(1, worldName);
								preparedStatement.setDouble(2, location.getX());
								preparedStatement.setDouble(3, location.getY());
								preparedStatement.setDouble(4, location.getZ());
								preparedStatement.setFloat(5, location.getChunk().getX());
								preparedStatement.setFloat(6, location.getChunk().getZ());

								// execute prepared statement
								preparedStatement.executeUpdate();
							}
						}
						catch (Exception e) {

							// output simple error message
							plugin.getLogger().warning("An error occured while inserting a location "
									+ "into the " + getName() + " datastore.");
							plugin.getLogger().warning(e.getLocalizedMessage());

							// if debugging is enabled, output stack trace
							if (plugin.debug) {
								e.getStackTrace();
							}
							continue;
						}
						count++;
						blockCache.put(location, CacheStatus.TRUE);
					}
					connection.commit();
					connection.setAutoCommit(true);
				}
				catch (SQLException e) {
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "insert a block in the " + getName() + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());
			
					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}
				
				Long elapsedTime = (System.nanoTime() - startTime);
				if (plugin.profile) {
					if (count > 0) {
						plugin.getLogger().info(count + " blocks inserted into " + getName() + " datastore in " 
						+ TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds.");
					}
				}
			}
		}.runTaskAsynchronously(plugin);
		
	}


	/**
	 * Insert a single record into the SQLite datastore
	 * @param location
	 */
	@Override
	void insertRecord(final Location location) {
		
		// if location is null do nothing and return
		if (location == null) {
			return;
		}
		
		// put location in cache with pending insert status
		blockCache.put(location,CacheStatus.PENDING_INSERT);

		String testWorldName = null;

		// test that world in location is valid
		try {
			testWorldName = location.getWorld().getName();
		} catch (Exception e) {
			plugin.getLogger().warning("An error occured while inserting"
					+ " a record in the " + this.getName() + " datastore. World invalid!");
			return;
		}
		final String worldName = testWorldName;

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// synchronize on database connection
					synchronized(connection) {

						// create prepared statement
						PreparedStatement preparedStatement = 
								connection.prepareStatement(Queries.getQuery("InsertOrReplaceBlock"));

						preparedStatement.setString(1, worldName);
						preparedStatement.setDouble(2, location.getX());
						preparedStatement.setDouble(3, location.getY());
						preparedStatement.setDouble(4, location.getZ());
						preparedStatement.setFloat(5, location.getChunk().getX());
						preparedStatement.setFloat(6, location.getChunk().getZ());

						// execute prepared statement
						preparedStatement.executeUpdate();
					}
				}
				catch (Exception e) {

					// output simple error message
					plugin.getLogger().warning("An error occured while inserting a location "
							+ "into the SQLite datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}
				blockCache.put(location, CacheStatus.TRUE);
			}
		}.runTaskAsynchronously(plugin);
	}


	/**
	 * Delete a list of locations from the SQLite datastore
	 * @param locations HashSet of locations
	 */
	@Override
	void deleteRecords(final Collection<Location> locations) {
		
		// set cache for all records in list to pending delete
		int count = 0;
		for (Location location : locations) {
			blockCache.put(location, CacheStatus.PENDING_DELETE);
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
				Long startTime = System.nanoTime();
				
				try {
					connection.setAutoCommit(false);

					int rowsAffected = 0;
					
					for (Location location : locations) {
						
						// if key is null return, skip to next location
						if (location == null) {
							continue;
						}

						final String worldName = location.getWorld().getName();
						final int x = location.getBlockX();
						final int y = location.getBlockY();
						final int z = location.getBlockZ();
					
						try {
							// synchronize on database connection
							synchronized(connection) {

								// create prepared statement
								PreparedStatement preparedStatement = 
										connection.prepareStatement(Queries.getQuery("DeleteBlock"));

								preparedStatement.setString(1, worldName);
								preparedStatement.setInt(2, x);
								preparedStatement.setInt(3, y);
								preparedStatement.setInt(4, z);

								// execute prepared statement
								rowsAffected = preparedStatement.executeUpdate();
							}
						}
						catch (Exception e) {
					
							// output simple error message
							plugin.getLogger().warning("An error occurred while attempting to "
									+ "delete a block from the " + getName() + " datastore.");
							plugin.getLogger().warning(e.getLocalizedMessage());
					
							// if debugging is enabled, output stack trace
							if (plugin.debug) {
								e.getStackTrace();
							}
						}
						blockCache.remove(location);
						count = count + rowsAffected;
					}				
					connection.commit();
					connection.setAutoCommit(true);
				}
				catch (SQLException e) {
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "delete a block from the " + getName() + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());
			
					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}
				
				Long elapsedTime = (System.nanoTime() - startTime);
				if (plugin.profile) {
					if (count > 0) {
						plugin.getLogger().info(count + " blocks removed from " + getName() + " datastore in " 
						+ TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds.");
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	/**
	 * Delete a single location record from the SQLite datastore
	 * @param location
	 */
	@Override
	void deleteRecord(final Location location) {
	
		// if key is null return
		if (location == null) {
			return;
		}
		
		final String worldName = location.getWorld().getName();
		final int x = location.getBlockX();
		final int y = location.getBlockY();
		final int z = location.getBlockZ();
	
		// set block to pending delete in cache
		blockCache.put(location,CacheStatus.PENDING_DELETE);
	
		new BukkitRunnable() {
			@Override
			public void run() {
	
				try {

					// synchronize on database connection
					synchronized(connection) {

						// create prepared statement
						PreparedStatement preparedStatement = 
								connection.prepareStatement(Queries.getQuery("DeleteBlock"));

						preparedStatement.setString(1, worldName);
						preparedStatement.setInt(2, x);
						preparedStatement.setInt(3, y);
						preparedStatement.setInt(4, z);

						// execute prepared statement
						int rowsAffected = preparedStatement.executeUpdate();
						
						// output debugging information
						if (plugin.debug) {
							plugin.getLogger().info(rowsAffected + " rows deleted.");
						}
					}
				}
				catch (Exception e) {
	
					// output simple error message
					plugin.getLogger().warning("An error occurred while attempting to "
							+ "delete a block from the " + getName() + " datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());
	
					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}
				blockCache.remove(location);
			}
		}.runTaskAsynchronously(plugin);
	}


	/**
	 * Retrieve all road block locations in chunk from the SQLite datastore
	 * @param chunk
	 * @return Set of locations
	 */
	@Override
	Set<Location> selectBlockLocationsInChunk(final Chunk chunk) {

		// create new set for results
		Set<Location> returnSet = new HashSet<Location>();

		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("SelectBlocksInChunk"));

			preparedStatement.setString(1, chunk.getWorld().getName());
			preparedStatement.setInt(2, chunk.getX());
			preparedStatement.setInt(3, chunk.getZ());

			// execute sql query
			Long startTime = System.nanoTime();
			ResultSet rs = preparedStatement.executeQuery();

			Long elapsedTime = System.nanoTime() - startTime;

			int count = 0;
			
			while (rs.next()) {

				String worldName = rs.getString("worldname");
				Double x = rs.getDouble("x");
				Double y = rs.getDouble("y");
				Double z = rs.getDouble("z");
				
				World world;

				try {
					world = plugin.getServer().getWorld(worldName);
				} catch (Exception e) {
					plugin.getLogger().warning("Stored destination has unloaded world: " 
							+ worldName + ". Skipping record.");
					continue;
				}

				Location location = new Location(world,x,y,z);
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
					+ "fetch records from the " + getName() + " datastore.");
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
	 * Retrieve all road block location records from SQLite datastore
	 * @return List of location records
	 */
	@Override
	Set<Location> selectAllRecords() {
		
		Set<Location> returnSet = new HashSet<Location>();
	
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(Queries.getQuery("SelectAllBlocks"));
	
			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();
	
			while (rs.next()) {
	
				String worldName = rs.getString("worldname");
				Double x = rs.getDouble("x");
				Double y = rs.getDouble("y");
				Double z = rs.getDouble("z");
				
				World world;
	
				try {
					world = plugin.getServer().getWorld(worldName);
				} catch (Exception e) {
					plugin.getLogger().warning("Stored block has unloaded world: " 
							+ worldName + ". Skipping record.");
					continue;
				}
	
				Location location = new Location(world,x,y,z);
				returnSet.add(location);
			}
		}
		catch (Exception e) {
	
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch all records from the " + getName() + " datastore.");
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
	 * @param chunk
	 */
	private void addCache(final Chunk chunk) {

		Set<Location> blockSet = selectBlockLocationsInChunk(chunk);

		int count = 0;
		
		for (Location location : blockSet) {
			blockCache.put(location,CacheStatus.TRUE);
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
	 * @param chunk
	 */
	private void flushCache(final Chunk chunk) {
		
		int count = 0;
		Long startTime = System.nanoTime();
		for (Location location : blockCache.keySet()) {
			if (location.getChunk().equals(chunk)) {
				blockCache.remove(location);
				count++;
			}
		}
		chunkCache.remove(chunk.getBlock(0, 0, 0).getLocation());
		
		Long elapsedTime = (System.nanoTime() - startTime);
		if (plugin.profile) {
			if (count > 0) {
				plugin.getLogger().info(count + " blocks removed from cache in " 
				+ TimeUnit.NANOSECONDS.toMicros(elapsedTime) + " microseconds.");
			}
		}
	}
	

	/**
	 * Check if road block locations for a chunk are loaded in the cache
	 * @param location
	 * @return true if chunk is cached, false if not
	 */
	private boolean isChunkCached(final Location location) {
		
		Location chunkLoc = location.getChunk().getBlock(0, 0, 0).getLocation();
		
		if (chunkCache.contains(chunkLoc)) {
			if (plugin.debug) {
				plugin.getLogger().info("Chunk is cached.");
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * Event listener for chunk unload event
	 * @param event
	 */
	@EventHandler
	void onChunkUnload(final ChunkUnloadEvent event) {
		flushCache(event.getChunk());
	}

}
