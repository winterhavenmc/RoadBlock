package com.winterhavenmc.roadblock.core.ports.datastore;


public interface ConnectionProvider
{
	/**
	 * Initialize datastore
	 */
	void connect();


	/**
	 * Close SQLite datastore connection
	 */
	void close();


	BlockRepository blocks();
}
