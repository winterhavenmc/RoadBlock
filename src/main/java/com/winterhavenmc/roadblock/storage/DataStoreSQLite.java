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

import com.winterhavenmc.roadblock.model.RoadBlock;
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


final class DataStoreSQLite extends DataStoreAbstract implements DataStore, Listener
{
	// reference to main class
	private final JavaPlugin plugin;

	// block cache
	private final BlockLocationCache blockCache;

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
	DataStoreSQLite(final JavaPlugin plugin)
	{
		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set file path for datastore file
		this.dataFilePath = plugin.getDataFolder() + File.separator + type.getStorageName();

		// create empty block cache
		this.blockCache = BlockLocationCache.getInstance();

		// create empty chunk location cache
		this.chunkCache = new HashSet<>();

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

	}


	/**
	 * Initialize SQLite datastore
	 */
	@Override
	public void initialize() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, do nothing and return
		if (this.isInitialized())
		{
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


	private int getSchemaVersion()
	{
		int version = -1;

		try
		{
			final Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			while (rs.next())
			{
				version = rs.getInt(1);
			}
		} catch (SQLException e)
		{
			plugin.getLogger().warning("Could not get schema version!");
		}
		return version;
	}


	private void updateSchema() throws SQLException
	{
		schemaVersion = getSchemaVersion();

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0)
		{
			int count;
			ResultSet rs = statement.executeQuery(Queries.getQuery("SelectBlockTable"));
			if (rs.next())
			{
				Collection<RoadBlock.BlockLocation> existingRecords = selectAllRecords();
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
	public void sync()
	{
		// no action necessary for this storage type
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public void close()
	{
		try
		{
			connection.close();
			plugin.getLogger().info(this + " datastore connection closed.");
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while closing the " + this + " datastore.");
			plugin.getLogger().warning(e.getMessage());
		}
		setInitialized(false);
	}


	/**
	 * Delete the SQLite datastore file
	 */
	@Override
	public boolean delete()
	{
		// get reference to dataStore file in file system
		File dataStoreFile = new File(dataFilePath);

		// if file exists, delete file
		boolean result = false;
		if (dataStoreFile.exists())
		{
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
	public boolean isProtected(final Location location)
	{
		return RoadBlock.BlockLocation.of(location) instanceof RoadBlock.BlockLocation.Valid validLocation
				&& isProtected(validLocation, location);
	}


	private boolean isProtected(RoadBlock.BlockLocation.Valid validLocation, Location location)
	{
		if (!isChunkCached(location))
		{
			cacheChunk(location.getChunk());
		}

		CacheStatus status = blockCache.get(validLocation);
		return status == CacheStatus.RESIDENT || status == CacheStatus.PENDING_INSERT;
	}


	/**
	 * Insert records into the SQLite datastore
	 *
	 * @param blockLocations Collection of records to insert
	 */
	@Override
	synchronized public int insertRecords(final Collection<RoadBlock.BlockLocation> blockLocations)
	{
		blockLocations.forEach(location -> blockCache.put(location, CacheStatus.PENDING_INSERT));
		new AsyncInsert(blockLocations).runTaskAsynchronously(plugin);
		return blockLocations.size();
	}


	/**
	 * Delete a list of locations from the SQLite datastore
	 *
	 * @param blockLocations Collection of locations
	 */
	@Override
	synchronized public int deleteRecords(final Collection<RoadBlock.BlockLocation> blockLocations)
	{
		blockLocations.forEach(location -> blockCache.put(location, CacheStatus.PENDING_DELETE));
		new AsyncDelete(blockLocations).runTaskAsynchronously(plugin);
		return blockLocations.size();
	}


	/**
	 * Retrieve all road block location records from SQLite datastore
	 *
	 * @return List of location records
	 */
	synchronized public Collection<RoadBlock.BlockLocation> selectAllRecords()
	{
		final Collection<RoadBlock.BlockLocation> results = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllBlocks"));
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
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
				if (schemaVersion == 0)
				{
					world = plugin.getServer().getWorld(worldName);
				}
				// else get world object from stored world uuid
				else
				{
					worldUidMsb = rs.getLong("worlduidmsb");
					worldUidLsb = rs.getLong("worlduidlsb");
					UUID worldUid = new UUID(worldUidMsb, worldUidLsb);
					world = plugin.getServer().getWorld(worldUid);
				}

				// if world is null, skip adding record to return set
				if (world == null)
				{
					plugin.getLogger().warning("Stored block has unloaded world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				// add block record to return set
				results.add(RoadBlock.BlockLocation.of(world.getName(), world.getUID(), blockX, blockY, blockZ, chunkX, chunkZ));
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch all records from the " + this + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return results;
	}


	/**
	 * Retrieve all road block locations in chunk from the SQLite datastore
	 *
	 * @param chunk the chunk for which to retrieve all road block locations from the datastore
	 * @return Collection of locations
	 */
	@Override
	synchronized public Collection<RoadBlock.BlockLocation.Valid> selectRecordsInChunk(final Chunk chunk)
	{
		final Collection<RoadBlock.BlockLocation.Valid> results = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectBlocksInChunk"));

			long worldUidMsb = chunk.getWorld().getUID().getMostSignificantBits();
			long worldUidLsb = chunk.getWorld().getUID().getLeastSignificantBits();

			preparedStatement.setLong(1, worldUidMsb);
			preparedStatement.setLong(2, worldUidLsb);
			preparedStatement.setInt(3, chunk.getX());
			preparedStatement.setInt(4, chunk.getZ());

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				final long worldUidMSB = rs.getLong("worlduidmsb");
				final long worldUidLSB = rs.getLong("worlduidlsb");
				String worldName = rs.getString("worldname");
				int blockX = rs.getInt("x");
				int blockY = rs.getInt("y");
				int blockZ = rs.getInt("z");
				int chunkX = rs.getInt("chunk_x");
				int chunkZ = rs.getInt("chunk_z");

				// get world by uid
				World world = plugin.getServer().getWorld(new UUID(worldUidMSB, worldUidLSB));

				// if world is not null, add block record to return set
				if (world != null && RoadBlock.BlockLocation.of(world.getName(), world.getUID(),
						blockX, blockY, blockZ, chunkX, chunkZ) instanceof RoadBlock.BlockLocation.Valid validBlockLocation)
				{
					results.add(validBlockLocation);
				}
				else
				{
					plugin.getLogger().warning("Stored location has invalid world: "
							+ worldName + ". Skipping record.");
				}
			}
		}
		catch (SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to "
					+ "fetch records from the " + this + " datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return results;
	}


	public Set<RoadBlock.BlockLocation> selectNearbyBlockLocations(final RoadBlock.BlockLocation blockLocation, final int distance)
	{
		if (blockLocation instanceof RoadBlock.BlockLocation.Valid validBlockLocation)
		{
			Set<RoadBlock.BlockLocation> results = new HashSet<>();

			try
			{
				PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectNearbyBlocks"));
				preparedStatement.setLong(1, validBlockLocation.worldUid().getMostSignificantBits());
				preparedStatement.setLong(2, validBlockLocation.worldUid().getLeastSignificantBits());
				preparedStatement.setInt(3, validBlockLocation.blockX() - distance);
				preparedStatement.setInt(4, validBlockLocation.blockX() + distance);
				preparedStatement.setInt(5, validBlockLocation.blockZ() - distance);
				preparedStatement.setInt(6, validBlockLocation.blockZ() + distance);
				ResultSet rs = preparedStatement.executeQuery();

				while (rs.next())
				{
					final UUID worldUid = new UUID(rs.getInt("worldMsb"),
							rs.getInt(rs.getInt("worldLsb")));
					final double x = rs.getDouble("x");
					final double y = rs.getDouble("y");
					final double z = rs.getDouble("z");

					World world = plugin.getServer().getWorld(worldUid);

					if (world != null)
					{
						// get location for stored record
						RoadBlock.BlockLocation newBlockLocation = RoadBlock.BlockLocation.of(new Location(world, x, y, z));

						if (newBlockLocation instanceof RoadBlock.BlockLocation.Valid)
						{
							results.add(newBlockLocation);
						}
					}
				}

			}
			catch (final SQLException e)
			{
				// output simple error message
				plugin.getLogger().warning("An error occurred while trying to "
						+ "select nearby block records from the " + this + " datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}

			return results;
		}
		else
		{
			return Collections.emptySet();
		}

	}


	public Set<Location> selectNearbyBlocks(final Location location, final int distance)
	{
		if (RoadBlock.BlockLocation.of(location) instanceof RoadBlock.BlockLocation.Valid validBlockLocation)
		{
			Set<Location> results = new HashSet<>();

			try
			{
				PreparedStatement preparedStatement =
						connection.prepareStatement(Queries.getQuery("SelectNearbyBlocks"));

				preparedStatement.setLong(1, validBlockLocation.worldUid().getMostSignificantBits());
				preparedStatement.setLong(2, validBlockLocation.worldUid().getLeastSignificantBits());
				preparedStatement.setInt(3, validBlockLocation.blockX() - distance);
				preparedStatement.setInt(4, validBlockLocation.blockX() + distance);
				preparedStatement.setInt(5, validBlockLocation.blockZ() - distance);
				preparedStatement.setInt(6, validBlockLocation.blockZ() + distance);

				// execute sql query
				ResultSet rs = preparedStatement.executeQuery();

				while (rs.next())
				{
					final double x = rs.getDouble("x");
					final double y = rs.getDouble("y");
					final double z = rs.getDouble("z");

					// get location for stored record
					Location newLocation = new Location(location.getWorld(), x, y, z);

					// add location to result set
					results.add(newLocation);
				}

			}
			catch (final SQLException e)
			{
				// output simple error message
				plugin.getLogger().warning("An error occurred while trying to "
						+ "select nearby block records from the " + this + " datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}

			return results;
		}
		else
		{
			return Collections.emptySet();
		}
	}


	/**
	 * Add all road block locations within chunk to cache
	 *
	 * @param chunk the chunk for which to load all road block locations into cache
	 */
	private void cacheChunk(final Chunk chunk)
	{
		final Collection<RoadBlock.BlockLocation.Valid> blockSet = selectRecordsInChunk(chunk);

		for (RoadBlock.BlockLocation blockLocation : blockSet)
		{
			blockCache.put(blockLocation, CacheStatus.RESIDENT);
		}

		chunkCache.add(chunk.getBlock(0, 0, 0).getLocation());
	}


	/**
	 * Remove all road block locations within chunk from cache<br>
	 * called on chunk unload event
	 *
	 * @param chunk the chunk for which to remove all road block locations from cache
	 */
	private void flushCache(final Chunk chunk)
	{
		for (RoadBlock.BlockLocation blockLocation : blockCache.keySet())
		{
			if (blockLocation instanceof RoadBlock.BlockLocation.Valid validLocation
					&& validLocation.worldUid().equals(chunk.getWorld().getUID())
					&& validLocation.chunkX() == chunk.getX()
					&& validLocation.chunkZ() == chunk.getZ())
			{
				blockCache.remove(blockLocation);
			}
		}

		chunkCache.remove(chunk.getBlock(0, 0, 0).getLocation());
	}


	/**
	 * Check if road block locations for a chunk are loaded in the cache
	 *
	 * @param location the location to test to determine if all chunk road blocks are cached
	 * @return {@code true} if chunk is cached, {@code false} if not
	 */
	private boolean isChunkCached(final Location location)
	{
		return chunkCache.contains(location.getChunk().getBlock(0, 0, 0).getLocation());
	}


	@Override
	synchronized public int getTotalBlocks()
	{
		int result = 0;

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("CountAllBlocks"));
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next())
			{
				result = rs.getInt("rowcount");
			}
		}
		catch (final SQLException e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to " +
					"get the row count from the " + this + " datastore.");

			plugin.getLogger().warning(e.getLocalizedMessage());
		}

		return result;
	}


	/**
	 * Event listener for chunk unload event
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public void onChunkUnload(final ChunkUnloadEvent event)
	{
		flushCache(event.getChunk());
	}


	private class AsyncInsert extends BukkitRunnable
	{
		private final Collection<RoadBlock.BlockLocation> blockLocations;

		public AsyncInsert(Collection<RoadBlock.BlockLocation> blockLocations)
		{
			this.blockLocations = blockLocations;
		}

		@Override
		public void run()
		{
			try
			{
				// set connection to transaction mode
				connection.setAutoCommit(false);

				for (RoadBlock.BlockLocation blockLocation : blockLocations)
				{
					if (blockLocation instanceof RoadBlock.BlockLocation.Valid(
							String worldName, UUID worldUid,
							int blockX, int blockY, int blockZ,
							int chunkX, int chunkZ))
					{
						// synchronize on database connection
						synchronized (this)
						{
							// create prepared statement
							PreparedStatement preparedStatement =
									connection.prepareStatement(Queries.getQuery("InsertOrIgnoreBlock"));

							preparedStatement.setString(1, worldName);
							preparedStatement.setLong(2, worldUid.getMostSignificantBits());
							preparedStatement.setLong(3, worldUid.getLeastSignificantBits());
							preparedStatement.setInt(4, blockX);
							preparedStatement.setInt(5, blockY);
							preparedStatement.setInt(6, blockZ);
							preparedStatement.setInt(7, chunkX);
							preparedStatement.setInt(8, chunkZ);

							// execute prepared statement
							preparedStatement.executeUpdate();
						}

						blockCache.put(blockLocation, CacheStatus.RESIDENT);
					}
				}
				connection.commit();
				connection.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				// output simple error message
				plugin.getLogger().warning("An error occurred while attempting to "
						+ "insert a block in the " + this + " datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}


	private class AsyncDelete extends BukkitRunnable
	{
		private final Collection<RoadBlock.BlockLocation> blockLocations;

		public AsyncDelete(Collection<RoadBlock.BlockLocation> blockLocations)
		{
			this.blockLocations = blockLocations;
		}

		@Override
		public void run()
		{
			try
			{
				connection.setAutoCommit(false);

				for (final RoadBlock.BlockLocation blockLocation : blockLocations)
				{
					// if key is null return, skip to next location
					if (!(blockLocation instanceof RoadBlock.BlockLocation.Valid validLocation))
					{
						continue;
					}

					try
					{
						// synchronize on database connection
						synchronized (this)
						{
							PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteBlock"));
							preparedStatement.setLong(1, validLocation.worldUid().getMostSignificantBits());
							preparedStatement.setLong(2, validLocation.worldUid().getLeastSignificantBits());
							preparedStatement.setInt(3, validLocation.blockX());
							preparedStatement.setInt(4, validLocation.blockY());
							preparedStatement.setInt(5, validLocation.blockZ());
							preparedStatement.executeUpdate();
						}
					}
					catch (SQLException e)
					{
						// output simple error message
						plugin.getLogger().warning("An error occurred while attempting to "
								+ "delete a block from the " + this + " datastore.");
						plugin.getLogger().warning(e.getLocalizedMessage());
					}

					blockCache.remove(blockLocation);
				}

				connection.commit();
				connection.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				// output simple error message
				plugin.getLogger().warning("An error occurred while attempting to "
						+ "delete a block from the " + this + " datastore.");
				plugin.getLogger().warning(e.getLocalizedMessage());
			}
		}
	}

}
