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

package com.winterhavenmc.roadblock.core.ports.datastore;

import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.models.blocklocation.BlockLocation;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;


public interface BlockRepository
{
	/**
	 * get all records
	 *
	 * @return Set of {@code Location} for all block records
	 */
	Set<BlockLocation.Valid> getAll();


	/**
	 * Store list of records
	 *
	 * @param blockRecords a {@code Set} of {@code Location} for block locations to be inserted into the datastore
	 */
	int save(final Set<BlockLocation.Valid> blockRecords);


	/**
	 * delete list of records
	 *
	 * @param blockLocations {@code Set} of {@code Location} containing unique composite keys of records to delete
	 */
	int delete(final Set<BlockLocation.Valid> blockLocations);


	/**
	 * count records in blocks table
	 *
	 * @return number of records in blocks table
	 */
	int getTotalBlocks();


	/**
	 * Get block records for locations within a chunk
	 *
	 * @param chunk the chunk containing records to be returned
	 * @return {@code Set} of {@code LocationRecords} for block records within the chunk
	 */
	Collection<BlockLocation.Valid> getBlocksInChunk(final Chunk chunk);


	/**
	 * Get block records for locations within {@code distance} of {@code location}
	 *
	 * @param location origin location
	 * @param distance distance from origin to select blocks
	 * @return Set of Locations that are within {@code distance} of {@code location}
	 */
	Collection<Location> getNearbyBlocks(final Location location, final int distance);

	boolean isChunkCached(Location location);

	void flushCache(Chunk chunk);

	boolean isProtected(Location location);

	int removeBlockLocations(Collection<Location> locations);

	int storeBlockLocations(Collection<Location> locations);

	Set<Location> getFill(Location startLocation, MaterialsProvider materialsProvider);

	boolean isAboveRoad(Player player);

	boolean isAboveRoad(Location location, int distance);

	boolean isRoadBlock(Block block);
}
