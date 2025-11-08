package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.library.messagebuilder.adapters.resources.configuration.BukkitConfigRepository;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import com.winterhavenmc.roadblock.adapters.datastore.DatastoreMessage;
import com.winterhavenmc.roadblock.adapters.datastore.sqlite.schema.SqliteSchemaUpdater;
import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


public class SqliteConnectionProvider implements ConnectionProvider
{
	public static final String DATASTORE_NAME = "SQLite";
	private final Plugin plugin;
	private BlockRepository blocks;
	private final MaterialsProvider materials;
	private final ConfigRepository configRepository;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	private SqliteConnectionProvider(final Plugin plugin, final MaterialsProvider materials)
	{
		this.plugin = plugin;
		this.materials = materials;
		this.configRepository = BukkitConfigRepository.create(plugin);
		this.dataFilePath = plugin.getDataFolder() + File.separator + "roadblocks.db";
	}


	public static ConnectionProvider connect(final Plugin plugin, final MaterialsProvider materials)
	{
		ConnectionProvider connectionProvider = new SqliteConnectionProvider(plugin, materials);
		connectionProvider.connect();

		return connectionProvider;

//		try
//		{
//			connectionProvider.connect();
//		}
//		catch (Exception exception)
//		{
//			plugin.getLogger().severe("Could not initialize datastore!");
//			plugin.getLogger().severe(exception.getLocalizedMessage());
//		}
	}


	/**
	 * Initialize SQLite datastore
	 */
	@Override
	public void connect()
	{
		// if data store is already initialized, do nothing and return
		if (initialized)
		{
			plugin.getLogger().info(DatastoreMessage.DATASTORE_INITIALIZED_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			return;
		}

		try
		{
			// register the driver
			Class.forName("org.sqlite.JDBC");

			// create database url
			final String dbUrl = "jdbc:sqlite" + ":" + dataFilePath;

			// create a database connection
			connection = DriverManager.getConnection(dbUrl);

			blocks = new SqliteBlockRepository(plugin, connection, configRepository, materials);

			// update database schema if necessary
			SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, configRepository, blocks);
			schemaUpdater.update();

			// create tables if necessary
			createBlockTable(connection, configRepository);
		}
		catch (ClassNotFoundException classNotFoundException)
		{
			throw new RuntimeException("class not found exception");
		}
		catch (SQLException sqlException)
		{
			throw new RuntimeException("sql exception");
		}

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(DatastoreMessage.DATASTORE_INITIALIZED_NOTICE.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
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
			plugin.getLogger().info(DatastoreMessage.DATASTORE_CLOSED_NOTICE.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning(DatastoreMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
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
			plugin.getLogger().warning(DatastoreMessage.CREATE_BLOCK_TABLE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}
