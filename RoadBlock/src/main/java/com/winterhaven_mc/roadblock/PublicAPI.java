package com.winterhaven_mc.roadblock;

import org.bukkit.Location;
import org.bukkit.Material;

public class PublicAPI {
	
	
	/**
	 * Check if location is within 5 blocks of road surface
	 * @param location
	 * @return
	 */
	public static boolean isRoadBelow(final Location location) {
		
		int maxDepth = 5;
		
		if (PluginMain.instance.blockManager.isRoadBelow(location, maxDepth)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Check if location is within maxDepth blocks of road surface
	 * @param location
	 * @param maxDepth
	 * @return
	 */
	public static boolean isRoadBelow(final Location location, final int maxDepth) {	
		return PluginMain.instance.blockManager.isRoadBelow(location, maxDepth);
	}
	
	
	public static boolean isRoadBlockMaterial(final Material material) {
		return PluginMain.instance.blockManager.isRoadBlockMaterial(material);
	}
	
	
}
