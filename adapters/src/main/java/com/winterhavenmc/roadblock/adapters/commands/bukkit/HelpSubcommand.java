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
import com.winterhavenmc.roadblock.core.util.MessageId;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * A class that implements the help subcommand
 */
final class HelpSubcommand extends AbstrtactSubcommand
{
	private final PluginCtx ctx;
	private final SubcommandRegistry subcommandRegistry;


	/**
	 * Class constructor
	 *
	 * @param subcommandRegistry a reference to the subcommand registry
	 */
	HelpSubcommand(final PluginCtx ctx, final SubcommandRegistry subcommandRegistry)
	{
		this.ctx = ctx;
		this.subcommandRegistry = Objects.requireNonNull(subcommandRegistry);
		this.name = "help";
		this.usageString = "/roadblock help [command]";
		this.description = MessageId.COMMAND_HELP_HELP;
		this.permissionNode = "roadblock.help";
		this.maxArgs = 1;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args)
	{
		if (args.length == 2 && args[0].equalsIgnoreCase(this.name))
		{
			return getCommandNames(sender, args);
		}
		return Collections.emptyList();
	}


	private @NotNull List<String> getCommandNames(CommandSender sender, String[] args)
	{
		return subcommandRegistry.getKeys().stream()
				.map(subcommandRegistry::getSubcommand)
				.filter(Optional::isPresent)
				.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
				.map(subcommand -> subcommand.get().getName())
				.filter(subCommandName -> subCommandName.toLowerCase().startsWith(args[1].toLowerCase()))
				.filter(subCommandName -> !subCommandName.equalsIgnoreCase(this.name))
				.collect(Collectors.toList());
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args)
	{
		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_HELP_PERMISSION).send();
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.isEmpty())
		{
			displayUsageAll(sender);
			return true;
		}

		// display subcommand help message or invalid command message
		subcommandRegistry.getSubcommand(args.getFirst()).ifPresentOrElse(
				subcommand -> sendCommandHelpMessage(sender, subcommand),
				() -> sendCommandInvalidMessage(sender)
		);

		return true;
	}


	/**
	 * Send help description for subcommand to command sender
	 *
	 * @param sender     the command sender
	 * @param subcommand the subcommand to display help description
	 */
	private void sendCommandHelpMessage(final CommandSender sender, final Subcommand subcommand)
	{
		ctx.messageBuilder().compose(sender, subcommand.getDescription()).send();
		subcommand.displayUsage(sender);
	}


	/**
	 * Send invalid subcommand message to command sender
	 *
	 * @param sender the command sender
	 */
	private void sendCommandInvalidMessage(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_INVALID_HELP).send();
		displayUsageAll(sender);
	}


	/**
	 * Display usage message for all commands
	 *
	 * @param sender the command sender
	 */
	void displayUsageAll(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_HELP_USAGE_HEADER).send();
		subcommandRegistry.getKeys().stream()
				.map(subcommandRegistry::getSubcommand)
				.filter(Optional::isPresent)
				.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
				.forEach(subcommand -> subcommand.get().displayUsage(sender));
	}

}
