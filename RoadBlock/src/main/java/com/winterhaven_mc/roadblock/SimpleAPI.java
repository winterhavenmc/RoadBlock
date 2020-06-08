package com.winterhaven_mc.roadblock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;


@SuppressWarnings({"unused", "WeakerAccess"})
public final class SimpleAPI {

	/**
	 * Private constructor to prevent instantiation this class
	 */
	private SimpleAPI() {
		throw new AssertionError();
	}


	/**
	 * Check if a material is in the configured list of RoadBlock materials
	 *
	 * @param material the material to check
	 * @return {@code true} if the material is in the configured list of RoadBlock materials, else {@code false}
	 */
	public static boolean isRoadBlockMaterial(final Material material) {
		return PluginMain.instance.blockManager.isRoadBlockMaterial(material);
	}


	/**
	 * Check if player location is within configured on-road-height distance above a road block
	 *
	 * @param player the player to check for road block proximity
	 * @return {@code true} if player is configured height or less above a road block, else {@code false}
	 */
	public static boolean isAboveRoad(final Player player) {
		return PluginMain.instance.blockManager.isAboveRoad(player);
	}


	/**
	 * Check if location is above a protected road block, using configured no-place-height
	 *
	 * @param location the location to check road block proximity
	 * @return {@code true} if block is within configured no-place-height above a road block, else {@code false}
	 */
	public static boolean isAboveRoad(final Location location) {

		// get configured no-place-height
		int height = PluginMain.instance.getConfig().getInt("no-place-height");

		// return result
		return PluginMain.instance.blockManager.isAboveRoad(location, height);
	}


	/**
	 * Check if location is above a protected road block, overriding configured no-place-height with passed height
	 *
	 * @param location the location to test
	 * @param height   the number of blocks above a road block to consider
	 * @return {@code true} if block is within {@code height} above a road block, else {@code false}
	 */
	public static boolean isAboveRoad(final Location location, final int height) {
		return PluginMain.instance.blockManager.isAboveRoad(location, height);
	}


	/**
	 * Check if a block can be placed at a location, using configured no-place-height
	 *
	 * @param location the location to check
	 * @return {@code true} if block can be placed at location, else {@code false}
	 */
	public static boolean canPlace(final Location location) {

		// get configured no-place-height
		int height = PluginMain.instance.getConfig().getInt("no-place-height");

		// check location with configured no-place-height
		return canPlace(location, height);
	}


	/**
	 * Check if a block can be placed at a location, overriding configured no-place-height with passed height
	 *
	 * @param location the location to check
	 * @param height   the height above road to consider not placeable
	 * @return {@code true} if the block can be place at location, else {@code false}
	 */
	public static boolean canPlace(final Location location, final int height) {

		// return result of: block is a protected road block or block is within passed height above a road block
		return PluginMain.instance.blockManager.isRoadBlock(location.getBlock())
				|| PluginMain.instance.blockManager.isAboveRoad(location, height);
	}

}
