package com.winterhavenmc.roadblock.bootstrap;

import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SQLiteConnectionProvider;
import com.winterhavenmc.roadblock.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.Plugin;

public final class Bootstrap
{
	private Bootstrap() { /* private constructor to prevent instantiation */ }

	public static ConnectionProvider getConnectionProvider(Plugin plugin)
	{
		return new SQLiteConnectionProvider(plugin);
	}

}
