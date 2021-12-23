package com.winterhaven_mc.roadblock.storage;

import com.winterhaven_mc.roadblock.PluginMain;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;


public interface DataStore {

	/**
	 * Initialize storage
	 *
	 * @throws Exception initialization failed
	 */
	void initialize() throws Exception;


	/**
	 * Check that block at location is protected
	 *
	 * @param location block location to check for RoadBlock protection
	 * @return {@code true} if block at location is protected, otherwise {@code false}
	 */
	boolean isProtected(final Location location);


	/**
	 * Store list of records
	 *
	 * @param blockRecords a {@code Collection} of {@code Location}
	 *                  for block locations to be inserted into the datastore
	 */
	@SuppressWarnings("UnusedReturnValue")
	int insertRecords(final Collection<BlockRecord> blockRecords);


	/**
	 * delete list of records
	 *
	 * @param blockRecords {@code Collection} of {@code Location}
	 *                    containing unique composite keys of records to delete
	 */
	@SuppressWarnings("UnusedReturnValue")
	int deleteRecords(final Collection<BlockRecord> blockRecords);


	/**
	 * get all records
	 *
	 * @return Set of {@code Location} for all block records
	 */
	Collection<BlockRecord> selectAllRecords();


	/**
	 * count records in blocks table
	 *
	 * @return numner of records in blocks table
	 */
	int getTotalBlocks();


	/**
	 * Get block records for locations within a chunk
	 *
	 * @param chunk the chunk containing records to be returned
	 * @return {@code Set} of {@code LocationRecords} for block records within the chunk
	 */
	Collection<BlockRecord> selectRecordsInChunk(final Chunk chunk);


	/**
	 * Get block records for locations within {@code distance} of {@code location}
	 *
	 * @param location origin location
	 * @param distance distance from origin to select blocks
	 * @return Set of Locations that are within {@code distance} of {@code location}
	 */
	Collection<Location> selectNearbyBlocks(final Location location, final int distance);


	/**
	 * Close storage
	 */
	void close();


	/**
	 * Sync datastore to disk if supported
	 */
	void sync();


	/**
	 * Delete datastore
	 */
	@SuppressWarnings("UnusedReturnValue")
	boolean delete();


	/**
	 * Check that datastore exists
	 *
	 * @return boolean
	 */
	boolean exists();


	/**
	 * Get datastore type
	 *
	 * @return DataStoreType of this datastore type
	 */
	DataStoreType getType();


	/**
	 * Get datastore name
	 *
	 * @return display name of datastore
	 */
	String getDisplayName();


	/**
	 * Get datastore initialized field
	 *
	 * @return boolean
	 */
	boolean isInitialized();


	/**
	 * Create new data store of configured type.<br>
	 * No parameter version used when no current datastore exists
	 *
	 * @return new datastore of configured type
	 */
	static DataStore create(PluginMain plugin) {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(plugin, dataStoreType, null);
	}


	/**
	 * Create new data store of configured type.<br>
	 * No parameter version used when no current datastore exists
	 *
	 * @param dataStoreType datastore type to create
	 * @return new datastore of configured type
	 */
	static DataStore create(PluginMain plugin, DataStoreType dataStoreType) {

		// if passed data store type is null, use default type
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(plugin, dataStoreType, null);
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param dataStoreType the datastore type to be created
	 * @param oldDataStore  the existing datastore to be converted to the new datastore
	 * @return instance of newly initialized datastore
	 */
	static DataStore create(final PluginMain plugin, final DataStoreType dataStoreType, final DataStore oldDataStore) {

		// get new data store of specified type
		final DataStore newDataStore = dataStoreType.create(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore.getDisplayName() + " datastore!");
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
			return null;
		}

		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convertDataStore(plugin, oldDataStore, newDataStore);
		}
		else {
			convertAll(plugin, newDataStore);
		}
		// return initialized data store
		return newDataStore;
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
