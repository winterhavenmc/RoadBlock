package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteQueries;
import com.winterhavenmc.roadblock.ports.datastore.BlockRepository;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public sealed interface SqliteSchemaUpdater permits SqliteSchemaUpdaterFromV0, SqliteSchemaUpdaterNoOp
{
	void update() throws SQLException;


	static SqliteSchemaUpdater create(final Plugin plugin,
	                                         final Connection connection,
	                                         final BlockRepository blockRepository)
	{
		int version = getSchemaVersion(plugin, connection);

		return (version == 0)
				? new SqliteSchemaUpdaterFromV0(plugin, connection, blockRepository)
				: new SqliteSchemaUpdaterNoOp(plugin);
	}


	private static int getSchemaVersion(final Plugin plugin, final Connection connection)
	{
		int version = -1;

		try
		{
			final Statement statement = connection.createStatement();

			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("GetUserVersion"));

			if (resultSet.next())
			{
				version = resultSet.getInt(1);
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("Could not read schema version!");
		}
		return version;
	}

}
