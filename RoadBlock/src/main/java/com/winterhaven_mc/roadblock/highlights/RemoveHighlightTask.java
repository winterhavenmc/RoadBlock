package com.winterhaven_mc.roadblock.highlights;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.winterhaven_mc.roadblock.PluginMain;


public class RemoveHighlightTask extends BukkitRunnable {

	private Player player;
	
	RemoveHighlightTask(final Player player) {
		this.player = player;
	}
	
	@Override
	public void run() {

		PluginMain.instance.highlightManager.unHighlightBlocks(player);
		
		PluginMain.instance.highlightManager.unsetPendingRemoveTask(player);
		
	}

}
