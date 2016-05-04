package com.winterhaven_mc.roadblock;

import org.bukkit.plugin.java.JavaPlugin;

import com.winterhaven_mc.roadblock.highlights.HighlightManager;
import com.winterhaven_mc.roadblock.storage.BlockManager;
import com.winterhaven_mc.roadblock.storage.DataStore;
import com.winterhaven_mc.roadblock.storage.DataStoreFactory;
import com.winterhaven_mc.roadblock.utilities.MessageManager;


public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;
	public BlockManager blockManager;
	public HighlightManager highlightManager;
	public MessageManager messageManager;
	CommandManager commandManager;
	public DataStore dataStore;
	
	public Boolean debug = getConfig().getBoolean("debug");
	public Boolean profile = getConfig().getBoolean("profile");
	

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
		
		// instantiate highlight manager
		highlightManager = new HighlightManager(this);	
	}
	
	@Override
	public void onDisable() {
		
		// close datastore
		dataStore.close();
	}
	
	public DataStore getDataStore() {
		return this.dataStore;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
}
