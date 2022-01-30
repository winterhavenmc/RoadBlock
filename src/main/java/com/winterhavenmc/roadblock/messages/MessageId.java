/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.roadblock.messages;

/**
 * Enum with entries for all player messages in language configuration files
 */
public enum MessageId {

	COMMAND_HELP_INVALID,
	COMMAND_HELP_HELP,
	COMMAND_HELP_MATERIALS,
	COMMAND_HELP_RELOAD,
	COMMAND_HELP_SHOW,
	COMMAND_HELP_STATUS,
	COMMAND_HELP_TOOL,
	COMMAND_HELP_USAGE_HEADER,

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
	COMMAND_FAIL_MATERIALS_PERMISSION,
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
