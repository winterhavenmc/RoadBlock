package com.winterhavenmc.roadblock;

import com.winterhavenmc.roadblock.commands.CommandManager;
import com.winterhavenmc.roadblock.highlights.HighlightManager;
import com.winterhavenmc.roadblock.listeners.EventListener;
import com.winterhavenmc.roadblock.messages.Macro;
import com.winterhavenmc.roadblock.messages.MessageId;
import com.winterhavenmc.roadblock.storage.BlockManager;

import com.winterhavenmc.util.messagebuilder.MessageBuilder;
import com.winterhavenmc.util.soundconfig.SoundConfiguration;
import com.winterhavenmc.util.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.util.worldmanager.WorldManager;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;


public final class PluginMain extends JavaPlugin {

	public MessageBuilder<MessageId, Macro> messageBuilder;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public BlockManager blockManager;
	public HighlightManager highlightManager;


	@Override
	public void onEnable() {

		// bStats
		new Metrics(this, 13919);

		// install default config.yml if not present
		saveDefaultConfig();

		// instantiate message builder
		messageBuilder = new MessageBuilder<>(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate block manager
		blockManager = new BlockManager(this);

		// instantiate highlight manager
		highlightManager = new HighlightManager(this);

		// instantiate command manager
		new CommandManager(this);

		// instantiate event listener
		new EventListener(this);
	}


	@Override
	public void onDisable() {

		// close datastore
		blockManager.close();
	}

}
