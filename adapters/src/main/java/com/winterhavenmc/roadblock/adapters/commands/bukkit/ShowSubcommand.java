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

package com.winterhavenmc.roadblock.adapters.commands.bukkit;

import com.winterhavenmc.roadblock.core.util.PluginCtx;
import com.winterhavenmc.roadblock.core.ports.highlights.HighlightStyle;
import com.winterhavenmc.roadblock.core.util.Macro;
import com.winterhavenmc.roadblock.core.util.MessageId;
import com.winterhavenmc.roadblock.core.util.Config;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;


/**
 * A class that implements the show subcommand
 */
final class ShowSubcommand extends AbstrtactSubcommand
{
	private final PluginCtx ctx;


	/**
	 * Class constructor
	 */
	ShowSubcommand(final PluginCtx ctx)
	{
		this.ctx = ctx;
		this.name = "show";
		this.usageString = "/roadblock show <distance>";
		this.description = MessageId.COMMAND_HELP_SHOW;
		this.permissionNode = "roadblock.show";
		this.maxArgs = 1;
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
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SHOW_PERMISSION).send();
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender);
			return true;
		}

		// get show distance from config
		int distance = Config.SHOW_DISTANCE.getInt(ctx.plugin().getConfig());

		// if argument passed, try to parse string to int
		if (argsList.size() == 1)
		{
			try
			{
				distance = Integer.parseInt(argsList.getFirst());
			}
			catch (NumberFormatException nfe)
			{
				// send player integer parse error message and return
				ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_SET_INVALID_INTEGER).send();

				// display usage message for player
				displayUsage(player);
				return true;
			}
		}

		// get set of block locations within distance of player location
		Collection<Location> locations = ctx.blocks().getNearbyBlocks(player.getLocation(), distance);

		// highlight blocks
		ctx.highlightManager().highlightBlocks(player, locations, HighlightStyle.PROTECT);

		// send player success message; TODO: add else statement to display message when empty
		if (!locations.isEmpty())
		{
			ctx.messageBuilder().compose(player, MessageId.COMMAND_SUCCESS_SHOW).setMacro(Macro.QUANTITY, locations.size()).send();
		}

		return true;
	}
}
