package com.winterhaven_mc.roadblock;

import com.winterhaven_mc.roadblock.commands.CommandManager;
import com.winterhaven_mc.roadblock.highlights.HighlightManager;
import com.winterhaven_mc.roadblock.listeners.EventListener;
import com.winterhaven_mc.roadblock.storage.BlockManager;

import com.winterhaven_mc.util.*;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;


public final class PluginMain extends JavaPlugin {

	public LanguageHandler languageHandler;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public BlockManager blockManager;
	public HighlightManager highlightManager;

//	public Boolean debug = getConfig().getBoolean("debug");
	public Boolean profile = getConfig().getBoolean("profile");


	/**
	 * Class constructor for testing
	 */
	public PluginMain() {
		super();
	}


	/**
	 * Class constructor for testing
	 */
	@SuppressWarnings("unused")
	private PluginMain(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
		super(loader, descriptionFile, dataFolder, file);
	}


	@Override
	public void onEnable() {

		// install default config.yml if not present
		saveDefaultConfig();

		// instantiate language manager
		languageHandler = new LanguageHandler(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

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
