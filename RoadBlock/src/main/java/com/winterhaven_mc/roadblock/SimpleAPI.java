package com.winterhaven_mc.roadblock;

import org.bukkit.Location;
import org.bukkit.Material;

public final class SimpleAPI {
	
	/**
	 * Private constructor to prevent instantiation this class
	 */
	private SimpleAPI() {
		throw new AssertionError();
	}
	
	/**
	 * Check if location is within 5 blocks of road surface
	 * @param location
	 * @return
	 */
	public final static boolean isRoadBelow(final Location location) {
		
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
	public final static boolean isRoadBelow(final Location location, final int maxDepth) {	
		return PluginMain.instance.blockManager.isRoadBelow(location, maxDepth);
	}
	
	
	public final static boolean isRoadBlockMaterial(final Material material) {
		return PluginMain.instance.blockManager.isRoadBlockMaterial(material);
	}
	
	
}
