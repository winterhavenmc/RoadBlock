package com.winterhaven_mc.roadblock;

import com.winterhaven_mc.roadblock.commands.CommandManager;
import com.winterhaven_mc.roadblock.highlights.HighlightManager;
import com.winterhaven_mc.roadblock.listeners.EventListener;
import com.winterhaven_mc.roadblock.storage.BlockManager;

import com.winterhaven_mc.util.*;

import org.bukkit.plugin.java.JavaPlugin;


public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;
	public BlockManager blockManager;
	public HighlightManager highlightManager;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;

	public Boolean debug = getConfig().getBoolean("debug");
	public Boolean profile = getConfig().getBoolean("profile");


	@Override
	public void onEnable() {

		// set static reference to main class
		instance = this;

		// install default config.yml if not present  
		saveDefaultConfig();

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate block manager
		blockManager = new BlockManager(this);

		// instantiate highlight manager
		highlightManager = new HighlightManager(this);

		// force loading of language file at startup
		LanguageManager.getInstance();

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
