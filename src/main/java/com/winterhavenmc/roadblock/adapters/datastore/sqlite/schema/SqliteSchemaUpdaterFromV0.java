package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteQueries;
import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
import com.winterhavenmc.roadblock.ports.datastore.BlockRepository;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;


public final class SqliteSchemaUpdaterFromV0 implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final BlockRepository blockRepository;


	public SqliteSchemaUpdaterFromV0(final Plugin plugin, final Connection connection, final BlockRepository blockRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.blockRepository = blockRepository;
	}


	@Override
	public void update() throws SQLException
	{
		int schemaVersion = getSchemaVersion(plugin, connection);

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0)
		{
			int count;
			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("SelectBlockTable"));
			if (resultSet.next())
			{
				Set<BlockLocation.Valid> existingRecords = this.blockRepository.getAll();
				statement.executeUpdate(SqliteQueries.getQuery("DropBlockTable"));
				statement.executeUpdate(SqliteQueries.getQuery("DropChunkIndex"));
				statement.executeUpdate(SqliteQueries.getQuery("CreateBlockTable"));
				statement.executeUpdate(SqliteQueries.getQuery("CreateChunkIndex"));

				count = this.blockRepository.save(existingRecords);
				plugin.getLogger().info(count + " block records migrated to schema v1");
			}

			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = 1");
		}

		// execute table creation statement
		statement.executeUpdate(SqliteQueries.getQuery("CreateBlockTable"));

		// execute index creation statement
		statement.executeUpdate(SqliteQueries.getQuery("CreateChunkIndex"));
	}

}
