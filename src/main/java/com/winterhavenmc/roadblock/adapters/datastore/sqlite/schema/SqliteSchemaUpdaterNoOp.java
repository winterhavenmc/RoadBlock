package com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema;

import org.bukkit.plugin.Plugin;


public final class SqliteSchemaUpdaterNoOp implements SqliteSchemaUpdater
{
	private final Plugin plugin;

	public SqliteSchemaUpdaterNoOp(final Plugin plugin)
	{
		this.plugin = plugin;
	}


	@Override
	public void update()
	{
		plugin.getLogger().info("SQLite schema is up to date.");
	}

}
