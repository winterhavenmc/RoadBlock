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

import com.winterhavenmc.roadblock.core.context.CommandCtx;
import com.winterhavenmc.roadblock.core.util.MessageId;

import org.bukkit.command.CommandSender;

import java.util.List;


/**
 * A class that implements the reload subcommand
 */
final class ReloadSubcommand extends AbstrtactSubcommand
{
	private final CommandCtx ctx;


	/**
	 * Class constructor
	 */
	ReloadSubcommand(final CommandCtx ctx)
	{
		this.ctx = ctx;
		this.name = "reload";
		this.usageString = "/roadblock reload";
		this.description = MessageId.COMMAND_HELP_RELOAD;
		this.permissionNode = "roadblock.reload";
		this.maxArgs = 0;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList)
	{
		// check that sender has permission for reload command
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_RELOAD_PERMISSION).send();
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			displayUsage(sender);
			return true;
		}

		// re-install config file if necessary
		ctx.plugin().saveDefaultConfig();

		// reload config file
		ctx.plugin().reloadConfig();

		// update road block materials list
		ctx.blockManager().reload();

		// reload messages
		ctx.messageBuilder().reload();

		// send player success message
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_SUCCESS_RELOAD).send();

		return true;
	}

}
