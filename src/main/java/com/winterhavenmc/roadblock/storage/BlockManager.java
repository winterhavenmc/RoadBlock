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

import com.winterhavenmc.roadblock.bootstrap.Bootstrap;
import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
import com.winterhavenmc.roadblock.ports.datastore.ConnectionProvider;
import com.winterhavenmc.roadblock.util.Config;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;


public final class BlockManager
{
	private final Plugin plugin;
	private final ConnectionProvider connectionProvider;
	private Set<Material> roadBlockMaterials;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public BlockManager(final PluginMain plugin)
	{
		this.plugin = plugin;
		this.connectionProvider = Bootstrap.getConnectionProvider(plugin);
		ConnectionProvider.connect(plugin, connectionProvider);
		this.roadBlockMaterials = updateMaterials();
	}


	/**
	 * Close data store
	 */
	public void close()
	{
		if (connectionProvider != null)
		{
			connectionProvider.close();
		}
	}


	/**
	 * reload data store
	 */
	public void reload()
	{
		// reload road block materials from config
		this.roadBlockMaterials = updateMaterials();
	}


	/**
	 * Create Set of all blocks of valid road block material attached to location
	 *
	 * @param startLocation location to begin searching for attached road blocks
	 * @return Set of Locations of attached road blocks
	 */
	public Set<Location> getFill(final Location startLocation)
	{
		if (startLocation == null) return Collections.emptySet();

		final Set<Location> returnSet = new HashSet<>();
		final Queue<Location> queue = new LinkedList<>();

		// put start location in queue
		queue.add(startLocation);
		while (!queue.isEmpty())
		{
			// remove location at head of queue
			Location loc = queue.poll();

			// if location is not in return set and is a road block material and is not too far from start...
			if (!returnSet.contains(loc) && roadBlockMaterials.contains(loc.getBlock().getType())
					&& loc.distanceSquared(startLocation) < Math.pow(Config.SPREAD_DISTANCE.getInt(plugin.getConfig()), 2))
			{
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
	 * Returns a Set of valid block locations from a Collection of Bukkit locations.
	 */
	public Set<BlockLocation.Valid> getBlockLocations(final Collection<Location> locations)
	{
		return locations.stream()
				.map(BlockLocation::of)
				.filter(BlockLocation.Valid.class::isInstance)
				.map(BlockLocation.Valid.class::cast)
				.collect(Collectors.toSet());
	}


	/**
	 * Check if block below player is a protected road block
	 *
	 * @param player the player to is above a road block
	 * @return {@code true} if player is within three blocks above a road block, else {@code false}
	 */
	public boolean isAboveRoad(final Player player)
	{
		// if player is null, return false
		if (player == null)
		{
			return false;
		}

		// get configured height above road
		final int distance = Config.ON_ROAD_HEIGHT.getInt(plugin.getConfig());

		// if distance is less than one, return false
		if (distance < 1)
		{
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
	public boolean isAboveRoad(final Location location, final int distance)
	{
		// if passed location is null, return false
		if (location == null)
		{
			return false;
		}

		// if passed distance is less than one, return false
		if (distance < 1)
		{
			return false;
		}

		boolean result = false;
		int checkDepth = distance;

		// iterate until maxDepth reached
		while (checkDepth > 0)
		{
			// get block at checkDepth
			Block testBlock = location.getBlock().getRelative(BlockFace.DOWN, checkDepth);

			// don't check datastore unless testBlock is road block material
			if (isRoadBlockMaterial(testBlock))
			{
				if (connectionProvider.blocks().isProtected(testBlock.getLocation()))
				{
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
	public boolean isRoadBlock(final Block block)
	{
		if (block == null) return false;

		// check if block is road block material
		if (!isRoadBlockMaterial(block))
		{
			return false;
		}

		// check if block is in cache or datastore
		return connectionProvider.blocks().isProtected(block.getLocation());
	}


	/**
	 * Check if block is a valid road block material
	 *
	 * @param block the block to test for valid configured road block material
	 * @return {@code true} if the block material is a configured road block material, {@code false} if it is not
	 */
	private boolean isRoadBlockMaterial(final Block block)
	{
		return block != null && roadBlockMaterials.contains(block.getType());
	}


	/**
	 * Check if block at location is a valid road block material
	 *
	 * @param location the location of a block to test for valid road block material
	 * @return {@code true} if the block at location is a configured road block material, {@code false} if it is not
	 */
	@SuppressWarnings("unused")
	private boolean isRoadBlockMaterial(final Location location)
	{
		return location != null && roadBlockMaterials.contains(location.getBlock().getType());
	}


	/**
	 * Check if a material is a valid road block material
	 *
	 * @param material the material type to test for valid road block material
	 * @return {@code true} if the material is a configured road block material, {@code false} if it is not
	 */
	public boolean isRoadBlockMaterial(final Material material)
	{
		return material != null && roadBlockMaterials.contains(material);
	}


	/**
	 * Insert block location records into datastore
	 *
	 * @param locations a Collection of Locations to be inserted into the datastore
	 */
	public int storeBlockLocations(final Collection<Location> locations)
	{
		return connectionProvider.blocks().save(getBlockLocations(locations));
	}


	/**
	 * Remove block locations from datastore
	 *
	 * @param locations a Collection of Locations to be deleted from the datastore
	 */
	public int removeBlockLocations(final Collection<Location> locations)
	{
		return connectionProvider.blocks().delete(getBlockLocations(locations));
	}


	/**
	 * Parse valid road block materials from config file
	 */
	public Set<Material> updateMaterials()
	{
		final Collection<String> materialStringList =
				new HashSet<>(Config.MATERIALS.getStringList(plugin.getConfig()));

		final HashSet<Material> returnSet = new HashSet<>();

		Material matchMaterial = null;

		for (String string : materialStringList)
		{
			// try to split on colon
			if (!string.isEmpty())
			{
				String[] materialElements = string.split("\\s*:\\s*");

				// try to match material
				if (materialElements.length > 0)
				{
					matchMaterial = Material.matchMaterial(materialElements[0]);
				}
			}

			// if matching material found, add to returnSet
			if (matchMaterial != null)
			{
				returnSet.add(matchMaterial);
			}
		}
		return returnSet;
	}


	public Set<Material> getRoadBlockMaterials()
	{
		return roadBlockMaterials;
	}


	synchronized public int getBlockTotal()
	{
		return connectionProvider.blocks().getTotalBlocks();
	}


	public Collection<Location> selectNearbyBlocks(final Location location, final int distance)
	{
		return connectionProvider.blocks().getNearbyBlocks(location, distance);
	}


}
