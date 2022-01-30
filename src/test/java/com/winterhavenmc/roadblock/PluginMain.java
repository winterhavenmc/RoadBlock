package com.winterhavenmc.roadblock;

import com.winterhavenmc.roadblock.commands.CommandManager;
import com.winterhavenmc.roadblock.highlights.HighlightManager;
import com.winterhavenmc.roadblock.listeners.BlockEventListener;
import com.winterhavenmc.roadblock.messages.Macro;
import com.winterhavenmc.roadblock.messages.MessageId;
import com.winterhavenmc.roadblock.storage.BlockManager;
import com.winterhavenmc.util.messagebuilder.MessageBuilder;
import com.winterhavenmc.util.soundconfig.SoundConfiguration;
import com.winterhavenmc.util.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.util.worldmanager.WorldManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;


public final class PluginMain extends JavaPlugin {

	public MessageBuilder<MessageId, Macro> messageBuilder;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public BlockManager blockManager;
	public HighlightManager highlightManager;


	/**
	 * Class constructor for testing
	 */
	@SuppressWarnings("unused")
	public PluginMain() {
		super();
	}


	/**
	 * Class constructor for testing
	 */
	@SuppressWarnings("unused")
	PluginMain(final JavaPluginLoader loader,
	           final PluginDescriptionFile descriptionFile,
	           final File dataFolder,
	           final File file) {
		super(loader, descriptionFile, dataFolder, file);
	}


	@Override
	public void onEnable() {

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
		new BlockEventListener(this);
	}


	@Override
	public void onDisable() {

		// close datastore
		blockManager.close();
	}

}
