package com.winterhaven_mc.roadblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockManager {

	// reference to main class
	private final PluginMain plugin;

	// HashMap of highlighted block locations for players
	private Map<UUID,HashSet<Location>> highlightedLocations = new HashMap<UUID,HashSet<Location>>();

	// list of enabled world names
	private List<String> enabledWorlds;
	
	// set of road block materials
	private Set<Material> roadBlockMaterials;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	BlockManager(final PluginMain plugin) {

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
	Set<Location> getFill(final Location startLocation) {
		
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
	boolean isRoadBelowPlayer(final Player player) {
		
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
	boolean isRoadBlock(final Block block) {
		
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
		
		if (roadBlockMaterials.contains(block.getType())) {
			return true;
		}
		return false;
	}


	/**
	 * Check if block at location is a valid road block material
	 * @param location
	 * @return
	 */
	boolean isRoadBlockMaterial(final Location location) {
		
		if (roadBlockMaterials.contains(location.getBlock().getType())) {
			return true;
		}
		return false;
	}
	
	
	boolean isHighlighted(final Player player, final Location location) {
		
		if (highlightedLocations.containsKey(player.getUniqueId())
				&& highlightedLocations.get(player.getUniqueId()).contains(location)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Remove player from highlighted blocks map
	 * @param player
	 */
	void removePlayerHighlightMap(final Player player) {
		
		if (highlightedLocations.containsKey(player.getUniqueId())) {
			highlightedLocations.get(player.getUniqueId()).clear();
			highlightedLocations.remove(player.getUniqueId());
		}
	}
	

	/**
	 * Put block locations in datastore
	 * @param locationSet
	 */
	void storeLocations(final HashSet<Location> locationSet) {
		plugin.dataStore.insertRecords(locationSet);
	}
	

	/**
	 * Remove block locations from datastore
	 * @param locationSet
	 */
	void removeLocations(final HashSet<Location> locationSet) {
		plugin.dataStore.deleteRecords(locationSet);
	}
	
	
	/**
	 * Remove highlighting from blocks for player
	 * @param player
	 */
	@SuppressWarnings("deprecation")
	void unHighlightBlocks(final Player player) {
		
		if (highlightedLocations.containsKey(player.getUniqueId())) {
			HashSet<Location> blockLocations = highlightedLocations.get(player.getUniqueId());

			for (Location location : blockLocations) {

				Block block = location.getBlock();
				player.sendBlockChange(location, block.getType(), block.getData());

			}
			removePlayerHighlightMap(player);
		}
	}


	/**
	 * Highlight blocks in locationSet for player,
	 * using blocks of <code>material</code> type as highlight
	 * @param player
	 * @param locationSet
	 * @param material
	 */
	void highlightBlocks(final Player player, final HashSet<Location> locationSet, final Material material) {
		
		// if player uuid not in hashmap, insert with block list
		if (!highlightedLocations.containsKey(player.getUniqueId())) {
			highlightedLocations.put(player.getUniqueId(), locationSet);
		}
		// else add blocklist to existing player highlighted blocks in highlight map
		else {
			highlightedLocations.get(player.getUniqueId()).addAll(locationSet);
		}

		// run showHighlight task with small delay
		new ShowHighlight(player, locationSet, material).runTaskLater(plugin, 5L);
			
	}


	/**
	 * get list of enabled worlds
	 * @return ArrayList of String enabledWorlds
	 */
	List<String> getEnabledWorlds() {
		return this.enabledWorlds;
	}

	boolean worldEnabled(World world) {
		
		if (this.getEnabledWorlds().contains(world.getName())) {
			return true;
		}
		return false;
		
	}

	/**
	 * update enabledWorlds ArrayList field from config file settings
	 */
	void updateEnabledWorlds() {
	
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
	void updateMaterials() {
		
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
