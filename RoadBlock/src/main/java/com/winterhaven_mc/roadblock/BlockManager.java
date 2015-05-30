package com.winterhaven_mc.roadblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockManager {

	PluginMain plugin;
	HashSet<Material> roadBlockMaterials;

	HashMap<UUID,HashSet<Location>> highlightedLocations = new HashMap<UUID,HashSet<Location>>();
	
	private ArrayList<String> enabledWorlds;
	
	/**
	 * Class constructor
	 * @param plugin
	 */
	BlockManager(PluginMain plugin) {
		
		this.plugin = plugin;
	
		roadBlockMaterials = getMaterials();
		
		updateEnabledWorlds();	
	}
	

	/**
	 * Parse valid road block materials from config file
	 * @return HashSet of materials
	 */
	HashSet<Material> getMaterials() {
		
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
			if (matchMaterial != null) {
				returnSet.add(matchMaterial);
				if (plugin.debug) {
					plugin.getLogger().info("Added material to road block material list: " + matchMaterial.toString());
				}
			}
		}
		return returnSet;
	}
	
	
	HashSet<Location> spreadLocation(Player player, Location location) {
		
		HashSet<Location> returnSet = new HashSet<Location>();
		
		// if block at location is not road block material, return
		if (!isRoadBlockMaterial(location.getBlock())) {
			return returnSet;
		}
		
		// if location is already highlighted, return
		if (isHighlighted(player,location)) {
			return returnSet;
		}
		
		// if location is too far from player, return
		if (location.distanceSquared(player.getLocation()) > 2500) {
			return returnSet;
		}

		// if player uuid not in hashmap, insert with empty returnSet
		if (!highlightedLocations.containsKey(player.getUniqueId())) {
			highlightedLocations.put(player.getUniqueId(), returnSet);
		}

		// put block location in highlighted blocks map
		highlightedLocations.get(player.getUniqueId()).add(location);

		// put block location in returnSet
		returnSet.add(location);

		// try adjacent block east
		returnSet.addAll(spreadLocation(player, location.clone().add(1, 0, 0)));

		// try adjacent block west
		returnSet.addAll(spreadLocation(player, location.clone().add(-1, 0, 0)));

		// try adjacent block north
		returnSet.addAll(spreadLocation(player, location.clone().add(0, 0, 1)));

		// try adjacent block south
		returnSet.addAll(spreadLocation(player, location.clone().add(0, 0, -1)));
		
		return returnSet;
	}
	
	
	boolean isRoadBelowPlayer(Player player) {
		
		int depth = 0;
		int maxDepth = 3;
	
		// convert player location to block location (with integer coordinates)
		Location testLocation = player.getLocation().getBlock().getLocation();
		
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


	boolean isRoadBlock(Block block) {
		
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
	
	
	boolean isRoadBlockMaterial(Block block) {
		
		if (roadBlockMaterials.contains(block.getType())) {
			return true;
		}
		return false;
	}


	boolean isRoadBlockMaterial(Location location) {
		
		if (roadBlockMaterials.contains(location.getBlock().getType())) {
			return true;
		}
		return false;
	}
	
	boolean isHighlighted(Player player, Location location) {
		
		if (highlightedLocations.containsKey(player.getUniqueId())
				&& highlightedLocations.get(player.getUniqueId()).contains(location)) {
			return true;
		}
		return false;
	}
	
	
	void removePlayerHighlightMap(Player player) {
		
		if (highlightedLocations.containsKey(player.getUniqueId())) {
			highlightedLocations.get(player.getUniqueId()).clear();
			highlightedLocations.remove(player.getUniqueId());
		}
		
	}
	
	
	void storeLocations(HashSet<Location> locationSet) {
		plugin.dataStore.insertRecords(locationSet);
	}
	
	
	void removeLocations(HashSet<Location> locationSet) {
		plugin.dataStore.deleteRecords(locationSet);
	}
	
	
	@SuppressWarnings("deprecation")
	void unHighlightBlocks(Player player) {
		
		if (highlightedLocations.containsKey(player.getUniqueId())) {
			HashSet<Location> blockLocations = highlightedLocations.get(player.getUniqueId());

			for (Location location : blockLocations) {

				Block block = location.getBlock();
				player.sendBlockChange(location, block.getType(), block.getData());

			}
			removePlayerHighlightMap(player);
		}
	}


	void highlightBlocks(Player player, HashSet<Location> locationSet, Material material) {
		
		// if player uuid not in hashmap, insert with block list
		if (!highlightedLocations.containsKey(player.getUniqueId())) {
			highlightedLocations.put(player.getUniqueId(), locationSet);
		}
		// else add blocklist to existing player highlighted blocks in highlight map
		else {
			highlightedLocations.get(player.getUniqueId()).addAll(locationSet);
		}

		new ShowHighlight(player, locationSet, material).runTaskLater(plugin, 5L);
			
	}


	/**
	 * get list of enabled worlds
	 * @return ArrayList of String enabledWorlds
	 */
	ArrayList<String> getEnabledWorlds() {
		return this.enabledWorlds;
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

	
	void updateMaterials() {		
		roadBlockMaterials = getMaterials();
	}
}
