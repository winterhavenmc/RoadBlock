package com.winterhavenmc.roadblock.plugin;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteConnectionProvider;

import com.winterhavenmc.roadblock.adapters.listeners.bukkit.BukkitBlockEventListener;
import com.winterhavenmc.roadblock.core.controller.PluginController;
import com.winterhavenmc.roadblock.core.controller.RoadBlockPluginController;
import com.winterhavenmc.roadblock.adapters.highlights.bukkit.BukkitHighlightManager;
import com.winterhavenmc.roadblock.core.ports.highlights.HighlightManager;
import com.winterhavenmc.roadblock.adapters.listeners.bukkit.BukkitEntityEventListener;
import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.roadblock.core.util.SimpleApi;

import org.bukkit.plugin.java.JavaPlugin;

public class Bootsrap extends JavaPlugin
{
	private PluginController pluginController;
	private static SimpleApi simpleApi;


	@Override
	public void onEnable()
	{
		pluginController = new RoadBlockPluginController();

		final MaterialsProvider materials = new MaterialsProvider(this);
		final ConnectionProvider connectionProvider = new SqliteConnectionProvider(this, materials);
		final BlockRepository blocks = connectionProvider.blocks();

		final MessageBuilder messageBuilder = MessageBuilder.create(this);
		final HighlightManager highlightManager = new BukkitHighlightManager(this);

		new BukkitBlockEventListener(this, messageBuilder, blocks);
		new BukkitEntityEventListener(this, messageBuilder, blocks, materials, highlightManager);

		pluginController.startUp(this, messageBuilder, highlightManager, connectionProvider, materials);

		Bootsrap.simpleApi = new SimpleApi(this, blocks, materials);
	}


	@Override
	public void onDisable()
	{
		pluginController.shutDown();
	}


	@SuppressWarnings("unused")
	public static SimpleApi simpleApi()
	{
		return Bootsrap.simpleApi;
	}

}
