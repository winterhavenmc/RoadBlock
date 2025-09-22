package com.winterhavenmc.roadblock.core;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;
import com.winterhavenmc.roadblock.core.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.roadblock.core.storage.BlockManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public interface PluginController
{
	void startUp(JavaPlugin plugin, ConnectionProvider connectionProvider);

	void shutDown();


	record CommandContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder, SoundConfiguration soundConfig,
	                                      WorldManager worldManager, BlockManager blockManager, HighlightManager highlightManager) { }

	record ListenerContextContainer(Plugin plugin, MessageBuilder messageBuilder, SoundConfiguration soundConfig,
	                                       WorldManager worldManager, BlockManager blockManager, HighlightManager highlightManager) { }

	record MetricsContextContainer(Plugin plugin, BlockManager blockManager) { }
}
