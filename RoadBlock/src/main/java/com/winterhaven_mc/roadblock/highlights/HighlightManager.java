package com.winterhaven_mc.roadblock.highlights;

import com.winterhaven_mc.roadblock.PluginMain;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HighlightManager {

	// reference to main class
	private final PluginMain plugin;
	
	// Map of highlighted block locations for players
	private final Map<UUID,HashSet<Location>> highlightMap;

	// Map of last player pending remove tasks
	private final Map<UUID,BukkitTask> pendingRemoveTask;
	
	
	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	public HighlightManager(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;
		
		// initialize highlight map
		highlightMap = new ConcurrentHashMap<>();
		
		// initialize timestamp map
		pendingRemoveTask = new ConcurrentHashMap<>();
	}
	
	
	/**
	 * Highlight blocks in locationSet for player,
	 * using blocks of {@code material} type as highlight
	 * @param player the player for whom to display the highlighted blocks
	 * @param locationSet a collection of Locations of blocks to highlight
	 * @param highlightStyle HighlightStyle enum value to use for highlighting
	 */
	public final void highlightBlocks(final Player player, final Collection<Location> locationSet,
			final HighlightStyle highlightStyle) {
		
		// if player uuid not in hashmap, insert with locationSet
		if (!highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.put(player.getUniqueId(), new HashSet<>(locationSet));
		}
		// else add locationSet to existing player highlighted blocks in highlight map
		else {
			highlightMap.get(player.getUniqueId()).addAll(locationSet);
		}

		// run showHighlight task with small delay
		new ShowHighlightTask(player,locationSet,highlightStyle.getMaterial()).runTaskLaterAsynchronously(plugin, 5L);
		
		// update pending remove highlight
	}
	
	
	/**
	 * Remove highlighting from blocks for player and remove locations from highlightMap
	 * @param player the player for whom to remove highlights from blocks
	 * @param locationSet a Collection of Locations to remove highlighting from blocks
	 */
	@SuppressWarnings("unused")
	public final void unHighlightBlocks(final Player player, final Collection<Location> locationSet) {
		
		// remove highlight for player for blocks in locationSet
		removeHighlight(player,locationSet);
		
		// remove locations from highlightMap
		if (highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.get(player.getUniqueId()).removeAll(locationSet);
		}
	}
	
	
	/**
	 * Remove highlighting from all blocks for player
	 * @param player the player for whom to remove all block highlighting
	 */
	public final void unHighlightBlocks(final Player player) {

		if (highlightMap.containsKey(player.getUniqueId())) {
			HashSet<Location> locationSet = highlightMap.get(player.getUniqueId());

			removeHighlight(player,locationSet);

			highlightMap.get(player.getUniqueId()).clear();
		}
	}
	
	
	/**
	 * Send block change to player with highlight material
	 * @param player the player for whom to show highlighted blocks
	 * @param locationSet a Collection of Location of block locations to highlight
	 * @param material the material type to use as a highlight
	 */
	@SuppressWarnings("deprecation")
	final void showHighlight(final Player player, final Collection<Location> locationSet, final Material material) {
		
		// iterate through all location in set
		for (Location location : locationSet) {
	
			// send player block change with highlight material
			player.sendBlockChange(location, material, (byte) 0);
		}
	}


	/**
	 * Send block change to player with highlight material
	 * @param player the player for whom to remove highlights
	 * @param locationSet a Collection of Location of block locations to remove highlight
	 */
	@SuppressWarnings("deprecation")
	private void removeHighlight(final Player player, final Collection<Location> locationSet) {
		
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
	 * @param player the player whoise UUID to remove from the highlight map
	 */
	public final void removePlayerFromMap(final Player player) {
		
		if (highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.get(player.getUniqueId()).clear();
			highlightMap.remove(player.getUniqueId());
		}
	}

	
	@SuppressWarnings("unused")
	public final boolean isHighlighted(final Player player, final Location location) {

		return highlightMap.containsKey(player.getUniqueId())
				&& highlightMap.get(player.getUniqueId()).contains(location);
	}
	
	
	final BukkitTask getPendingRemoveTask(final Player player) {
		return pendingRemoveTask.get(player.getUniqueId());
	}

	final void setPendingRemoveTask(final Player player, final BukkitTask task) {
		pendingRemoveTask.put(player.getUniqueId(), task);
		
	}
	
	final void unsetPendingRemoveTask(final Player player) {
		pendingRemoveTask.remove(player.getUniqueId());
	}

}
