package com.winterhavenmc.roadblock.highlights;

import com.winterhavenmc.roadblock.PluginMain;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;


/**
 * A class that applies highlighting to blocks for a player and creates a task to remove the highlighting after a delay
 */
final class ShowHighlightTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;
	private final Collection<Location> locationSet;
	private final Material material;


	/**
	 * Class constructor
	 * @param plugin reference to the plugin main class
	 * @param player the player for whom to highlight blocks
	 * @param locationSet Set of Location of blocks to be highlighted
	 * @param material the material to use for the highlighting
	 */
	ShowHighlightTask(final PluginMain plugin,
					  final Player player,
					  final Collection<Location> locationSet,
					  final Material material) {

		this.plugin = plugin;
		this.player = player;
		this.locationSet = locationSet;
		this.material = material;
	}


	@Override
	public void run() {

		// highlight blocks
		plugin.highlightManager.showHighlight(player, locationSet, material);

		// create task to unhighlight locationSet in 30 seconds
		final BukkitTask task = new RemoveHighlightTask(plugin, player).runTaskLaterAsynchronously(plugin, 30 * 20L);

		// if pending remove highlight task exists, cancel and replace with this task
		final BukkitTask previousTask = plugin.highlightManager.getPendingRemoveTask(player);

		if (previousTask != null) {
			previousTask.cancel();
		}

		// put taskId in pending remove map
		plugin.highlightManager.setPendingRemoveTask(player, task);
	}

}
