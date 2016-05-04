package com.winterhaven_mc.roadblock.storage;

import com.winterhaven_mc.roadblock.PluginMain;

public enum DataStoreType {

	SQLITE("SQLite") {
		
		public DataStore create() {
		
			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	// static reference to main class
	private final static PluginMain plugin = PluginMain.instance;
	
	// datastore type formatted display name
	private String displayName;

	// default datastore type
	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Class constructor
	 * @param name
	 */
	private DataStoreType(final String displayName) {
		this.displayName = displayName;
	}
	

	/**
	 * Create instance of a DataStore
	 * @return instance of a DataStore
	 */
	public abstract DataStore create();
	
	@Override
	public String toString() {
		return displayName;
	}

	public static DataStoreType getDefaultType() {
		return defaultType;
	}
	
	/**
	 * Match datastore type from passed name
	 * @param name
	 * @return matching DataStoreType or default type if no match
	 */
	public static DataStoreType match(final String name) {
		for (DataStoreType type : DataStoreType.values()) {
			if (type.toString().equalsIgnoreCase(name)) {
				return type;
			}
		}
		// no match; return default type
		return defaultType;
	}
}
