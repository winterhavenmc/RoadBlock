package com.winterhaven_mc.roadblock.highlights;

import com.winterhaven_mc.roadblock.PluginMain;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


final class RemoveHighlightTask extends BukkitRunnable {

	private final Player player;


	RemoveHighlightTask(final Player player) {
		this.player = player;
	}


	@Override
	public final void run() {
		PluginMain.instance.highlightManager.unHighlightBlocks(player);
		PluginMain.instance.highlightManager.unsetPendingRemoveTask(player);
	}

}
