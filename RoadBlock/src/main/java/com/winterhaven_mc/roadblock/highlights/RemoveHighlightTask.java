package com.winterhaven_mc.roadblock.highlights;

import com.winterhaven_mc.roadblock.PluginMain;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


final class RemoveHighlightTask extends BukkitRunnable {

	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);
	private final Player player;


	RemoveHighlightTask(final Player player) {
		this.player = player;
	}


	@Override
	public final void run() {
		plugin.highlightManager.unHighlightBlocks(player);
		plugin.highlightManager.unsetPendingRemoveTask(player);
	}

}
