package com.winterhaven_mc.roadblock;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;


public abstract class DataStore {

	protected boolean initialized;
	
	protected DataStoreType type;

	protected String filename;

	/**
	 * Initialize storage
	 * @throws Exception
	 */
	abstract void initialize() throws Exception;
	
	/**
	 * Check that block at location is protected
	 * @param testLocation
	 * @return
	 */
	abstract boolean isProtected(final Location location);

	/**
	 * Store list of records
	 * @param locations
	 */
	abstract void insertRecords(final Collection<Location> locations);

	/**
	 * Store record
	 * @param location
	 */
	abstract void insertRecord(final Location location);

	/**
	 * delete list of records
	 * @param locationSet
	 */
	abstract void deleteRecords(final Collection<Location> locationSet);

	/**
	 * Delete record
	 * @param block
	 * @return 
	 */	
	abstract void deleteRecord(final Location location);

	/**
	 * get all records
	 * @return List of blocks
	 */
	abstract Set<Location> selectAllRecords();

	/**
	 * Get block records for locations within a chunk
	 * @param chunk
	 * @return
	 */
	abstract Set<Location> selectBlockLocationsInChunk(Chunk chunk);
	
	/**
	 * Close storage
	 */
	abstract void close();

	/**
	 * Sync datastore to disk if supported
	 */
	abstract void sync();
	
	/**
	 * Delete datastore
	 */
	abstract void delete();
	
	/**
	 * Check that datastore exists
	 * @return boolean
	 */
	abstract boolean exists();
	
	/**
	 * Get datastore filename or equivalent
	 * @return
	 */
	String getFilename() {
		return this.filename;
	}

	/**
	 * Get datastore type
	 */
	DataStoreType getType() {
		return this.type;
	}
	
	/**
	 * Get datastore name
	 * @return
	 */
	String getName() {
		return this.getType().toString();
	}

	/**
	 * Get datastore initialized field
	 * @return boolean
	 */
	boolean isInitialized() {
		return this.initialized;
	}
	
	/**
	 * Set initialized field
	 * @param initialized
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

	
}
