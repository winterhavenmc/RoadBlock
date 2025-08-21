package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
import com.winterhavenmc.roadblock.ports.datastore.BlockRepository;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.*;


public class SQLiteBlockRepository implements BlockRepository
{
	private final Plugin plugin;
	private final LocaleProvider localeProvider;
	private final Connection connection;
	private final SQLiteBlockQueryExecutor blockQueryHelper = new SQLiteBlockQueryExecutor();
	private final SQLiteBlockRowMapper blockRowMapper;
	private final int schemaVersion;
	private final BlockLocationCache blockCache;
	private final Collection<Location> chunkCache;


	public SQLiteBlockRepository(final Plugin plugin, final Connection connection, final LocaleProvider localeProvider)
	{
		this.plugin = plugin;
		this.localeProvider = localeProvider;
		this.connection = connection;
		this.blockCache = BlockLocationCache.getInstance();
		this.chunkCache = new HashSet<>();
		this.schemaVersion = getSchemaVersion();
		this.blockRowMapper = new SQLiteBlockRowMapper(plugin, localeProvider);
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
			plugin.getLogger().warning(SqliteMessage.SCHEMA_VERSION_ERROR.getLocalizedMessage(localeProvider.getLocale()));
		}

		return version;
	}


	/**
	 * Insert records into the SQLite datastore
	 *
	 * @param blockLocations Set of records to insert
	 */
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
					count += blockQueryHelper.insertRecord(validBlockLocation, preparedStatement);
					blockCache.put(validBlockLocation, CacheStatus.RESIDENT);
				}
			}
			connection.commit();
			connection.setAutoCommit(true);
			return count;
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.INSERT_BLOCK_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
			return 0;
		}
	}


	/**
	 * Retrieve all road block location records from SQLite datastore
	 *
	 * @return Set of location records
	 */
	public Set<BlockLocation.Valid> getAll()
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllBlocks")))
		{
			ResultSet resultSet = blockQueryHelper.selectAllRecords(preparedStatement);

			return blockRowMapper.mapLocations(resultSet, schemaVersion);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SELECT_ALL_BLOCKS_ERROR.getLocalizedMessage(localeProvider.getLocale()));
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
			plugin.getLogger().warning(SqliteMessage.SELECT_BLOCK_COUNT_ERROR.getLocalizedMessage(localeProvider.getLocale()));
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
			ResultSet resultSet = blockQueryHelper.selectRecordsInChunk(chunk, preparedStatement);
			return blockRowMapper.mapLocations(resultSet, schemaVersion);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SELECT_BLOCKS_IN_CHUNK_ERROR.getLocalizedMessage(localeProvider.getLocale()));
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
				ResultSet resultSet = blockQueryHelper.selectNearbyBlocks(validBlockLocation, distance, preparedStatement);
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
				plugin.getLogger().warning(SqliteMessage.SELECT_BLOCKS_BY_PROXIMITY_ERROR.getLocalizedMessage(localeProvider.getLocale()));
				plugin.getLogger().warning(sqlException.getLocalizedMessage());
			}
		}

		return results;
	}


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
						blockQueryHelper.deleteRecords(validLocation, preparedStatement);
						count += 1;
					}
					catch (SQLException sqlException)
					{
						plugin.getLogger().warning(SqliteMessage.DELETE_BLOCK_RECORD_ERROR.getLocalizedMessage(localeProvider.getLocale()));
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
			plugin.getLogger().warning(SqliteMessage.DELETE_BLOCK_RECORD_ERROR.getLocalizedMessage(localeProvider.getLocale()));
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

}
