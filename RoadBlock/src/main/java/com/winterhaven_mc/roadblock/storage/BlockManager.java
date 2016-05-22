package com.winterhaven_mc.roadblock.storage;

import com.winterhaven_mc.roadblock.PluginMain;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;


public final class BlockManager {

	// reference to main class
	private final PluginMain plugin;

	// set of road block materials
	private Set<Material> roadBlockMaterials;
		
	/**
	 * Class constructor
	 * @param plugin
	 */
	public BlockManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;
	
		// get road block materials from config file
		updateMaterials();
	}

	
	/**
	 * Create HashSet of all blocks of valid road block material attached to location
	 * @param startLocation
	 * @return
	 */
	public final Set<Location> getFill(final Location startLocation) {
		
		if (startLocation == null) {
			return Collections.emptySet();
		}
		
		// create HashSet for return values
		final Set<Location> returnSet = new HashSet<Location>();
		
		// create queue using linked list implementation
		final Queue<Location> queue = new LinkedList<Location>();
		
		// put start location in queue
		queue.add(startLocation);
		
		// loop until queue is empty
		while (!queue.isEmpty()) {

			// remove location at head of queue
			Location loc = queue.poll();
		
			// if location is not in return set and is a road block material and is not too far from start...
			if (!returnSet.contains(loc) && roadBlockMaterials.contains(loc.getBlock().getType())
					&& loc.distanceSquared(startLocation) < Math.pow(plugin.getConfig().getInt("spread-distance"),2)) {
				
				// add location to return set
				returnSet.add(loc);

				// add adjacent locations to queue
				queue.add(loc.clone().add(0,0,1));
				queue.add(loc.clone().add(0,0,-1));
				queue.add(loc.clone().add(1,0,0));
				queue.add(loc.clone().add(-1,0,0));
			}
		}			
		return returnSet;
	}
	

	/**
	 * Check if block below player is a protected road block, up to maxDepth
	 * @param player
	 * @return
	 */
	public final boolean isRoadBelowPlayer(final Player player) {
		
		if (player == null) {
			return false;
		}
		
		int depth = 0;
		final int maxDepth = 3;
	
		// convert player location to block location (with integer coordinates)
		Location testLocation = player.getLocation().getBlock().getLocation().clone();
		
		// iterate until maxDepth reached
		while (depth < maxDepth) {
			
			// don't check datastore unless block at location is road block material
			if (isRoadBlockMaterial(testLocation)) {
				if (plugin.dataStore.isProtected(testLocation)) {
					return true;
				}
			}
			// decrement test location height by one block
			testLocation.add(0,-1,0);
			depth++;
		}		
		return false;
	}


	/**
	 * Check if block below location is a protected road block, searching down to maxDepth
	 * @param location
	 * @param passedMaxDepth
	 * @return
	 */
	public final boolean isRoadBelow(final Location location, final int passedMaxDepth) {
		
		if (location == null) {
			return false;
		}
		
		int depth = 0;
		int maxDepth = passedMaxDepth;
		
		// if maxDepth passed is less than or equal to zero, default to 5
		if (maxDepth <= 0) {
			maxDepth = 5;
		}
		
		// don't let maxDepth go below bottom of world
		maxDepth = Math.min(maxDepth, location.getBlockY());
	
		// get copy of location as block location (with integer coordinates)
		final Location testLocation = location.getBlock().getLocation().clone();
		
		// iterate until maxDepth reached
		while (depth < maxDepth) {
			
			// don't check datastore unless block at location is road block material
			if (isRoadBlockMaterial(testLocation)) {
				if (plugin.dataStore.isProtected(testLocation)) {
					return true;
				}
			}
			// decrement test location height by one block
			testLocation.add(0,-1,0);
			depth++;
		}
		return false;
	}


	/**
	 * Check if block is a protected road block
	 * @param block
	 * @return
	 */
	public final boolean isRoadBlock(final Block block) {
		
		if (block == null) {
			return false;
		}
		
		// check if block is road block material
		if (!isRoadBlockMaterial(block)) {
			return false;
		}
		
		// check if block is in cache or datastore
		if (plugin.dataStore.isProtected(block.getLocation())) {
			return true;
		}
		return false;
	}
	

	/**
	 * Check if block is a valid road block material
	 * @param block
	 * @return
	 */
	final boolean isRoadBlockMaterial(final Block block) {
		
		if (block == null) {
			return false;
		}
		return roadBlockMaterials.contains(block.getType());
	}


	/**
	 * Check if block at location is a valid road block material
	 * @param location
	 * @return
	 */
	final boolean isRoadBlockMaterial(final Location location) {
		
		if (location == null) {
			return false;
		}
		return roadBlockMaterials.contains(location.getBlock().getType());
	}

	
	/**
	 * Check if a material is a valid road block material
	 * @param material
	 * @return
	 */
	public final boolean isRoadBlockMaterial(final Material material) {
		
		if (material == null) {
			return false;
		}
		return roadBlockMaterials.contains(material);
	}
	
	
	/**
	 * Put block locations in datastore
	 * @param locationSet
	 */
	public final void storeLocations(final HashSet<Location> locationSet) {
		plugin.dataStore.insertRecords(locationSet);
	}
	

	/**
	 * Remove block locations from datastore
	 * @param locationSet
	 */
	public final void removeLocations(final HashSet<Location> locationSet) {
		plugin.dataStore.deleteRecords(locationSet);
	}
	
	public final void removeLocation(final Location location) {
		plugin.dataStore.deleteRecord(location);
	}
	
	
	/**
	 * Parse valid road block materials from config file
	 * @return HashSet of materials
	 */
	public final void updateMaterials() {
		
		final ArrayList<String> materialStringList = 
				new ArrayList<String>(plugin.getConfig().getStringList("materials"));
		
		final HashSet<Material> returnSet = new HashSet<Material>();
		
		Material matchMaterial = null;
		
		for (String string : materialStringList) {
			
			// try to split on colon
			if (!string.isEmpty()) {
				String materialElements[] = string.split("\\s*:\\s*");
	
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

	public final Set<Material> getRoadBlockMaterials() {
		return roadBlockMaterials;
	}

}
