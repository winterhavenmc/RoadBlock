package com.winterhaven_mc.roadblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;


public class DataStoreFactory {

	static PluginMain plugin = PluginMain.instance;


	/**
	 * Create new data store of configured type.<br>
	 * No parameter version used when no current datastore exists
	 * @return new datastore of configured type
	 */
	static DataStore create() {
		
		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.SQLITE;
		}
		return create(dataStoreType, null);
	}
	
	/**
	 * Create new data store of given type.<br>
	 * Single parameter version used when no current datastore exists
	 * @param dataStoreType
	 * @return
	 */
	static DataStore create(DataStoreType dataStoreType) {
		return create(dataStoreType, null);
	}
	
	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 * @param dataStoreType
	 * @param oldDataStore
	 * @return
	 */
	static DataStore create(DataStoreType dataStoreType, DataStore oldDataStore) {
	
		DataStore newDataStore = null;
		
		// get initialized sqlite data store
		newDataStore = createSqlite();
		
		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convertDataStore(oldDataStore, newDataStore);
		}
		else {
			convertAll(newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}
	

	/**
	 * try to create new sqlite data store; disable plugin on failure
	 * @return
	 */
	static DataStore createSqlite() {

		// create new sqlite datastore object
		DataStore newDataStore = new DataStoreSQLite(plugin);

		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			// error initializing sqlite datastore, so try yaml as fallback
			plugin.getLogger().warning("An error occurred while trying to initialize the sqlite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
			plugin.getLogger().warning("Disabling plugin...");
			plugin.getPluginLoader().disablePlugin(plugin);
		}
		return newDataStore;
	}

	/**
	 * convert old data store to new data store
	 * @param oldDataStore
	 * @param newDataStore
	 */
	static void convertDataStore(DataStore oldDataStore, DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}
		
		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {
			
			plugin.getLogger().info("Converting existing " + oldDataStore.getName() + " datastore to "
					+ newDataStore.getName() + " datastore...");
			
			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not initialize " 
							+ oldDataStore.getName() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}
			
			List<Location> allRecords = new ArrayList<Location>();
			
			allRecords = oldDataStore.getAllRecords();
			
			int count = 0;
			for (Location record : allRecords) {
				newDataStore.insertRecord(record);
				count++;
			}
			plugin.getLogger().info(count + " records converted to " + newDataStore.getName() + " datastore.");
			
			newDataStore.sync();
			
			oldDataStore.close();
			oldDataStore.delete();
		}
	}

	
	/**
	 * convert all existing data stores to new data store
	 * @param newDataStore
	 */
	static void convertAll(DataStore newDataStore) {
		
		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<DataStoreType>(Arrays.asList(DataStoreType.values()));
		
		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore);
		
		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;
			
			if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}
			
			// add additional datastore types here as they become available
			
			if (oldDataStore != null) {
				convertDataStore(oldDataStore, newDataStore);
			}
		}
	}
	
	
	static void reload() {
		
		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();
		
		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
				
		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {
			
			// create new datastore
			plugin.dataStore = create(newType,plugin.dataStore);
		}
		
	}
}
