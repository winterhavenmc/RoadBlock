package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema.SqliteSchemaUpdater;
import com.winterhavenmc.roadblock.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


public class SQLiteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final LocaleProvider localeProvider;
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
		this.localeProvider = LocaleProvider.create(plugin);
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
			plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			return;
		}

		// register the driver
		Class.forName("org.sqlite.JDBC");

		// create database url
		final String dbUrl = "jdbc:sqlite" + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		blocks = new SQLiteBlockRepository(plugin, connection, localeProvider);

		// update database schema if necessary
		SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, localeProvider, blocks);
		schemaUpdater.update();

		// create tables if necessary
		createBlockTable(connection, localeProvider);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_NOTICE.getLocalizedMessage(localeProvider.getLocale()));
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
			plugin.getLogger().info(SqliteMessage.DATASTORE_CLOSED_NOTICE.getLocalizedMessage(localeProvider.getLocale()));
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning(SqliteMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(e.getMessage());
		}
		this.initialized = false;
	}


	@Override
	public BlockRepository blocks()
	{
		return blocks;
	}


	private void createBlockTable(final Connection connection, final LocaleProvider localeProvider)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateBlockTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_BLOCK_TABLE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}
