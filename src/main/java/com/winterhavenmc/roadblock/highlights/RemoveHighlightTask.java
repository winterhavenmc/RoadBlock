package com.winterhavenmc.roadblock.highlights;

import com.winterhavenmc.roadblock.PluginMain;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * A class that extends BukkitRunnable to execute a task after a delay to remove highlighting from blocks for a player
 */
final class RemoveHighlightTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;


	/**
	 * Class constructor
	 *
	 * @param plugin a reference to the plugin main class
	 * @param player the player for whom to remove block highlighting
	 */
	RemoveHighlightTask(final PluginMain plugin, final Player player) {
		this.plugin = plugin;
		this.player = player;
	}


	@Override
	public void run() {
		plugin.highlightManager.unHighlightBlocks(player);
		plugin.highlightManager.unsetPendingRemoveTask(player);
	}

}
