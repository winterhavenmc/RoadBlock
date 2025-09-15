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

package com.winterhavenmc.roadblock.core.commands;

import com.winterhavenmc.library.messagebuilder.keys.ItemKey;
import com.winterhavenmc.library.messagebuilder.keys.ValidItemKey;
import com.winterhavenmc.roadblock.core.PluginController;
import com.winterhavenmc.roadblock.core.util.MessageId;
import com.winterhavenmc.roadblock.core.util.SoundId;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;


/**
 * A class that implements the tool subcommand
 */
final class ToolSubcommand extends AbstrtactSubcommand
{


	private final PluginController.ContextContainer ctx;

	/**
	 * Class constructor
	 */
	ToolSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "tool";
		this.usageString = "/roadblock tool";
		this.description = MessageId.COMMAND_HELP_TOOL;
		this.permissionNode = "roadblock.tool";
		this.maxArgs = 0;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList)
	{
		// sender must be player
		if (!(sender instanceof final Player player))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check player permissions
		if (!player.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_TOOL_PERMISSION).send();
			ctx.soundConfig().playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// create road block tool
		ValidItemKey itemKey = ItemKey.of("TOOL").isValid().orElseThrow();
		final ItemStack roadBlockTool = ctx.messageBuilder().itemForge().createItem(itemKey).orElse(null);

		// put tool in player's inventory
		final HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(roadBlockTool);

		// if no room in inventory, send message
		if (!noFit.isEmpty())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_TOOL_INVENTORY_FULL).send();
			ctx.soundConfig().playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// play success sound
		ctx.soundConfig().playSound(player, SoundId.COMMAND_SUCCESS_TOOL);

		return true;
	}
}
