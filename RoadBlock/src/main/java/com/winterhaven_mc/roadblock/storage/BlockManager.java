package com.winterhaven_mc.roadblock.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.winterhaven_mc.roadblock.PluginMain;

public class BlockManager {

	// reference to main class
	private final PluginMain plugin;

	// list of enabled world names
	private List<String> enabledWorlds;
	
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
		
		// get enabled worlds from config file
		updateEnabledWorlds();	
	}

	
	/**
	 * Create HashSet of all blocks of valid road block material attached to location
	 * @param startLocation
	 * @return
	 */
	public Set<Location> getFill(final Location startLocation) {
		
		if (startLocation == null) {
			return Collections.emptySet();
		}
		
		// create HashSet for return values
		Set<Location> returnSet = new HashSet<Location>();
		
		// create queue using linked list implementation
		Queue<Location> queue = new LinkedList<Location>();
		
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
	public boolean isRoadBelowPlayer(final Player player) {
		
		if (player == null) {
			return false;
		}
		
		int depth = 0;
		int maxDepth = 3;
	
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
	 * @param maxDepth
	 * @return
	 */
	public boolean isRoadBelow(final Location location, final int passedMaxDepth) {
		
		if (location == null) {
			return false;
		}
		
		int depth = 0;
		int maxDepth = passedMaxDepth;
		
		// if maxDepth passed is less than one, default to 5
		if (maxDepth <= 0) {
			maxDepth = 5;
		}
		
		// don't let maxDepth go below bottom of world
		maxDepth = Math.min(maxDepth, location.getBlockY());
	
		// get copy of location as block location (with integer coordinates)
		Location testLocation = location.getBlock().getLocation().clone();
		
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
	public boolean isRoadBlock(final Block block) {
		
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
	boolean isRoadBlockMaterial(final Block block) {
		
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
	boolean isRoadBlockMaterial(final Location location) {
		
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
	public boolean isRoadBlockMaterial(final Material material) {
		
		if (material == null) {
			return false;
		}
		return roadBlockMaterials.contains(material);
	}
	
	
	/**
	 * Put block locations in datastore
	 * @param locationSet
	 */
	public void storeLocations(final HashSet<Location> locationSet) {
		plugin.dataStore.insertRecords(locationSet);
	}
	

	/**
	 * Remove block locations from datastore
	 * @param locationSet
	 */
	public void removeLocations(final HashSet<Location> locationSet) {
		plugin.dataStore.deleteRecords(locationSet);
	}
	
	public void removeLocation(final Location location) {
		plugin.dataStore.deleteRecord(location);
	}
	
	/**
	 * get list of enabled worlds
	 * @return ArrayList of String enabledWorlds
	 */
	public List<String> getEnabledWorlds() {
		return this.enabledWorlds;
	}

	public boolean isWorldEnabled(World world) {
		
		if (world == null) {
			return false;
		}
		
		return this.getEnabledWorlds().contains(world.getName());
	}

	
	/**
	 * update enabledWorlds ArrayList field from config file settings
	 */
	public void updateEnabledWorlds() {
	
		// copy list of enabled worlds from config into enabledWorlds ArrayList field
		this.enabledWorlds = new ArrayList<String>(plugin.getConfig().getStringList("enabled-worlds"));
	
		// if enabledWorlds ArrayList is empty, add all worlds to ArrayList
		if (this.enabledWorlds.isEmpty()) {
			for (World world : plugin.getServer().getWorlds()) {
				enabledWorlds.add(world.getName());
			}
		}
	
		// remove each disabled world from enabled worlds field
		for (String disabledWorld : plugin.getConfig().getStringList("disabled-worlds")) {
			this.enabledWorlds.remove(disabledWorld);
		}
	}

	
	/**
	 * Parse valid road block materials from config file
	 * @return HashSet of materials
	 */
	public void updateMaterials() {
		
		ArrayList<String> materialStringList = new ArrayList<String>(plugin.getConfig().getStringList("materials"));
		
		HashSet<Material> returnSet = new HashSet<Material>();
		
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

	public Set<Material> getRoadBlockMaterials() {
		return roadBlockMaterials;
	}

}
