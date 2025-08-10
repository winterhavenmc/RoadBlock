package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
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


	SQLiteBlockRowMapper(final Plugin plugin)
	{
		this.plugin = plugin;
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

			// if world is not null, add block record to return set
			if (world != null && BlockLocation.of(world.getName(), world.getUID(),
					blockX, blockY, blockZ, chunkX, chunkZ) instanceof BlockLocation.Valid validBlockLocation)
			{
				results.add(validBlockLocation);
			}
			else
			{
				plugin.getLogger().warning("Stored location has invalid world: "
						+ worldName + ". Skipping record.");
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

			// if world is not null, add block record to return set
			if (world != null && BlockLocation.of(world.getName(), world.getUID(),
					blockX, blockY, blockZ, chunkX, chunkZ) instanceof BlockLocation.Valid validBlockLocation)
			{
				results.add(validBlockLocation);
			}
			else
			{
				plugin.getLogger().warning("Stored location has invalid world: "
						+ worldName + ". Skipping record.");
			}
		}

		return results;
	}

}
