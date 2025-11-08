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

package com.winterhavenmc.roadblock.adapters.highlights.bukkit;

import com.winterhavenmc.roadblock.core.ports.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.ports.highlights.HighlightStyle;
import com.winterhavenmc.roadblock.core.tasks.ShowHighlightTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A class that manages the highlighting of blocks to show the protected status of blocks
 */
public final class BukkitHighlightManager implements HighlightManager
{
	private final Plugin plugin;
	private final Map<UUID, Set<Location>> highlightLocationMap;
	private final Map<UUID, BukkitTask> unHighlightTaskMap;


	/**
	 * Class constructor
	 */
	public BukkitHighlightManager(final Plugin plugin)
	{
		this.plugin = plugin;
		highlightLocationMap = new ConcurrentHashMap<>();
		unHighlightTaskMap = new ConcurrentHashMap<>();

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
	@Override
	public void highlightBlocks(final Player player,
	                            final Collection<Location> locationSet,
	                            final HighlightStyle highlightStyle)
	{
		// null parameter check
		if (player != null && locationSet != null && highlightStyle != null)
		{
			// if player uuid is in map, add all in locationSet
			if (highlightLocationMap.containsKey(player.getUniqueId()))
			{
				// add locationSet to existing player highlighted blocks in highlight map
				highlightLocationMap.get(player.getUniqueId()).addAll(locationSet);
			}
			else
			{
				// add newly created locationSet for player
				highlightLocationMap.put(player.getUniqueId(), new HashSet<>(locationSet));
			}

			// run showHighlight task with small delay
			new ShowHighlightTask(plugin, this, player, locationSet, highlightStyle)
					.runTaskLaterAsynchronously(plugin, 2L);
		}
	}


	/**
	 * Remove highlighting from all blocks for player
	 *
	 * @param player the player for whom to remove all block highlighting
	 */
	@Override
	public void unHighlightBlocks(final Player player)
	{
		// null parameter check
		if (player != null)
		{
			// check if player has entry in highlight map
			if (highlightLocationMap.containsKey(player.getUniqueId()))
			{
				// get block locations from highlight map for player
				Set<Location> locationSet = highlightLocationMap.get(player.getUniqueId());

				// send block change to player for each block location in highlight map for player
				locationSet.forEach(location -> player.sendBlockChange(location, location.getBlock().getBlockData()));

				// remove player from highlight map
				highlightLocationMap.remove(player.getUniqueId());

				// cancel unhighlight task for player
				cancelUnhighlightTask(player);
			}
		}
	}


	/**
	 * Get a task from the pending remove map
	 *
	 * @param player the player whose task will be retrieved from the map
	 * @return BukkitTask - the task retrieved
	 */
	@Override
	public Optional<BukkitTask> getUnhighlightTask(Player player)
	{
		return (player != null)
				? Optional.ofNullable(unHighlightTaskMap.get(player.getUniqueId()))
				: Optional.empty();
	}


	/**
	 * Insert a task into the pending remove task map
	 *
	 * @param player the player whose task will be inserted in the map
	 * @param task   the task to be inserted in the map
	 */
	@Override
	public void putUnhighlightTask(final Player player, final BukkitTask task)
	{
		// null parameter check
		if (player != null && task != null)
		{
			// insert entry for player in pending remove task map
			unHighlightTaskMap.put(player.getUniqueId(), task);
		}
	}


	/**
	 * Cancel task for player and remove from the unhighlight task map
	 *
	 * @param player the player whose task will be removed from the map
	 */
	@Override
	public void cancelUnhighlightTask(final Player player)
	{
		// null parameter check
		if (player != null)
		{
			// cancel pending unhighlight task for player
			getUnhighlightTask(player).ifPresent(BukkitTask::cancel);

			// remove entry for player from unhighlight task map
			unHighlightTaskMap.remove(player.getUniqueId());
		}
	}


	/**
	 * Event handler for PlayerQuitEvent;
	 * removes player from highlighted blocks hashmap
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	@Override
	public void onPlayerQuit(final PlayerQuitEvent event)
	{
		// null parameter check
		if (event != null)
		{
			// remove any entry for player from highlight map
			highlightLocationMap.remove(event.getPlayer().getUniqueId());

			// cancel any pending unhighlight task for player
			cancelUnhighlightTask(event.getPlayer());
		}
	}

}
