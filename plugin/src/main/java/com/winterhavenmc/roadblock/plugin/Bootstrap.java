package com.winterhavenmc.roadblock.plugin;

import com.winterhavenmc.roadblock.core.PluginController;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteConnectionProvider;

import com.winterhavenmc.roadblock.core.RoadBlockPluginController;
import org.bukkit.plugin.java.JavaPlugin;


public class Bootstrap extends JavaPlugin
{
	PluginController pluginController;
	SqliteConnectionProvider connectionProvider;


	@Override
	public void onEnable()
	{
		pluginController = new RoadBlockPluginController();
		connectionProvider = new SqliteConnectionProvider(this);
		pluginController.startUp(this, connectionProvider);
	}


	@Override
	public void onDisable()
	{
		pluginController.shutDown();
	}
}
