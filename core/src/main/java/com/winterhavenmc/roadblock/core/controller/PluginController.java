package com.winterhavenmc.roadblock.core.controller;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.roadblock.core.ports.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;

import org.bukkit.plugin.java.JavaPlugin;


public interface PluginController
{
	void startUp(JavaPlugin plugin, MessageBuilder messageBuilder, HighlightManager highlightManager,
	             ConnectionProvider connectionProvider, MaterialsProvider materials);
	void shutDown();
}
