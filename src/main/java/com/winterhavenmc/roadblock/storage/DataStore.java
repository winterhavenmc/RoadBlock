/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.roadblock.storage;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


interface DataStore {

	/**
	 * Initialize storage
	 *
	 * @throws Exception initialization failed
	 */
	void initialize() throws Exception;


	/**
	 * Get datastore initialized field
	 *
	 * @return boolean
	 */
	boolean isInitialized();


	/**
	 * Get datastore type
	 *
	 * @return DataStoreType of this datastore type
	 */
	DataStoreType getType();


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
	 * Create new data store of configured type.<br>
	 * No parameter version used when no current datastore exists
	 *
	 * @return new datastore of configured type
	 */
	static DataStore connect(final JavaPlugin plugin) {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// get new data store of specified type
		final DataStore newDataStore = dataStoreType.connect(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore + " datastore!");
			plugin.getLogger().severe(e.getLocalizedMessage());
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// convert any existing data stores to new type
		DataStoreType.convertAll(plugin, newDataStore);

		// return initialized data store
		return newDataStore;
	}


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

}
