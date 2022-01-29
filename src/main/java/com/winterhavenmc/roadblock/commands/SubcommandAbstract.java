package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;


abstract class SubcommandAbstract implements Subcommand {

	private String name;
	private Collection<String> aliases = new LinkedHashSet<>();
	private String usageString;
	private MessageId description;
	private int minArgs;
	private int maxArgs;


	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<String> getAliases() {
		return aliases;
	}

	@Override
	public String getUsage() {
		return usageString;
	}

	@Override
	public void displayUsage(final CommandSender sender) {
		sender.sendMessage(usageString);
	}

	@Override
	public MessageId getDescription() {
		return description;
	}

	@Override
	public int getMinArgs() { return minArgs; }

	@Override
	public int getMaxArgs() { return maxArgs; }


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
									  final String alias, final String[] args) {

		return Collections.emptyList();
	}

}
