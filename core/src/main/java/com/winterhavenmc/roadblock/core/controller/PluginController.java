package com.winterhavenmc.roadblock.core.controller;

import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.java.JavaPlugin;


public interface PluginController
{
	void startUp(JavaPlugin plugin, ConnectionProvider connectionProvider);
	void shutDown();
}
