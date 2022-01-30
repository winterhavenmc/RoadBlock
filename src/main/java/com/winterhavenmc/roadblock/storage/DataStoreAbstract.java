/*
 * Copyright (c) 2022 Tim Savage.
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

package com.winterhavenmc.roadblock.storage;


abstract class DataStoreAbstract {

	private boolean initialized;

	DataStoreType type;


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
	@Override
	public String toString() {
		return this.getType().toString();
	}

}
