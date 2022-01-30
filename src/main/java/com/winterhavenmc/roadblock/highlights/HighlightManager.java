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

package com.winterhavenmc.roadblock.highlights;

import com.winterhavenmc.roadblock.PluginMain;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A class that manages the highlighting of blocks to show the protected status of blocks
 */
public final class HighlightManager implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// Map of highlighted block locations for players
	private final Map<UUID, HashSet<Location>> highlightMap;

	// Map of last player pending remove tasks
	private final Map<UUID, BukkitTask> pendingRemoveTask;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public HighlightManager(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// initialize highlight map
		highlightMap = new ConcurrentHashMap<>();

		// initialize timestamp map
		pendingRemoveTask = new ConcurrentHashMap<>();

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Highlight blocks in locationSet for player,
	 * using blocks of {@code material} type as highlight
	 *
	 * @param player         the player for whom to display the highlighted blocks
	 * @param locationSet    a collection of Locations of blocks to highlight
	 * @param highlightStyle HighlightStyle enum value to use for highlighting
	 */
	public void highlightBlocks(final Player player,
	                            final Collection<Location> locationSet,
	                            final HighlightStyle highlightStyle) {

		// check for null parameters
		Objects.requireNonNull(player);
		Objects.requireNonNull(locationSet);
		Objects.requireNonNull(highlightStyle);

		// if player uuid not in map, insert with locationSet
		if (!highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.put(player.getUniqueId(), new HashSet<>(locationSet));
		}
		// else add locationSet to existing player highlighted blocks in highlight map
		else {
			highlightMap.get(player.getUniqueId()).addAll(locationSet);
		}

		// run showHighlight task with small delay
		new ShowHighlightTask(plugin, player, locationSet, highlightStyle.getMaterial())
				.runTaskLaterAsynchronously(plugin, 5L);
	}


	/**
	 * Remove highlighting from all blocks for player
	 *
	 * @param player the player for whom to remove all block highlighting
	 */
	public void unHighlightBlocks(final Player player) {

		// check for null parameters
		Objects.requireNonNull(player);

		if (highlightMap.containsKey(player.getUniqueId())) {
			HashSet<Location> locationSet = highlightMap.get(player.getUniqueId());

			removeHighlight(player, locationSet);

			highlightMap.get(player.getUniqueId()).clear();
		}
	}


	/**
	 * Send block change to player with highlight material
	 *
	 * @param player      the player for whom to show highlighted blocks
	 * @param locationSet a Collection of Location of block locations to highlight
	 * @param material    the material type to use as a highlight
	 */
	void showHighlight(final Player player, final Collection<Location> locationSet, final Material material) {

		// check for null parameters
		Objects.requireNonNull(player);
		Objects.requireNonNull(locationSet);
		Objects.requireNonNull(material);

		// iterate through all location in set
		for (Location location : locationSet) {

			// create block data with passed material
			BlockData blockData = plugin.getServer().createBlockData(material);

			// send player block change with highlight material
			player.sendBlockChange(location, blockData);
		}
	}


	/**
	 * Send block change to player with highlight material
	 *
	 * @param player      the player for whom to remove highlights
	 * @param locationSet a Collection of Location of block locations to remove highlight
	 */
	void removeHighlight(final Player player, final Collection<Location> locationSet) {

		// check for null parameters
		Objects.requireNonNull(player);
		Objects.requireNonNull(locationSet);

		// iterate through all location in set
		for (Location location : locationSet) {

			// get block data at location
			BlockData blockData = location.getBlock().getBlockData();

			// send player block change with existing block type and data
			player.sendBlockChange(location, blockData);
		}
	}


	/**
	 * Remove player from highlight map
	 *
	 * @param player the player whose UUID to remove from the highlight map
	 */
	private void removePlayerFromMap(final Player player) {

		// check for null parameters
		Objects.requireNonNull(player);

		if (highlightMap.containsKey(player.getUniqueId())) {
			highlightMap.get(player.getUniqueId()).clear();
			highlightMap.remove(player.getUniqueId());
		}
	}


	/**
	 * Get a task from the pending remove map
	 *
	 * @param player the player whose task will be retrieved from the map
	 * @return BukkitTask - the task retrieved
	 */
	BukkitTask getPendingRemoveTask(final Player player) {

		// check for null parameter
		Objects.requireNonNull(player);

		return pendingRemoveTask.get(player.getUniqueId());
	}


	/**
	 * Insert a task into the pending remove task map
	 *
	 * @param player the player whose task will be inserted in the map
	 * @param task the task to be inserted in the map
	 */
	void setPendingRemoveTask(final Player player, final BukkitTask task) {

		// check for null parameters
		Objects.requireNonNull(player);
		Objects.requireNonNull(task);

		pendingRemoveTask.put(player.getUniqueId(), task);
	}


	/**
	 * Remove a task from the pending remove task map
	 *
	 * @param player the player whose task will be removed from the map
	 */
	void unsetPendingRemoveTask(final Player player) {

		// check for null parameter
		Objects.requireNonNull(player);

		pendingRemoveTask.remove(player.getUniqueId());
	}


	/**
	 * Event handler for PlayerQuitEvent;
	 * removes player from highlighted blocks hashmap
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {

		// remove player from highlight map
		plugin.highlightManager.removePlayerFromMap(event.getPlayer());
	}

}
