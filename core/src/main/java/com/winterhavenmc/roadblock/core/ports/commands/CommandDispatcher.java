package com.winterhavenmc.roadblock.core.ports.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import javax.annotation.Nonnull;
import java.util.List;

public interface CommandDispatcher extends TabExecutor
{
	@Override
	List<String> onTabComplete(@Nonnull CommandSender sender,
	                           @Nonnull Command command,
	                           @Nonnull String alias,
	                           String[] args);

	@Override
	boolean onCommand(@Nonnull CommandSender sender,
	                  @Nonnull Command command,
	                  @Nonnull String label,
	                  String[] args);


	/**
	 * Get matching list of subcommands for which sender has permission
	 *
	 * @param sender      the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender has permission
	 */
	List<String> getMatchingSubcommandNames(CommandSender sender, String matchString);
}
