package com.winterhaven_mc.roadblock;

import org.bukkit.plugin.java.JavaPlugin;


public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;
	DataStore dataStore;
	public BlockManager blockManager;
	MessageManager messageManager;
	CommandManager commandManager;
	
	Boolean debug = getConfig().getBoolean("debug");
	Boolean profile = getConfig().getBoolean("profile");
	

	@Override
	public void onEnable() {
		
		// set static reference to main class
		instance = this;
		
		// install default config.yml if not present  
		saveDefaultConfig();
		
		// instantiate message manager
		messageManager = new MessageManager(this);

		// instantiate command manager
		commandManager = new CommandManager(this);

		// get initialized destination storage object
		dataStore = DataStoreFactory.create();
		
		// instantiate event listener
		new EventListener(this);

		// instantiate block manager
		blockManager = new BlockManager(this);
		
	}
	
	@Override
	public void onDisable() {
		
		// close datastore
		dataStore.close();
	}

}
