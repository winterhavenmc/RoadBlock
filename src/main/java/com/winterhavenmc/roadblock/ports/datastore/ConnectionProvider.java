package com.winterhavenmc.roadblock.ports.datastore;

import org.bukkit.plugin.Plugin;
import java.sql.SQLException;


public interface ConnectionProvider
{
	/**
	 * Create new data store of configured type.<br>
	 * No parameter version used when no current datastore exists
	 */
	static void initialize(Plugin plugin, ConnectionProvider connectionProvider)
	{
		try
		{
			connectionProvider.initialize();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize SQLite datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}
	}


	/**
	 * Initialize SQLite datastore
	 */
	void initialize() throws SQLException, ClassNotFoundException;


	/**
	 * Close SQLite datastore connection
	 */
	void close();


	BlockRepository blocks();
}
