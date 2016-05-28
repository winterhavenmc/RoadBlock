package com.winterhaven_mc.roadblock;

import org.bukkit.Location;
import org.bukkit.Material;

@SuppressWarnings("unused")
public final class SimpleAPI {
	
	/**
	 * Private constructor to prevent instantiation this class
	 */
	private SimpleAPI() {
		throw new AssertionError();
	}
	
	/**
	 * Check if location is within 5 blocks above a road block
	 * @param location the location to check road block proximity
	 * @return {@code true} if block is 5 blocks or less above a road block, else {@code false}
	 */
	public static boolean isRoadBelow(final Location location) {
		
		int maxDepth = 5;

		return PluginMain.instance.blockManager.isRoadBelow(location, maxDepth);
	}
	
	
	/**
	 * Check if location is within maxDepth blocks of a road block
	 * @param location the location to test
	 * @param maxDepth the number of blocks above a road block to consider
	 * @return {@code true} if block is within {@code maxDepth} above a road block, else {@code false}
	 */
	public static boolean isRoadBelow(final Location location, final int maxDepth) {
		return PluginMain.instance.blockManager.isRoadBelow(location, maxDepth);
	}
	
	
	public static boolean isRoadBlockMaterial(final Material material) {
		return PluginMain.instance.blockManager.isRoadBlockMaterial(material);
	}
	
	
}
