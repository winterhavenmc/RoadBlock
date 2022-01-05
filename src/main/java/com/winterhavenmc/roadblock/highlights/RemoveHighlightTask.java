package com.winterhavenmc.roadblock.highlights;

import com.winterhavenmc.roadblock.PluginMain;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


final class RemoveHighlightTask extends BukkitRunnable {

	private final PluginMain plugin;
	private final Player player;


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
