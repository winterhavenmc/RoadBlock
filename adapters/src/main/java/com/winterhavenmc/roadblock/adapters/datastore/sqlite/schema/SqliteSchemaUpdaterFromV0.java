package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;
import com.winterhavenmc.roadblock.adapters.datastore.DatastoreMessage;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteQueries;
import com.winterhavenmc.roadblock.models.blocklocation.BlockLocation;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteConnectionProvider.DATASTORE_NAME;


public final class SqliteSchemaUpdaterFromV0 implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final ConfigRepository configRepository;
	private final BlockRepository blockRepository;


	public SqliteSchemaUpdaterFromV0(final Plugin plugin,
	                                 final Connection connection,
	                                 final ConfigRepository configRepository,
	                                 final BlockRepository blockRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.configRepository = configRepository;
		this.blockRepository = blockRepository;
	}


	@Override
	public void update()
	{
		int schemaVersion = SqliteSchemaUpdater.getSchemaVersion(plugin, connection, configRepository);

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

				setSchemaVersion(connection, plugin.getLogger(), configRepository, schemaVersion);
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(DatastoreMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = this.blockRepository.save(existingBlockRecords);
		plugin.getLogger().info(DatastoreMessage.SCHEMA_BLOCK_RECORDS_MIGRATED_NOTICE.getLocalizedMessage(configRepository.locale(), count, schemaVersion));
	}

}
