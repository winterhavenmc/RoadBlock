package com.winterhavenmc.roadblock.util;

import com.winterhavenmc.roadblock.storage.DataStoreType;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static org.bukkit.Material.matchMaterial;


/**
 * Enum that defines config keys as members and default values as constructor parameter {@code String}.
 * <p>Provides method to fetch configuration setting using Enum member name as key.
 * Parameter values are used solely for testing validity of default config.yml file.
 */
public enum Config {

	DEBUG(Boolean.FALSE),
	PROFILE(Boolean.FALSE),
	STORAGE_TYPE(DataStoreType.SQLITE),
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
	private static Set<Material> materialSetCache;


	/**
	 * A nested Enum that provides routines to convert between key naming conventions. The members of
	 * this Enum use upper snake case, because they are constants, while the yaml file uses
	 * lower kebab case for the key naming convention.
	 * <P>
	 * There are overloaded methods providing for passing a String or an Enum member. All methods return {@code String}.
	 * <p>
	 * <i>examples:</i>
	 * <p>
	 * <pre>
	 * {@code
	 * String fileKey = Case.LOWER_KEBAB.convert(Config.SAFETY_TIME); // safety-time
	 * String enumKey = Case.UPPER_SNAKE.convert(fileKey); // SAFETY_TIME }
	 * </pre>
	 */
	public enum Case {
		UPPER_SNAKE() {
			public String convert(final String string) {
				return string.toUpperCase().replace('-','_');
			}
		},
		LOWER_KEBAB() {
			public String convert(final String string) {
				return string.toLowerCase().replace('_','-');
			}
		};

		public abstract String convert(final String string);

		String convert(final Config config) {
			return convert(config.name());
		}
	}


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
	public String toLowerKebabCase() {
		return Case.LOWER_KEBAB.convert(this);
	}

	/**
	 * Convert Enum member name to upper snake case (used for testing)
	 * @return {@code String} the Enum member name converted to upper snake case
	 */
	public String toUpperSnakeCase() {
		return Case.UPPER_SNAKE.convert(this);
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
	public Object getDefaultObject() {
		return this.defaultObject;
	}

	/**
	 * Get value as boolean for corresponding key in current configuration
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code boolean} the referenced value in the current configuration instance
	 */
	public boolean getBoolean(final Configuration configuration) {
		return configuration.getBoolean(this.asFileKey());
	}

	/**
	 * Get value as int for corresponding key in current configuration
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code int} the referenced value in the current configuration instance
	 */
	public int getInt(final Configuration configuration) {
		return configuration.getInt(this.asFileKey());
	}

	/**
	 * Get value as String for corresponding key in current configuration
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code String} the referenced value in the current configuration instance
	 */
	public String getString(final Configuration configuration) {
		return configuration.getString(this.asFileKey());
	}

	/**
	 * Get value as List of String for corresponding key in current configuration
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code List<String>} the referenced value in the current configuration instance
	 */
	@SuppressWarnings("unused")
	public List<String> getStringList(final Configuration configuration) {
		return configuration.getStringList(this.asFileKey());
	}

	/**
	 * Get value as Set of Material for corresponding key in current configuration
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code Set<Material>} a set of matching materials from a list of strings in the current configuration instance
 	 */
	public Set<Material> getMaterialSet(final Configuration configuration) {

		// if cached set is not null, return set as result
		if (materialSetCache != null) {
			return materialSetCache;
		}

		// initialize result set
		Set<Material> resultSet = new HashSet<>();

		// get list of material names from config
		List<String> materialNameList = configuration.getStringList(this.asFileKey());

		// validate materials in config list and add to result set
		for (String materialName : materialNameList) {
			if (matchMaterial(materialName) != null) {
				resultSet.add(matchMaterial(materialName));
			}
		}

		// cache result set and return as result
		materialSetCache = resultSet;
		return resultSet;
	}

	/**
	 * Get value as Object for corresponding key in current configuration
	 * @param configuration {@code Configuration} reference to the plugin current configuration instance
	 * @return {@code Object} the referenced value in the current configuration instance
	 */
	public Object get(final Configuration configuration) {
		return configuration.get(this.asFileKey());
	}

	public static void reload(final JavaPlugin plugin) {
		// clear cache
		materialSetCache = null;

		// reload plugin config
		plugin.reloadConfig();
	}

	// for testing
	public static boolean materialCacheNull() {
		return (materialSetCache == null);
	}

}
