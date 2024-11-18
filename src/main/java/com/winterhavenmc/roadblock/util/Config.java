package com.winterhavenmc.roadblock.util;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;


/**
 * Enum that defines config keys as members and default values as constructor parameter {@code String}.
 * <p>Provides method to fetch configuration setting using Enum member name as key.
 * Parameter values are used solely for testing validity of default config.yml file.
 */
public enum Config {
	DEBUG(Boolean.FALSE.toString()),
	PROFILE(Boolean.FALSE.toString()),
	LANGUAGE("en-US"),
	ENABLED_WORLDS(Arrays.toString(new String[]{})),
	DISABLED_WORLDS(Arrays.toString(new String[]{"disabled_world1", "disabled_world2"})),
	TOOL_MATERIAL(Material.GOLDEN_PICKAXE.name()),
	PROTECT_MATERIAL(Material.EMERALD_BLOCK.name()),
	UNPROTECT_MATERIAL(Material.REDSTONE_BLOCK.name()),
	DISPLAY_TOTAL(Boolean.TRUE.toString()),
	SPREAD_DISTANCE("100"),
	SHOW_DISTANCE("100"),
	NO_PLACE_HEIGHT("3"),
	TARGET_DISTANCE("5"),
	ON_ROAD_HEIGHT("6"),
	SNOW_PLOW(Boolean.TRUE.toString()),
	SOUND_EFFECTS(Boolean.TRUE.toString()),
	SPEED_BOOST(Boolean.TRUE.toString()),
	TITLES_ENABLED(Boolean.TRUE.toString()),
	MATERIALS(Arrays.toString(List.of(
			Material.DIRT_PATH,
			Material.COBBLESTONE,
			Material.COBBLESTONE_SLAB,
			Material.COBBLESTONE_STAIRS,
			Material.MOSSY_COBBLESTONE,
			Material.MOSSY_COBBLESTONE_SLAB,
			Material.MOSSY_COBBLESTONE_STAIRS,
			Material.STONE_BRICKS,
			Material.STONE_BRICK_SLAB,
			Material.STONE_BRICK_STAIRS,
			Material.CRACKED_STONE_BRICKS,
			Material.MOSSY_STONE_BRICKS,
			Material.MOSSY_STONE_BRICK_SLAB,
			Material.MOSSY_STONE_BRICK_STAIRS
		).toArray()));

	private final String defaultValue;


	/**
	 * Class constructor for Enum members
	 * @param defaultValue {@code String} default value referenced by corresponding key as found in config.yml file
	 */
	Config(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Get corresponding key for Enum member, formatted for style used in config.yml file
	 * @return {@code String} the key as formatted in config.yml file
	 */
	public String getKey() {
		return this.toLowerKebabCase();
	}

	/**
	 * Get default value for key, matching exactly the corresponding string in the default config.yml file
	 * @return {@code String} the value for the corresponding key
	 */
	public String getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * Get value as boolean for corresponding key in current configuration
	 * @param plugin {@code JavaPlugin} reference to the plugin instance
	 * @return {@code boolean} the referenced value in the current configuration
	 */
	public boolean getBoolean(final JavaPlugin plugin) {
		return plugin.getConfig().getBoolean(getKey());
	}

	/**
	 * Get value as int for corresponding key in current configuration
	 * @param plugin {@code JavaPlugin} reference to the plugin instance
	 * @return {@code int} the referenced value in the current configuration
	 */
	public int getInt(final JavaPlugin plugin) {
		return plugin.getConfig().getInt(getKey());
	}

	/**
	 * Get value as String for corresponding key in current configuration
	 * @param plugin {@code JavaPlugin} reference to the plugin instance
	 * @return {@code String} the referenced value in the current configuration
	 */
	public String getString(final JavaPlugin plugin) {
		return plugin.getConfig().getString(getKey());
	}

	/**
	 * Get value as List of String for corresponding key in current configuration
	 * @param plugin {@code JavaPlugin} reference to the plugin instance
	 * @return {@code List<String>} the referenced value in the current configuration
	 */
	public List<String> getStringList(final JavaPlugin plugin) {
		return plugin.getConfig().getStringList(getKey());
	}

	/**
	 * Convert Enum member name to lower kebab case
	 * @return {@code String} the Enum member name as lower kebab case
	 */
	private String toLowerKebabCase() {
		return this.name().toLowerCase().replace('_', '-');
	}

}
