package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import com.winterhavenmc.roadblock.adapters.datastore.DatastoreMessage;

import org.bukkit.plugin.Plugin;

import static com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteConnectionProvider.DATASTORE_NAME;


public final class SqliteSchemaUpdaterNoOp implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final ConfigRepository configRepository;


	public SqliteSchemaUpdaterNoOp(final Plugin plugin, final ConfigRepository configRepository)
	{
		this.plugin = plugin;
		this.configRepository = configRepository;
	}


	@Override
	public void update()
	{
		plugin.getLogger().info(DatastoreMessage.SCHEMA_UP_TO_DATE_NOTICE.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
	}

}
