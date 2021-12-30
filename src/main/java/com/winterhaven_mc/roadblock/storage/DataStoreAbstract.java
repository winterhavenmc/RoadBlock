package com.winterhaven_mc.roadblock.storage;


public abstract class DataStoreAbstract {

	private boolean initialized;

	DataStoreType type;

	String filename;


	/**
	 * Get datastore filename or equivalent
	 *
	 * @return filename of this datastore type
	 */
	public String getFilename() {
		return this.filename;
	}


	/**
	 * Get datastore type
	 *
	 * @return DataStoreType of this datastore type
	 */
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Get datastore name
	 *
	 * @return display name of datastore
	 */
	public String getDisplayName() {
		return this.getType().toString();
	}


	/**
	 * Get datastore name
	 *
	 * @return display name of datastore
	 */
	@Override
	public String toString() {
		return this.getType().toString();
	}


	/**
	 * Get datastore initialized field
	 *
	 * @return boolean
	 */
	public boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set initialized field
	 *
	 * @param initialized boolean value to set field
	 */
	public void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

}
