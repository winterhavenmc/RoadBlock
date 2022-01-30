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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public final class BlockManager {

	// reference to main class
	private final JavaPlugin plugin;

	// set of road block materials
	private Set<Material> roadBlockMaterials;

	// data store
	DataStore dataStore;

	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public BlockManager(final JavaPlugin plugin) {

		// set reference to main class
		this.plugin = plugin;

		// get road block materials from config file
		updateMaterials();

		// create data store using configured type
		dataStore = DataStore.connect(plugin);
	}


	/**
	 * Close data store
	 */
	public void close() {
		if (dataStore != null) {
			dataStore.close();
		}
	}


	/**
	 * reload data store
	 */
	public void reload() {

		// reload road block materials from config
		updateMaterials();

		// get current datastore type
		final DataStoreType currentType = dataStore.getType();

		// get configured datastore type
		final DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			dataStore = DataStore.connect(plugin);
		}
	}


	/**
	 * Create Set of all blocks of valid road block material attached to location
	 *
	 * @param startLocation location to begin searching for attached road blocks
	 * @return Set of Locations of attached road blocks
	 */
	public Set<Location> getFill(final Location startLocation) {

		// if passed location is null, return empty set
		if (startLocation == null) {
			return Collections.emptySet();
		}

		// create HashSet for return values
		final Set<Location> returnSet = new HashSet<>();

		// create queue using linked list implementation
		final Queue<Location> queue = new LinkedList<>();

		// put start location in queue
		queue.add(startLocation);

		// loop until queue is empty
		while (!queue.isEmpty()) {

			// remove location at head of queue
			Location loc = queue.poll();

			// if location is not in return set and is a road block material and is not too far from start...
			if (!returnSet.contains(loc) && roadBlockMaterials.contains(loc.getBlock().getType())
					&& loc.distanceSquared(startLocation) < Math.pow(plugin.getConfig().getInt("spread-distance"), 2)) {

				// add location to return set
				returnSet.add(loc);

				// add adjacent locations to queue
				queue.add(loc.clone().add(0, 0, 1));
				queue.add(loc.clone().add(0, 0, -1));
				queue.add(loc.clone().add(1, 0, 0));
				queue.add(loc.clone().add(-1, 0, 0));
			}
		}
		return returnSet;
	}


	/**
	 * Check if block below player is a protected road block
	 *
	 * @param player the player to is above a road block
	 * @return {@code true} if player is within three blocks above a road block, else {@code false}
	 */
	public boolean isAboveRoad(final Player player) {

		// if player is null, return false
		if (player == null) {
			return false;
		}

		// get configured height above road
		final int distance = plugin.getConfig().getInt("on-road-height");

		// if distance is less than one, return false
		if (distance < 1) {
			return false;
		}

		// return result of isAboveRoad for player location and configured height
		return isAboveRoad(player.getLocation(), distance);
	}


	/**
	 * Check if block below location is a protected road block, searching down to maxDepth
	 *
	 * @param location the location to test if above a road block
	 * @param distance the distance in blocks to test below location for road blocks
	 * @return {@code true} if location is above a road block, else {@code false}
	 */
	public boolean isAboveRoad(final Location location, final int distance) {

		// if passed location is null, return false
		if (location == null) {
			return false;
		}

		// if passed distance is less than one, return false
		if (distance < 1) {
			return false;
		}

		boolean result = false;
		int checkDepth = distance;

		// iterate until maxDepth reached
		while (checkDepth > 0) {

			// get block at checkDepth
			Block testBlock = location.getBlock().getRelative(BlockFace.DOWN, checkDepth);

			// don't check datastore unless testBlock is road block material
			if (isRoadBlockMaterial(testBlock)) {
				if (dataStore.isProtected(testBlock.getLocation())) {
					result = true;
					break;
				}
			}

			// decrement checkDepth
			checkDepth--;
		}
		return result;
	}


	/**
	 * Check if block is a protected road block
	 *
	 * @param block the block to test
	 * @return {@code true} if the block is a protected road block, else {@code false}
	 */
	public boolean isRoadBlock(final Block block) {

		// if passed block is null, return false
		if (block == null) {
			return false;
		}

		// check if block is road block material
		if (!isRoadBlockMaterial(block)) {
			return false;
		}

		// check if block is in cache or datastore
		return dataStore.isProtected(block.getLocation());
	}


	/**
	 * Check if block is a valid road block material
	 *
	 * @param block the block to test for valid configured road block material
	 * @return {@code true} if the block material is a configured road block material, {@code false} if it is not
	 */
	private boolean isRoadBlockMaterial(final Block block) {
		return block != null && roadBlockMaterials.contains(block.getType());
	}


	/**
	 * Check if block at location is a valid road block material
	 *
	 * @param location the location of a block to test for valid road block material
	 * @return {@code true} if the block at location is a configured road block material, {@code false} if it is not
	 */
	@SuppressWarnings("unused")
	private boolean isRoadBlockMaterial(final Location location) {
		return location != null && roadBlockMaterials.contains(location.getBlock().getType());
	}


	/**
	 * Check if a material is a valid road block material
	 *
	 * @param material the material type to test for valid configured road block material
	 * @return {@code true} if the material is a valid configured road block material, {@code false} if it is not
	 */
	public boolean isRoadBlockMaterial(final Material material) {
		return material != null && roadBlockMaterials.contains(material);
	}


	/**
	 * Insert block location records into datastore
	 *
	 * @param blockRecords a Collection of Locations to be inserted into the datastore
	 */
	public void storeLocations(final Collection<BlockRecord> blockRecords) {
		dataStore.insertRecords(blockRecords);
	}


	/**
	 * Remove block locations from datastore
	 *
	 * @param blockRecords a Collection of Locations to be deleted from the datastore
	 */
	public void removeLocations(final Collection<BlockRecord> blockRecords) {
		dataStore.deleteRecords(blockRecords);
	}


	/**
	 * Remove a block location from datastore
	 *
	 * @param blockRecord the location to be removed from the datastore
	 */
	public void removeLocation(final BlockRecord blockRecord) {
		Set<BlockRecord> blockRecords = new HashSet<>();
		blockRecords.add(blockRecord);
		dataStore.deleteRecords(blockRecords);
	}


	/**
	 * Parse valid road block materials from config file
	 */
	public void updateMaterials() {

		final Collection<String> materialStringList =
				new HashSet<>(plugin.getConfig().getStringList("materials"));

		final HashSet<Material> returnSet = new HashSet<>();

		Material matchMaterial = null;

		for (String string : materialStringList) {

			// try to split on colon
			if (!string.isEmpty()) {
				String[] materialElements = string.split("\\s*:\\s*");

				// try to match material
				if (materialElements.length > 0) {
					matchMaterial = Material.matchMaterial(materialElements[0]);
				}
			}

			// if matching material found, add to returnSet
			if (matchMaterial != null) {
				returnSet.add(matchMaterial);
			}
		}
		this.roadBlockMaterials = returnSet;
	}


	synchronized public int getBlockTotal() {
		return dataStore.getTotalBlocks();
	}


	public Collection<Location> selectNearbyBlocks(final Location location, final int distance) {
		return dataStore.selectNearbyBlocks(location, distance);
	}


	public Set<Material> getRoadBlockMaterials() {
		return roadBlockMaterials;
	}

}
