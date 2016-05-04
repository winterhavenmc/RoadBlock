package com.winterhaven_mc.roadblock.highlights;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.winterhaven_mc.roadblock.PluginMain;

public class HighlightManager {

	// reference to main class
	private final PluginMain plugin;
	
	// Map of highlighted block locations for players
	private Map<UUID,HashSet<Location>> highlightMap;

	// Map of last player pending remove tasks
	private Map<UUID,BukkitTask> pendingRemoveTask;
	
	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public HighlightManager(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;
		
		// initialize highlight map
		highlightMap = new ConcurrentHashMap<UUID,HashSet<Location>>();
		
		// initialize timestamp map
		pendingRemoveTask = new ConcurrentHashMap<UUID,BukkitTask>();
	}
	
	
	/**
	 * Highlight blocks in locationSet for player,
	 * using blocks of <code>material</code> type as highlight
	 * @param player
	 * @param locationSet
	 * @param material
	 */
	public void highlightBlocks(final Player player, final Collection<Location> locationSet, final Material material) {
		
		// if player uuid not in hashmap, insert with locationSet
		if (!highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.put(player.getUniqueId(), new HashSet<Location>(locationSet));
		}
		// else add locationSet to existing player highlighted blocks in highlight map
		else {
			highlightMap.get(player.getUniqueId()).addAll(locationSet);
		}

		// run showHighlight task with small delay
		new ShowHighlightTask(player,locationSet,material).runTaskLaterAsynchronously(plugin, 5L);
		
		// update pending remove highlight
	}
	
	
	/**
	 * Remove highlighting from blocks for player and remove locations from highlightMap
	 * @param player
	 */
	public void unHighlightBlocks(final Player player, final Collection<Location> locationSet) {
		
		// remove highlight for player for blocks in locationSet
		removeHighlight(player,locationSet);
		
		// remove locations from highlightMap
		if (highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.get(player.getUniqueId()).removeAll(locationSet);
		}
	}
	
	
	/**
	 * Remove highlighting from all blocks for player
	 * @param player
	 */
	public void unHighlightBlocks(final Player player) {

		if (highlightMap.containsKey(player.getUniqueId())) {
			HashSet<Location> locationSet = highlightMap.get(player.getUniqueId());

			removeHighlight(player,locationSet);

			highlightMap.get(player.getUniqueId()).clear();
		}
	}
	
	
	/**
	 * Send block change to player with highlight material
	 * @param player
	 * @param locationSet
	 * @param material
	 */
	@SuppressWarnings("deprecation")
	public void showHighlight(final Player player, final Collection<Location> locationSet, final Material material) {
		
		// iterate through all location in set
		for (Location location : locationSet) {
	
			// send player block change with highlight material
			player.sendBlockChange(location, material, (byte) 0);
		}
	}


	/**
	 * Send block change to player with highlight material
	 * @param player
	 * @param locationSet
	 * @param material
	 */
	@SuppressWarnings("deprecation")
	public void removeHighlight(final Player player, final Collection<Location> locationSet) {
		
		// iterate through all location in set
		for (Location location : locationSet) {
	
			// get block at location
			Block block = location.getBlock();
			
			// send player block change with existing block type and data
			player.sendBlockChange(location, block.getType(), block.getData());
		}
	}


	/**
	 * Remove player from highlight map
	 * @param player
	 */
	public void removePlayerFromMap(final Player player) {
		
		if (highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.get(player.getUniqueId()).clear();
			highlightMap.remove(player.getUniqueId());
		}
	}

	
	public boolean isHighlighted(final Player player, final Location location) {
		
		if (highlightMap.containsKey(player.getUniqueId())
				&& highlightMap.get(player.getUniqueId()).contains(location)) {
			return true;
		}
		return false;
	}
	
	
	BukkitTask getPendingRemoveTask(final Player player) {
		return pendingRemoveTask.get(player.getUniqueId());
	}

	void setPendingRemoveTask(final Player player, final BukkitTask task) {
		pendingRemoveTask.put(player.getUniqueId(), task);
		
	}
	
	void unsetPendingRemoveTask(final Player player) {
		pendingRemoveTask.remove(player.getUniqueId());
	}

}
