package com.winterhaven_mc.roadblock.storage;

import com.winterhaven_mc.roadblock.PluginMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;


enum DataStoreType {

	SQLITE("SQLite") {
		@Override
		public DataStore create(PluginMain plugin) {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

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
	public abstract DataStore create(PluginMain plugin);

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


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	static void convertDataStore(final PluginMain plugin, final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {

			plugin.getLogger().info("Converting existing " + oldDataStore.getDisplayName() + " datastore to "
					+ newDataStore.getDisplayName() + " datastore...");

			// initialize old datastore if necessary
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				}
				catch (Exception e) {
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore.getDisplayName() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			// get set of all location records in old datastore
			Collection<BlockRecord> allRecords = new HashSet<>(oldDataStore.selectAllRecords());

			int count = newDataStore.insertRecords(allRecords);
			plugin.getLogger().info(count + " records converted to "
					+ newDataStore.getDisplayName() + " datastore.");

			newDataStore.sync();

			oldDataStore.close();
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the new datastore that all other existing datastores should be converted to
	 */
	static void convertAll(final PluginMain plugin, final DataStore newDataStore) {

		// get array list of all data store types
		final ArrayList<DataStoreType> dataStores =
				new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore.getType());

		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;

			if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}

			// add additional datastore types here as they become available

			if (oldDataStore != null) {
				convertDataStore(plugin, oldDataStore, newDataStore);
			}
		}
	}

}
