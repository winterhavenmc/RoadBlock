package com.winterhaven_mc.roadblock.highlights;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.winterhaven_mc.roadblock.PluginMain;


public final class ShowHighlightTask extends BukkitRunnable {
	
	private final PluginMain plugin = PluginMain.instance;
	
	private final Player player;
	private final Collection<Location> locationSet;
	private final Material material;
	
	ShowHighlightTask(final Player player, final Collection<Location> locationSet, final Material material) {
		
		this.player = player;
		this.locationSet = locationSet;
		this.material = material;
	}
	
	@Override
	public void run() {

		// highlight blocks
		plugin.highlightManager.showHighlight(player, locationSet, material);

		// create task to unhighlight locationSet in 30 seconds
		final BukkitTask task = new RemoveHighlightTask(player).runTaskLaterAsynchronously(plugin, 30 * 20L);
		
		// if pending remove highlight task exists, cancel and replace with this task
		final BukkitTask previousTask = plugin.highlightManager.getPendingRemoveTask(player);
		
		if (previousTask != null) {
			previousTask.cancel();
		}
		
		// put taskId in pending remove map
		plugin.highlightManager.setPendingRemoveTask(player, task);
	}

}
