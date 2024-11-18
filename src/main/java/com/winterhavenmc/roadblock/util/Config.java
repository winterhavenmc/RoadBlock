package com.winterhavenmc.roadblock.util;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;


public enum Config {
	DEBUG("false"),
	PROFILE("false"),
	LANGUAGE("en-US"),
	ENABLED_WORLDS("[]"),
	DISABLED_WORLDS("[disabled_world1, disabled_world2]"),
	TOOL_MATERIAL("GOLDEN_PICKAXE"),
	PROTECT_MATERIAL("EMERALD_BLOCK"),
	UNPROTECT_MATERIAL("REDSTONE_BLOCK"),
	DISPLAY_TOTAL("true"),
	SPREAD_DISTANCE("100"),
	SHOW_DISTANCE("100"),
	NO_PLACE_HEIGHT("3"),
	TARGET_DISTANCE("5"),
	ON_ROAD_HEIGHT("6"),
	SNOW_PLOW("true"),
	SOUND_EFFECTS("true"),
	SPEED_BOOST("true"),
	TITLES_ENABLED("true"),
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

	Config(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return this.toLowerKebabCase();
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public Boolean getBoolean(JavaPlugin plugin) {
		return plugin.getConfig().getBoolean(getKey());
	}

	public int getInt(final JavaPlugin plugin) {
		return plugin.getConfig().getInt(getKey());
	}

	public String getString(final JavaPlugin plugin) {
		return plugin.getConfig().getString(getKey());
	}

	public List<String> getStringList(final JavaPlugin plugin) {
		return plugin.getConfig().getStringList(getKey());
	}

	private String toLowerKebabCase() {
		return this.name().toLowerCase().replace('_', '-');
	}

}
