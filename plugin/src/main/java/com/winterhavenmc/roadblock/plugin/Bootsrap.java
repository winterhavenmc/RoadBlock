package com.winterhavenmc.roadblock.plugin;

import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteConnectionProvider;

import com.winterhavenmc.roadblock.core.controller.PluginController;
import com.winterhavenmc.roadblock.core.controller.RoadBlockPluginController;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;

import org.bukkit.plugin.java.JavaPlugin;

public class Bootsrap extends JavaPlugin
{
	private PluginController pluginController;


	@Override
	public void onEnable()
	{
		final ConnectionProvider connectionProvider = new SqliteConnectionProvider(this);
		pluginController = new RoadBlockPluginController();

		pluginController.startUp(this, connectionProvider);
	}


	@Override
	public void onDisable()
	{
		pluginController.shutDown();
	}

}
