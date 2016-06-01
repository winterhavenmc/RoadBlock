package com.winterhaven_mc.roadblock;

import com.winterhaven_mc.roadblock.commands.CommandManager;
import com.winterhaven_mc.roadblock.highlights.HighlightManager;
import com.winterhaven_mc.roadblock.listeners.EventListener;
import com.winterhaven_mc.roadblock.storage.BlockManager;
import com.winterhaven_mc.roadblock.storage.DataStore;
import com.winterhaven_mc.roadblock.storage.DataStoreFactory;
import com.winterhaven_mc.roadblock.utilities.MessageManager;
import com.winterhaven_mc.util.SoundManager;
import com.winterhaven_mc.util.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;
	public BlockManager blockManager;
	public HighlightManager highlightManager;
	public MessageManager messageManager;
	public DataStore dataStore;
	public WorldManager worldManager;
	public SoundManager soundManager;
	
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
		
		// instantiate message manager
		messageManager = new MessageManager(this);

		// instantiate sound manager
		soundManager = new SoundManager(this);

		// get initialized destination storage object
		dataStore = DataStoreFactory.create();
		
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
		dataStore.close();
	}
	
	public final DataStore getDataStore() {
		return this.dataStore;
	}

	public final void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
}
