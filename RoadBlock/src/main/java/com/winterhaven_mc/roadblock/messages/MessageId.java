package com.winterhaven_mc.roadblock.messages;

public enum MessageId {
	
	COMMAND_SUCCESS_RELOAD,
	COMMAND_SUCCESS_SHOW,
	COMMAND_FAIL_ARGS_COUNT_UNDER,
	COMMAND_FAIL_ARGS_COUNT_OVER,
	COMMAND_FAIL_CONSOLE,
	COMMAND_FAIL_INVALID_COMMAND,
	COMMAND_FAIL_TOOL_INVENTORY_FULL,
	COMMAND_FAIL_TOOL_PERMISSION,
	COMMAND_FAIL_HELP_PERMISSION,
	COMMAND_FAIL_RELOAD_PERMISSION,
	COMMAND_FAIL_SHOW_PERMISSION,
	COMMAND_FAIL_STATUS_PERMISSION,
	COMMAND_FAIL_SET_INVALID_INTEGER,
	TOOL_SUCCESS_PROTECT,
	TOOL_SUCCESS_UNPROTECT,
	TOOL_SUCCESS_BREAK_BLOCK,
	TOOL_FAIL_DISTANCE_EXCEEDED,
	TOOL_FAIL_WORLD_DISABLED,
	TOOL_FAIL_USE_PERMISSION,
	TOOL_FAIL_USE_BLOCK_BREAK_PERMISSION,
	TOOL_FAIL_INVALID_MATERIAL,
	BLOCK_PLACE_FAIL_GRASS_PATH,
	BLOCK_PLACE_FAIL_ABOVE_ROAD,

}
