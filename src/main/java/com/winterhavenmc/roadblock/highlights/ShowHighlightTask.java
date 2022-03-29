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
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

import static com.winterhavenmc.util.TimeUnit.SECONDS;


/**
 * A class that applies highlighting to blocks for a player and creates a task to remove the highlighting after a delay
 */
final class ShowHighlightTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;
	private final Collection<Location> locationSet;
	private final HighlightStyle highlightStyle;


	/**
	 * Class constructor
	 * @param plugin reference to the plugin main class
	 * @param player the player for whom to highlight blocks
	 * @param locationSet Set of Location of blocks to be highlighted
	 * @param highlightStyle the highlight style to use
	 */
	ShowHighlightTask(final PluginMain plugin,
					  final Player player,
					  final Collection<Location> locationSet,
					  final HighlightStyle highlightStyle) {

		this.plugin = plugin;
		this.player = player;
		this.locationSet = locationSet;
		this.highlightStyle = highlightStyle;
	}


	@Override
	public void run() {

		// create block data for highlight style material
		BlockData blockData = plugin.getServer().createBlockData(highlightStyle.getMaterial(plugin));

		// highlight blocks
		locationSet.forEach(location -> player.sendBlockChange(location, blockData));

		// create task to unhighlight locationSet in 30 seconds
		final BukkitTask task = new RemoveHighlightTask(plugin, player).runTaskLaterAsynchronously(plugin, SECONDS.toTicks(30));

		// if pending remove highlight task exists, cancel task
		plugin.highlightManager.cancelUnhighlightTask(player);

		// put new task in pending remove map
		plugin.highlightManager.putUnhighlightTask(player, task);
	}

}
