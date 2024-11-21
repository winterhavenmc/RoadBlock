package com.winterhavenmc.roadblock.util;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.List;
import java.util.Locale;


/**
 * Enum that defines config keys as members and default values as constructor parameter {@code String}.
 * <p>Provides method to fetch configuration setting using Enum member name as key.
 * Parameter values are used solely for testing validity of default config.yml file.
 */
public enum Config {

	DEBUG(Boolean.FALSE),
	PROFILE(Boolean.FALSE),
	LANGUAGE(Locale.US.toLanguageTag()),
	ENABLED_WORLDS(List.of()),
	DISABLED_WORLDS(List.of("disabled_world1", "disabled_world2")),
	TOOL_MATERIAL(Material.GOLDEN_PICKAXE),
	PROTECT_MATERIAL(Material.EMERALD_BLOCK),
	UNPROTECT_MATERIAL(Material.REDSTONE_BLOCK),
	DISPLAY_TOTAL(Boolean.TRUE),
	SPREAD_DISTANCE(100),
	SHOW_DISTANCE(100),
	NO_PLACE_HEIGHT(3),
	TARGET_DISTANCE(5),
	ON_ROAD_HEIGHT(6),
	SNOW_PLOW(Boolean.TRUE),
	SOUND_EFFECTS(Boolean.TRUE),
	SPEED_BOOST(Boolean.TRUE),
	TITLES_ENABLED(Boolean.TRUE),
	MATERIALS(List.of(
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
	));

	private final Object defaultObject;

	/**
	 * Class constructor for Enum members
	 * @param defaultValue {@code Object} default value referenced by corresponding key as found in config.yml file
	 */
	Config(final Object defaultValue) {
		this.defaultObject = defaultValue;
	}

	/**
	 * Get corresponding key for Enum member, formatted for style used in config.yml file
	 * @return {@code String} the key as formatted in config.yml file
	 */
	public String asFileKey() {
		return this.toLowerKebabCase();
	}

	/**
	 * Convert Enum member name to lower kebab case
	 * @return {@code String} the Enum member name as lower kebab case
	 */
	private String toLowerKebabCase() {
		return this.name().toLowerCase().replace('_', '-');
	}

	/**
	 * Get default value for key, matching exactly the corresponding string in the default config.yml file
	 * @return {@code String} the value for the corresponding key
	 */
	public String getDefaultString() {
		return this.defaultObject.toString();
	}

	/**
	 * Get default object for key
	 * @return {@code Object} the default object
	 */
	@SuppressWarnings("unused")
	public Object getDefaultObject() {
		return this.defaultObject;
	}

	/**
	 * Get value as boolean for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code boolean} the referenced value in the current configuration instance
	 */
	public boolean getBoolean(final Configuration configuration) {
		return configuration.getBoolean(this.asFileKey());
	}

	/**
	 * Get value as int for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code int} the referenced value in the current configuration instance
	 */
	public int getInt(final Configuration configuration) {
		return configuration.getInt(this.asFileKey());
	}

	/**
	 * Get value as String for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code String} the referenced value in the current configuration instance
	 */
	public String getString(final Configuration configuration) {
		return configuration.getString(this.asFileKey());
	}

	/**
	 * Get value as List of String for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code List<String>} the referenced value in the current configuration instance
	 */
	public List<String> getStringList(final Configuration configuration) {
		return configuration.getStringList(this.asFileKey());
	}

	/**
	 * Get value as Object for corresponding key in current configuration
	 *
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code Object} the referenced value in the current configuration instance
	 */
	public Object get(final Configuration configuration) {
		return configuration.get(this.asFileKey());
	}

}
