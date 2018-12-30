package com.winterhaven_mc.roadblock.storage;

import com.winterhaven_mc.roadblock.PluginMain;


enum DataStoreType {

	SQLITE("SQLite") {
		@Override
		public DataStore create() {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	// static reference to main class
	private final static PluginMain plugin = PluginMain.instance;

	// datastore type formatted display name
	private final String displayName;

	// default datastore type
	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Class constructor
	 *
	 * @param displayName the display name of the DataStoreType
	 */
	DataStoreType(final String displayName) {
		this.displayName = displayName;
	}


	/**
	 * Create instance of a DataStore
	 *
	 * @return instance of a DataStore
	 */
	public abstract DataStore create();

	@Override
	public final String toString() {
		return displayName;
	}


	public static DataStoreType getDefaultType() {
		return defaultType;
	}


	/**
	 * Match datastore type from passed string; ignores case
	 *
	 * @param name A string to match to a DataStoreType
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
