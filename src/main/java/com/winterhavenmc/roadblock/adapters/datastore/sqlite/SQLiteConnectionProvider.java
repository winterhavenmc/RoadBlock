package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema.SqliteSchemaUpdater;
import com.winterhavenmc.roadblock.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


public class SQLiteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;
	private BlockRepository blocks;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public SQLiteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.dataFilePath = plugin.getDataFolder() + File.separator + "roadblocks.db";
	}


	/**
	 * Initialize SQLite datastore
	 */
	@Override
	public void connect() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, do nothing and return
		if (initialized)
		{
			plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_ERROR.toString());
			return;
		}

		// register the driver
		Class.forName("org.sqlite.JDBC");

		// create database url
		final String dbUrl = "jdbc:sqlite" + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		blocks = new SQLiteBlockRepository(plugin, connection);

		// update database schema if necessary
		SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, blocks);
		schemaUpdater.update();

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_NOTICE.toString());
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public void close()
	{
		try
		{
			connection.close();
			plugin.getLogger().info(SqliteMessage.DATASTORE_CLOSED_NOTICE.toString());
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning(SqliteMessage.DATASTORE_CLOSE_ERROR.toString());
			plugin.getLogger().warning(e.getMessage());
		}
		this.initialized = false;
	}


	@Override
	public BlockRepository blocks()
	{
		return blocks;
	}

}
