package com.winterhaven_mc.roadblock;


import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ShowHighlight extends BukkitRunnable {

	private Player player;
	private HashSet<Location> locations;
	private Material material;
	
	ShowHighlight(Player player, HashSet<Location> locations, Material material) {
		
		this.player = player;
		this.locations = locations;
		this.material = material;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {

		// highlight blocks
		for (Location location : locations) {
			player.sendBlockChange(location, material, (byte) 0);
		}
		
	}

}
