package com.winterhaven_mc.roadblock;

import java.util.HashSet;
import java.util.List;

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
	abstract boolean isProtected(Location location);

	/**
	 * Store list of records
	 * @param locations
	 */
	abstract void insertRecords(final HashSet<Location> locations);

	/**
	 * Store record
	 * @param location
	 */
	abstract void insertRecord(Location location);

	/**
	 * delete list of records
	 * @param locationSet
	 */
	abstract void deleteRecords(HashSet<Location> locationSet);

	/**
	 * Delete record
	 * @param block
	 * @return 
	 */	
	abstract void deleteRecord(Location location);

	/**
	 * get all records
	 * @return List of blocks
	 */
	abstract List<Location> getAllRecords();

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
		return this.getType().getName();
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
	void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}
