package com.winterhavenmc.roadblock.adapters.datastore.sqlite;

import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
import com.winterhavenmc.roadblock.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.ports.datastore.ConnectionProvider;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.Set;


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
			plugin.getLogger().info("SQLite datastore already initialized.");
			return;
		}

		// register the driver
		Class.forName("org.sqlite.JDBC");

		// create database url
		final String dbUrl = "jdbc:sqlite" + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		// update database schema if necessary
		updateSchema();

		blocks = new SQLiteBlockRepository(plugin, connection);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info("SQLite datastore initialized.");

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
			plugin.getLogger().info(this + " datastore connection closed.");
		}
		catch (Exception e)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while closing the " + this + " datastore.");
			plugin.getLogger().warning(e.getMessage());
		}
		this.initialized = false;
	}


	@Override
	public BlockRepository blocks()
	{
		return blocks;
	}


	int getSchemaVersion()
	{
		int version = -1;

		try
		{
			final Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			if (rs.next())
			{
				version = rs.getInt(1);
			}
		}
		catch (SQLException e)
		{
			plugin.getLogger().warning("Could not get schema version!");
		}
		return version;
	}


	void updateSchema() throws SQLException
	{
		int schemaVersion = getSchemaVersion();

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0)
		{
			int count;
			ResultSet rs = statement.executeQuery(Queries.getQuery("SelectBlockTable"));
			if (rs.next())
			{
				Set<BlockLocation.Valid> existingRecords = this.blocks().getAll();
				statement.executeUpdate(Queries.getQuery("DropBlockTable"));
				statement.executeUpdate(Queries.getQuery("DropChunkIndex"));
				statement.executeUpdate(Queries.getQuery("CreateBlockTable"));
				statement.executeUpdate(Queries.getQuery("CreateChunkIndex"));

				count = this.blocks().save(existingRecords);
				plugin.getLogger().info(count + " block records migrated to schema v1");
			}

			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = 1");
		}

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("CreateBlockTable"));

		// execute index creation statement
		statement.executeUpdate(Queries.getQuery("CreateChunkIndex"));
	}


}
