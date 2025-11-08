/*
 * Copyright (c) 2025 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.roadblock.adapters.datastore;

import com.winterhavenmc.roadblock.core.util.LocalizedMessage;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public enum DatastoreMessage implements LocalizedMessage
{
	DATASTORE_INITIALIZED_NOTICE("SQLite datastore initialized."),
	DATASTORE_INITIALIZED_ERROR("The SQLite datastore is already initialized."),
	DATASTORE_CLOSE_ERROR("An error occurred while closing the SQLite datastore."),
	DATASTORE_CLOSED_NOTICE("The SQLite datastore connection was successfully closed."),

	SCHEMA_VERSION_ERROR("Could not read schema version."),
	SCHEMA_UPDATE_ERROR("An error occurred while trying to update the SQLite datastore schema."),
	SCHEMA_UP_TO_DATE_NOTICE("Current schema is up to date."),
	SCHEMA_BLOCK_RECORDS_MIGRATED_NOTICE("{0} block records migrated to schema v{1}"),

	CREATE_BLOCK_TABLE_ERROR("An error occurred while trying to create the Block table in the SQLite datastore."),
	CREATE_BLOCK_INVALID_WORLD_ERROR("Stored location has invalid world  ''{0}''. Skipping record."),
	CREATE_BLOCK_ERROR("A valid block location could not be created: {0}"),

	SELECT_ALL_BLOCKS_ERROR("An error occurred while trying to select all block records from the SQLite datastore."),
	SELECT_BLOCK_COUNT_ERROR("An error occurred while trying to get the block count from the SQLite datastore."),
	SELECT_BLOCKS_IN_CHUNK_ERROR("An error occurred while trying to select block records in a given chunk from the SQLite datastore."),
	SELECT_BLOCKS_BY_PROXIMITY_ERROR("An error occurred while trying to select block records by proximity from the SQLite datastore."),

	INSERT_BLOCK_ERROR("An error occurred while attempting to insert a block in the SQLite datastore."),

	DELETE_BLOCK_RECORD_ERROR("An error occurred while attempting to delete a block record from the SQLite datastore."),
	;

	private final String defaultMessage;


	DatastoreMessage(String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}

	@Override
	public String toString()
	{
		return defaultMessage;
	}


	@Override
	public String getLocalizedMessage(final Locale locale)
	{
		try
		{
			ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			return bundle.getString(name());
		}
		catch (MissingResourceException exception)
		{
			return this.defaultMessage;
		}
	}


	public String getLocalizedMessage(final Locale locale, final Object... objects)
	{
		try
		{
			final ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			String pattern = bundle.getString(name());
			return MessageFormat.format(pattern, objects);
		}
		catch (MissingResourceException exception)
		{
			return this.defaultMessage;
		}
	}

}
