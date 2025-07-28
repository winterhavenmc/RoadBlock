package com.winterhavenmc.roadblock.adapters.datastore;

import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
import org.bukkit.Chunk;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class SQLiteBlockQueryHelper
{
	ResultSet selectAllRecords(final PreparedStatement preparedStatement) throws SQLException
	{
		return preparedStatement.executeQuery();
	}


	ResultSet selectRecordsInChunk(final Chunk chunk, final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, chunk.getWorld().getUID().getMostSignificantBits());
		preparedStatement.setLong(2, chunk.getWorld().getUID().getLeastSignificantBits());
		preparedStatement.setInt( 3, chunk.getX());
		preparedStatement.setInt( 4, chunk.getZ());
		return preparedStatement.executeQuery();
	}


	ResultSet selectNearbyBlocks(final BlockLocation.Valid validBlockLocation,
	                             final int distance,
	                             final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, validBlockLocation.worldUid().getMostSignificantBits());
		preparedStatement.setLong(2, validBlockLocation.worldUid().getLeastSignificantBits());
		preparedStatement.setInt( 3, validBlockLocation.blockX() - distance);
		preparedStatement.setInt( 4, validBlockLocation.blockX() + distance);
		preparedStatement.setInt( 5, validBlockLocation.blockZ() - distance);
		preparedStatement.setInt( 6, validBlockLocation.blockZ() + distance);
		return preparedStatement.executeQuery();
	}


	int deleteRecords(final BlockLocation.Valid validLocation,
	                  final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, validLocation.worldUid().getMostSignificantBits());
		preparedStatement.setLong(2, validLocation.worldUid().getLeastSignificantBits());
		preparedStatement.setInt( 3, validLocation.blockX());
		preparedStatement.setInt( 4, validLocation.blockY());
		preparedStatement.setInt( 5, validLocation.blockZ());
		return preparedStatement.executeUpdate();
	}


	int insertRecord(final BlockLocation.Valid blockLocation,
	                 final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setString(1, blockLocation.worldName());
		preparedStatement.setLong(  2, blockLocation.worldUid().getMostSignificantBits());
		preparedStatement.setLong(  3, blockLocation.worldUid().getLeastSignificantBits());
		preparedStatement.setInt(   4, blockLocation.blockX());
		preparedStatement.setInt(   5, blockLocation.blockY());
		preparedStatement.setInt(   6, blockLocation.blockZ());
		preparedStatement.setInt(   7, blockLocation.chunkX());
		preparedStatement.setInt(   8, blockLocation.chunkZ());
		return preparedStatement.executeUpdate();
	}

}
