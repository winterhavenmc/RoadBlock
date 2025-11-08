package com.winterhavenmc.roadblock.plugin;

import com.winterhavenmc.roadblock.adapters.commands.bukkit.BukkitCommandDispatcher;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.roadblock.adapters.highlights.bukkit.BukkitHighlightManager;
import com.winterhavenmc.roadblock.adapters.listeners.bukkit.BukkitBlockEventListener;
import com.winterhavenmc.roadblock.adapters.listeners.bukkit.BukkitEntityEventListener;

import com.winterhavenmc.roadblock.core.ports.config.BukkitMaterialsProvider;
import com.winterhavenmc.roadblock.core.util.PluginCtx;
import com.winterhavenmc.roadblock.core.ports.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.roadblock.core.util.MetricsHandler;
import com.winterhavenmc.roadblock.core.util.SimpleApi;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;


public class Bootsrap extends JavaPlugin
{
	private ConnectionProvider connectionProvider;
	private static SimpleApi simpleApi;


	@Override
	public void onEnable()
	{
		// install default config.yml if not present
		saveDefaultConfig();

		final MessageBuilder messageBuilder = MessageBuilder.create(this);
		final MaterialsProvider materials = new BukkitMaterialsProvider(this);
		this.connectionProvider = SqliteConnectionProvider.connect(this, materials);
		final BlockRepository blocks = connectionProvider.blocks();
		final HighlightManager highlightManager = new BukkitHighlightManager(this);

		final PluginCtx ctx = new PluginCtx(this, messageBuilder, materials, blocks, highlightManager);

		new BukkitCommandDispatcher(ctx);
		new BukkitBlockEventListener(ctx);
		new BukkitEntityEventListener(ctx);
		new MetricsHandler(ctx);

		Bootsrap.simpleApi = new SimpleApi(ctx);
	}


	@Override
	public void onDisable()
	{
		connectionProvider.close();
	}


	@SuppressWarnings("unused")
	public static SimpleApi simpleApi()
	{
		return Bootsrap.simpleApi;
	}

}
