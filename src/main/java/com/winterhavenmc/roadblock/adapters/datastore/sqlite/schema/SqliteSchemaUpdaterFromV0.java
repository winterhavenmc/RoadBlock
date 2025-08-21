package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteMessage;
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
	private final LocaleProvider localeProvider;
	private final BlockRepository blockRepository;


	public SqliteSchemaUpdaterFromV0(final Plugin plugin,
	                                 final Connection connection,
	                                 final LocaleProvider localeProvider,
	                                 final BlockRepository blockRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.localeProvider = localeProvider;
		this.blockRepository = blockRepository;
	}


	@Override
	public void update()
	{
		int schemaVersion = SqliteSchemaUpdater.getSchemaVersion(plugin, connection, localeProvider);

		if (schemaVersion == 0 && tableExists(connection, "Graveyards"))
		{
			updateBlockTableSchema(connection, schemaVersion);
		}
	}


	private void updateBlockTableSchema(final Connection connection, final int schemaVersion)
	{
		Set<BlockLocation.Valid> existingBlockRecords = blockRepository.getAll();

		try (final Statement statement = connection.createStatement())
		{
			ResultSet resultSet = statement.executeQuery(SqliteQueries.getQuery("SelectBlockTable"));
			if (resultSet.next())
			{
				statement.executeUpdate(SqliteQueries.getQuery("DropBlockTable"));
				statement.executeUpdate(SqliteQueries.getQuery("DropChunkIndex"));
				statement.executeUpdate(SqliteQueries.getQuery("CreateBlockTable"));
				statement.executeUpdate(SqliteQueries.getQuery("CreateChunkIndex"));

				setSchemaVersion(connection, plugin.getLogger(), localeProvider, schemaVersion);
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = this.blockRepository.save(existingBlockRecords);
		plugin.getLogger().info(SqliteMessage.SCHEMA_BLOCK_RECORDS_MIGRATED_NOTICE.getLocalizedMessage(localeProvider.getLocale(), count, schemaVersion));
	}

}
