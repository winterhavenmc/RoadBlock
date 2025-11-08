package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;
import com.winterhavenmc.roadblock.adapters.datastore.BlockLocationCache;
import com.winterhavenmc.roadblock.adapters.datastore.CacheStatus;
import com.winterhavenmc.roadblock.adapters.datastore.DatastoreMessage;
import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.core.util.Config;
import com.winterhavenmc.roadblock.models.blocklocation.BlockLocation;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteConnectionProvider.DATASTORE_NAME;


public class SqliteBlockRepository implements BlockRepository, Listener
{
	private final Plugin plugin;
	private final Connection connection;
	private final ConfigRepository configRepository;
	private final MaterialsProvider materialsProvider;
	private final SqliteBlockRowMapper blockRowMapper;
	private final int schemaVersion;
	private final BlockLocationCache blockCache;
	private final Collection<Location> chunkCache;
	private final SqliteBlockQueryExecutor blockQueryExecutor;


	public SqliteBlockRepository(final Plugin plugin,
	                             final Connection connection,
	                             final ConfigRepository configRepository,
	                             final MaterialsProvider materialsProvider)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.configRepository = configRepository;
		this.materialsProvider = materialsProvider;
		this.blockCache = BlockLocationCache.getInstance();
		this.chunkCache = new HashSet<>();
		this.schemaVersion = getSchemaVersion();
		this.blockRowMapper = new SqliteBlockRowMapper(plugin, configRepository);
		this.blockQueryExecutor = new SqliteBlockQueryExecutor();

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

	}


	private int getSchemaVersion()
	{
		int version = 0;

		try (Statement statement = connection.createStatement())
		{
			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("GetUserVersion"));

			if (resultSet.next())
			{
				version = resultSet.getInt(1);
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SCHEMA_VERSION_ERROR.getLocalizedMessage(configRepository.locale()));
		}

		return version;
	}


	/**
	 * Insert records into the SQLite datastore
	 *
	 * @param blockLocations Set of records to insert
	 */
	@Override
	public int save(final Set<BlockLocation.Valid> blockLocations)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertOrIgnoreBlock")))
		{
			connection.setAutoCommit(false);
			int count = 0;
			for (BlockLocation blockLocation : blockLocations)
			{
				if (blockLocation instanceof BlockLocation.Valid validBlockLocation)
				{
					count += blockQueryExecutor.insertRecord(validBlockLocation, preparedStatement);
					blockCache.put(validBlockLocation, CacheStatus.RESIDENT);
				}
			}
			connection.commit();
			connection.setAutoCommit(true);
			return count;
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.INSERT_BLOCK_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
			return 0;
		}
	}


	/**
	 * Retrieve all road block location records from SQLite datastore
	 *
	 * @return Set of location records
	 */
	@Override
	public Set<BlockLocation.Valid> getAll()
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllBlocks")))
		{
			ResultSet resultSet = blockQueryExecutor.selectAllRecords(preparedStatement);

			return blockRowMapper.mapLocations(resultSet, schemaVersion);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SELECT_ALL_BLOCKS_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
			return Set.of();
		}
	}


	/**
	 * count records in blocks table
	 *
	 * @return number of records in blocks table
	 */
	@Override
	public int getTotalBlocks()
	{
		int count = 0;

		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("CountAllBlocks")))
		{
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
			{
				count = resultSet.getInt("rowcount");
			}
		}
		catch (final SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SELECT_BLOCK_COUNT_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return count;
	}


	/**
	 * Retrieve all road block locations in chunk from the SQLite datastore
	 *
	 * @param chunk the chunk for which to retrieve all road block locations from the datastore
	 * @return Collection of locations
	 */
	@Override
	public Collection<BlockLocation.Valid> getBlocksInChunk(final Chunk chunk)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectBlocksInChunk")))
		{
			ResultSet resultSet = blockQueryExecutor.selectRecordsInChunk(chunk, preparedStatement);
			return blockRowMapper.mapLocations(resultSet, schemaVersion);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SELECT_BLOCKS_IN_CHUNK_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
			return Set.of();
		}
	}


	/**
	 * Get block records for locations within {@code distance} of {@code location}
	 *
	 * @param location origin location
	 * @param distance distance from origin to select blocks
	 * @return Set of Locations that are within {@code distance} of {@code location}
	 */
	@Override
	public Set<Location> getNearbyBlocks(final Location location, final int distance)
	{
		Set<Location> results = new HashSet<>();

		if (BlockLocation.of(location) instanceof BlockLocation.Valid validBlockLocation)
		{
			try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectNearbyBlocks")))
			{
				ResultSet resultSet = blockQueryExecutor.selectNearbyBlocks(validBlockLocation, distance, preparedStatement);
				while (resultSet.next())
				{
					final double x = resultSet.getDouble("x");
					final double y = resultSet.getDouble("y");
					final double z = resultSet.getDouble("z");
					Location newLocation = new Location(location.getWorld(), x, y, z);
					results.add(newLocation);
				}

			}
			catch (final SQLException sqlException)
			{
				plugin.getLogger().warning(DatastoreMessage.SELECT_BLOCKS_BY_PROXIMITY_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
				plugin.getLogger().warning(sqlException.getLocalizedMessage());
			}
		}

		return results;
	}


	@Override
	public int delete(final Set<BlockLocation.Valid> blockLocations)
	{
		int count = 0;

		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("DeleteBlock")))
		{
			connection.setAutoCommit(false);

			for (final BlockLocation blockLocation : blockLocations)
			{
				if (blockLocation instanceof BlockLocation.Valid validLocation)
				{
					try
					{
						blockQueryExecutor.deleteRecords(validLocation, preparedStatement);
						count += 1;
					}
					catch (SQLException sqlException)
					{
						plugin.getLogger().warning(DatastoreMessage.DELETE_BLOCK_RECORD_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
						plugin.getLogger().warning(sqlException.getLocalizedMessage());
					}
				}

				blockCache.remove(blockLocation);
			}

			connection.commit();
			connection.setAutoCommit(true);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.DELETE_BLOCK_RECORD_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return count;
	}


	/**
	 * Check if road block locations for a chunk are loaded in the cache
	 *
	 * @param location the location to test to determine if all chunk road blocks are cached
	 * @return {@code true} if chunk is cached, {@code false} if not
	 */
	@Override
	public boolean isChunkCached(final Location location)
	{
		return chunkCache.contains(location.getChunk().getBlock(0, 0, 0).getLocation());
	}


	/**
	 * Remove all road block locations within chunk from cache<br>
	 * called on chunk unload event
	 *
	 * @param chunk the chunk for which to remove all road block locations from cache
	 */
	@Override
	public void flushCache(final Chunk chunk)
	{
		for (BlockLocation blockLocation : blockCache.keySet())
		{
			if (blockLocation instanceof BlockLocation.Valid validLocation
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
	 * Check if a location is a protected road block
	 *
	 * @param location the location key to check for protected status
	 * @return {@code true} if the location is protected, {@code false} if it is not
	 */
	@Override
	public boolean isProtected(final Location location)
	{
		return BlockLocation.of(location) instanceof BlockLocation.Valid validLocation
				&& isProtected(validLocation, location);
	}


	private boolean isProtected(BlockLocation.Valid validLocation, Location location)
	{
		if (!this.isChunkCached(location))
		{
			this.cacheChunk(location.getChunk());
		}

		CacheStatus status = blockCache.get(validLocation);
		return status == CacheStatus.RESIDENT || status == CacheStatus.PENDING_INSERT;
	}


	/**
	 * Add all road block locations within chunk to cache
	 *
	 * @param chunk the chunk for which to load all road block locations into cache
	 */
	private void cacheChunk(final Chunk chunk)
	{
		final Collection<BlockLocation.Valid> blockSet = this.getBlocksInChunk(chunk);

		for (BlockLocation blockLocation : blockSet)
		{
			blockCache.put(blockLocation, CacheStatus.RESIDENT);
		}

		chunkCache.add(chunk.getBlock(0, 0, 0).getLocation());
	}


	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event)
	{
		flushCache(event.getChunk());
	}



	/**
	 * Remove block locations from datastore
	 *
	 * @param locations a Collection of Locations to be deleted from the datastore
	 */
	@Override
	public int removeBlockLocations(final Collection<Location> locations)
	{
		return delete(getBlockLocations(locations));
	}


	/**
	 * Returns a Set of valid block locations from a Collection of Bukkit locations.
	 */
	public Set<BlockLocation.Valid> getBlockLocations(final Collection<Location> locations)
	{
		return locations.stream()
				.map(BlockLocation::of)
				.filter(BlockLocation.Valid.class::isInstance)
				.map(BlockLocation.Valid.class::cast)
				.collect(Collectors.toSet());
	}


	/**
	 * Insert block location records into datastore
	 *
	 * @param locations a Collection of Locations to be inserted into the datastore
	 */
	@Override
	public int storeBlockLocations(final Collection<Location> locations)
	{
		return save(getBlockLocations(locations));
	}


	/**
	 * Create Set of all blocks of valid road block material attached to location
	 *
	 * @param startLocation location to begin searching for attached road blocks
	 * @return Set of Locations of attached road blocks
	 */
	@Override
	public Set<Location> getFill(final Location startLocation, final MaterialsProvider materialsProvider)
	{
		if (startLocation == null) return Collections.emptySet();

		final Set<Location> returnSet = new HashSet<>();
		final Queue<Location> queue = new LinkedList<>();

		// put start location in queue
		queue.add(startLocation);
		while (!queue.isEmpty())
		{
			// remove location at head of queue
			Location loc = queue.poll();

			// if location is not in return set and is a road block material and is not too far from start...
			if (!returnSet.contains(loc) && materialsProvider.contains(loc.getBlock().getType())
					&& loc.distanceSquared(startLocation) < Math.pow(Config.SPREAD_DISTANCE.getInt(plugin.getConfig()), 2))
			{
				// add location to return set
				returnSet.add(loc);

				// add adjacent locations to queue
				queue.add(loc.clone().add(0, 0, 1));
				queue.add(loc.clone().add(0, 0, -1));
				queue.add(loc.clone().add(1, 0, 0));
				queue.add(loc.clone().add(-1, 0, 0));
			}
		}
		return returnSet;
	}



	/**
	 * Check if block below player is a protected road block
	 *
	 * @param player the player to is above a road block
	 * @return {@code true} if player is within three blocks above a road block, else {@code false}
	 */
	@Override
	public boolean isAboveRoad(final Player player)
	{
		// if player is null, return false
		if (player == null)
		{
			return false;
		}

		// get configured height above road
		final int distance = Config.ON_ROAD_HEIGHT.getInt(plugin.getConfig());

		// if distance is less than one, return false
		if (distance < 1)
		{
			return false;
		}

		// return result of isAboveRoad for player location and configured height
		return isAboveRoad(player.getLocation(), distance);
	}


	/**
	 * Check if block below location is a protected road block, searching down to maxDepth
	 *
	 * @param location the location to test if above a road block
	 * @param distance the distance in blocks to test below location for road blocks
	 * @return {@code true} if location is above a road block, else {@code false}
	 */
	@Override
	public boolean isAboveRoad(final Location location, final int distance)
	{
		// if passed location is null, return false
		if (location == null)
		{
			return false;
		}

		// if passed distance is less than one, return false
		if (distance < 1)
		{
			return false;
		}

		boolean result = false;
		int checkDepth = distance;

		// iterate until maxDepth reached
		while (checkDepth > 0)
		{
			// get block at checkDepth
			Block testBlock = location.getBlock().getRelative(BlockFace.DOWN, checkDepth);

			// don't check datastore unless testBlock is road block material
			if (materialsProvider.isRoadBlockMaterial(testBlock))
			{
				if (isProtected(testBlock.getLocation()))
				{
					result = true;
					break;
				}
			}

			// decrement checkDepth
			checkDepth--;
		}
		return result;
	}


	/**
	 * Check if block is a protected road block
	 *
	 * @param block the block to test
	 * @return {@code true} if the block is a protected road block, else {@code false}
	 */
	@Override
	public boolean isRoadBlock(final Block block)
	{
		if (block == null) return false;

		// check if block is road block material
		if (!materialsProvider.isRoadBlockMaterial(block))
		{
			return false;
		}

		// check if block is in cache or datastore
		return isProtected(block.getLocation());
	}

}
