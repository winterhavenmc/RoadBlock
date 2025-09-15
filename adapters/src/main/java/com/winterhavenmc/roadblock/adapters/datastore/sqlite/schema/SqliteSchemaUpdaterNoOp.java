package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteMessage;
import org.bukkit.plugin.Plugin;


public final class SqliteSchemaUpdaterNoOp implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final LocaleProvider localeProvider;


	public SqliteSchemaUpdaterNoOp(final Plugin plugin, final LocaleProvider localeProvider)
	{
		this.plugin = plugin;
		this.localeProvider = localeProvider;
	}


	@Override
	public void update()
	{
		plugin.getLogger().info(SqliteMessage.SCHEMA_UP_TO_DATE_NOTICE.getLocalizedMessage(localeProvider.getLocale()));
	}

}
