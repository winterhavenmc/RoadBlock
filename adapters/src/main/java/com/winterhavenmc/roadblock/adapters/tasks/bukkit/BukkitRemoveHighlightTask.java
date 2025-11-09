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

package com.winterhavenmc.roadblock.adapters.tasks.bukkit;

import com.winterhavenmc.roadblock.core.ports.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.tasks.RemoveHighlightTask;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * A class that extends BukkitRunnable to execute a task after a delay to remove highlighting from blocks for a player
 */
final class BukkitRemoveHighlightTask extends BukkitRunnable implements RemoveHighlightTask
{
	private final HighlightManager highlightManager;
	private final Player player;


	/**
	 * Class constructor
	 *
	 * @param player the player for whom to remove block highlighting
	 */
	BukkitRemoveHighlightTask(final HighlightManager highlightManager, final Player player)
	{
		this.highlightManager = highlightManager;
		this.player = player;
	}


	@Override
	public void run()
	{
		highlightManager.unHighlightBlocks(player);
	}

}
