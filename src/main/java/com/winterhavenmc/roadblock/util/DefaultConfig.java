package com.winterhavenmc.roadblock.util;

public enum DefaultConfig {
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

	MATERIALS("[DIRT_PATH, COBBLESTONE, COBBLESTONE_SLAB, COBBLESTONE_STAIRS, " +
			"MOSSY_COBBLESTONE, MOSSY_COBBLESTONE_SLAB, MOSSY_COBBLESTONE_STAIRS, " +
			"STONE_BRICKS, STONE_BRICK_SLAB, STONE_BRICK_STAIRS, CRACKED_STONE_BRICKS, " +
			"MOSSY_STONE_BRICKS, MOSSY_STONE_BRICK_SLAB, MOSSY_STONE_BRICK_STAIRS]"),
	;

	private final String value;

	DefaultConfig(String value) {
		this.value = value;
	}

	public String getKey() {
		return this.toLowerKebabCase();
	}

	public String getValue() {
		return this.value;
	}

	private String toLowerKebabCase() {
		return this.name().toLowerCase().replace('_', '-');
	}

}
