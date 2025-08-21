package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
import com.winterhavenmc.roadblock.model.blocklocation.BlockLocationReason;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SQLiteBlockRowMapper
{
	private final Plugin plugin;
	private final LocaleProvider localeProvider;


	SQLiteBlockRowMapper(final Plugin plugin, final LocaleProvider localeProvider)
	{
		this.plugin = plugin;
		this.localeProvider = localeProvider;
	}


	Set<BlockLocation.Valid> mapLocations(final ResultSet resultSet, final int schemaVersion) throws SQLException
	{
		return (schemaVersion == 0)
				? mapLocationsV0(resultSet)
				: mapLocationsV1(resultSet);
	}


	Set<BlockLocation.Valid> mapLocationsV0(final ResultSet resultSet) throws SQLException
	{
		Set<BlockLocation.Valid> results = new HashSet<>();

		while (resultSet.next())
		{
			final String worldName = resultSet.getString("worldname");
			final int blockX = resultSet.getInt("x");
			final int blockY = resultSet.getInt("y");
			final int blockZ = resultSet.getInt("z");
			final int chunkX = resultSet.getInt("chunk_x");
			final int chunkZ = resultSet.getInt("chunk_z");

			// get world by uid
			World world = plugin.getServer().getWorld(worldName);

			BlockLocation blockLocation = createBlockLocation(world, worldName, blockX, blockY, blockZ, chunkX, chunkZ);
			if (blockLocation instanceof BlockLocation.Valid validBlockLocation)
			{
				results.add(validBlockLocation);
			}
		}

		return results;
	}


	Set<BlockLocation.Valid> mapLocationsV1(final ResultSet resultSet) throws SQLException
	{
		Set<BlockLocation.Valid> results = new HashSet<>();

		while (resultSet.next())
		{
			final long worldUidMSB = resultSet.getLong("worlduidmsb");
			final long worldUidLSB = resultSet.getLong("worlduidlsb");
			final String worldName = resultSet.getString("worldname");
			final int blockX = resultSet.getInt("x");
			final int blockY = resultSet.getInt("y");
			final int blockZ = resultSet.getInt("z");
			final int chunkX = resultSet.getInt("chunk_x");
			final int chunkZ = resultSet.getInt("chunk_z");

			// get world by uid
			World world = plugin.getServer().getWorld(new UUID(worldUidMSB, worldUidLSB));

			BlockLocation blockLocation = createBlockLocation(world, worldName, blockX, blockY, blockZ, chunkX, chunkZ);
			if (blockLocation instanceof BlockLocation.Valid validBlockLocation)
			{
				results.add(validBlockLocation);
			}
		}

		return results;
	}


	private BlockLocation createBlockLocation(final World world, final String worldName,
	                                          final int blockX, final int blockY, final int blockZ,
	                                          final int chunkX, final int chunkZ)
	{
		if (world == null)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_BLOCK_INVALID_WORLD_ERROR
					.getLocalizedMessage(localeProvider.getLocale(), worldName));
			return new BlockLocation.Invalid(BlockLocationReason.WORLD_NULL);
		}
		else
		{
			BlockLocation blockLocation = BlockLocation.of(world.getName(), world.getUID(),
					blockX, blockY, blockZ, chunkX, chunkZ);

			if (blockLocation instanceof BlockLocation.Invalid(BlockLocationReason reason))
			{
				plugin.getLogger().warning(SqliteMessage.CREATE_BLOCK_ERROR
						.getLocalizedMessage(localeProvider.getLocale(), reason.getMessage()));
				return new BlockLocation.Invalid(reason);
			}
			else
			{
				return blockLocation;
			}
		}
	}

}
