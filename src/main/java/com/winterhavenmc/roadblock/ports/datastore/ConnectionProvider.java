package com.winterhavenmc.roadblock.ports.datastore;

import org.bukkit.plugin.Plugin;
import java.sql.SQLException;


public interface ConnectionProvider
{
	static void connect(Plugin plugin, ConnectionProvider connectionProvider)
	{
		try
		{
			connectionProvider.connect();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}
	}


	/**
	 * Initialize datastore
	 */
	void connect() throws SQLException, ClassNotFoundException;


	/**
	 * Close SQLite datastore connection
	 */
	void close();


	BlockRepository blocks();
}
