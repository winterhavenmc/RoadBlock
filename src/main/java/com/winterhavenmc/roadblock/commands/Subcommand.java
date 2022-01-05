package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;


@SuppressWarnings("unused")
public interface Subcommand {

	boolean onCommand(final CommandSender sender, final List<String> argsList);

	List<String> onTabComplete(final CommandSender sender, final Command command,
							   final String alias, final String[] args);

	String getName();

	void setName(final String name);

	List<String> getAliases();

	void setAliases(final List<String> aliases);

	void addAlias(final String alias);

	String getUsage();

	void setUsage(final String usageString);

	void displayUsage(final CommandSender sender);

	MessageId getDescription();

	void setDescription(final MessageId messageId);

	int getMinArgs();

	void setMinArgs(final int minArgs);

	int getMaxArgs();

	void setMaxArgs(final int maxArgs);

}
