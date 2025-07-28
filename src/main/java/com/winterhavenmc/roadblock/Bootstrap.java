package com.winterhavenmc.roadblock;

import com.winterhavenmc.roadblock.adapters.datastore.SQLiteConnectionProvider;
import com.winterhavenmc.roadblock.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.Plugin;

public final class Bootstrap
{
	public static ConnectionProvider getConnectionProvider(Plugin plugin)
	{
		return new SQLiteConnectionProvider(plugin);
	}

}
