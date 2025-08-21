package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteMessage;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteQueries;
import com.winterhavenmc.roadblock.ports.datastore.BlockRepository;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.logging.Logger;


public sealed interface SqliteSchemaUpdater permits SqliteSchemaUpdaterFromV0, SqliteSchemaUpdaterNoOp
{
	void update() throws SQLException;


	static SqliteSchemaUpdater create(final Plugin plugin,
                                      final Connection connection,
									  final LocaleProvider localeProvider,
                                      final BlockRepository blockRepository)
	{
		int version = getSchemaVersion(plugin, connection, localeProvider);

		return (version == 0)
				? new SqliteSchemaUpdaterFromV0(plugin, connection, localeProvider, blockRepository)
				: new SqliteSchemaUpdaterNoOp(plugin, localeProvider);
	}


	static int getSchemaVersion(final Plugin plugin, final Connection connection, final LocaleProvider localeProvider)
	{
		int version = -1;

		try
		{
			final Statement statement = connection.createStatement();
			final ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("GetUserVersion"));

			if (resultSet.next())
			{
				version = resultSet.getInt(1);
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SCHEMA_VERSION_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return version;
	}


	default void setSchemaVersion(final Connection connection,
	                              final Logger logger,
	                              final LocaleProvider localeProvider,
	                              final int version)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate("PRAGMA user_version = " + version);
		}
		catch (SQLException sqlException)
		{
			logger.warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			logger.warning(sqlException.getLocalizedMessage());
		}
	}


	default boolean tableExists(final Connection connection, final String tableName)
	{
		try (PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectTable")))
		{
			preparedStatement.setString(1, tableName);
			try (ResultSet resultSet = preparedStatement.executeQuery())
			{
				return resultSet.next(); // returns true if a row is found
			}
		}
		catch (SQLException sqlException)
		{
			return false;
		}
	}

}
