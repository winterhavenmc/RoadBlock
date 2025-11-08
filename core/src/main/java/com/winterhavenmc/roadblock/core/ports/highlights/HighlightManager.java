package com.winterhavenmc.roadblock.core.ports.highlights;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Optional;


public interface HighlightManager extends Listener
{
	/**
	 * Highlight blocks in locationSet for player,
	 * using blocks of {@code material} type as highlight
	 *
	 * @param player         the player for whom to display the highlighted blocks
	 * @param locationSet    a collection of Locations of blocks to highlight
	 * @param highlightStyle HighlightStyle enum value to use for highlighting
	 */
	void highlightBlocks(Player player,
	                     Collection<Location> locationSet,
	                     HighlightStyle highlightStyle);

	/**
	 * Remove highlighting from all blocks for player
	 *
	 * @param player the player for whom to remove all block highlighting
	 */
	void unHighlightBlocks(Player player);

	/**
	 * Get a task from the pending remove map
	 *
	 * @param player the player whose task will be retrieved from the map
	 * @return BukkitTask - the task retrieved
	 */
	Optional<BukkitTask> getUnhighlightTask(Player player);

	/**
	 * Insert a task into the pending remove task map
	 *
	 * @param player the player whose task will be inserted in the map
	 * @param task   the task to be inserted in the map
	 */
	void putUnhighlightTask(Player player, BukkitTask task);

	/**
	 * Cancel task for player and remove from the unhighlight task map
	 *
	 * @param player the player whose task will be removed from the map
	 */
	void cancelUnhighlightTask(Player player);

	/**
	 * Event handler for PlayerQuitEvent;
	 * removes player from highlighted blocks hashmap
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event);
}
