package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.adapters.resources.configuration.BukkitConfigRepository;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema.SqliteSchemaUpdater;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


public class SqliteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final ConfigRepository configRepository;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;
	private BlockRepository blocks;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public SqliteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.configRepository = BukkitConfigRepository.create(plugin);
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
			plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_ERROR.getLocalizedMessage(configRepository.locale()));
			return;
		}

		// register the driver
		Class.forName("org.sqlite.JDBC");

		// create database url
		final String dbUrl = "jdbc:sqlite" + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		blocks = new SqliteBlockRepository(plugin, connection, configRepository);

		// update database schema if necessary
		SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, configRepository, blocks);
		schemaUpdater.update();

		// create tables if necessary
		createBlockTable(connection, configRepository);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_NOTICE.getLocalizedMessage(configRepository.locale()));
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
			plugin.getLogger().info(SqliteMessage.DATASTORE_CLOSED_NOTICE.getLocalizedMessage(configRepository.locale()));
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning(SqliteMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(configRepository.locale()));
			plugin.getLogger().warning(e.getMessage());
		}
		this.initialized = false;
	}


	@Override
	public BlockRepository blocks()
	{
		return blocks;
	}


	private void createBlockTable(final Connection connection, final ConfigRepository configRepository)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateBlockTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_BLOCK_TABLE_ERROR.getLocalizedMessage(configRepository.locale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}
