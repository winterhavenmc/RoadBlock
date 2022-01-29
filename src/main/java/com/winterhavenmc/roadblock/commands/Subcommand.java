package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;


/**
 * An interface that declares the methods required to implement a subcommand
 */
@SuppressWarnings("unused")
interface Subcommand {

	/**
	 * Executes the given command, returning its success.
	 *
	 * @param sender the command sender (player or console) who issued the command
	 * @param argsList List of String - the arguments passed
	 * @return true if command was executed successfully, false if not
	 */
	boolean onCommand(final CommandSender sender, final List<String> argsList);


	/**
	 * Requests a list of possible completions for a command argument.
	 *
	 * @param sender The command sender (player or console) who issued the command
	 * @param command Command which was executed
	 * @param alias The alias used
	 * @param args The arguments passed to the command, including final partial argument to be completed
	 * @return A List of possible completions for the final argument
	 */
	List<String> onTabComplete(final CommandSender sender, final Command command,
							   final String alias, final String[] args);


	/**
	 * Get a subcommand name
	 *
	 * @return String - the name of the subcommand
	 */
	String getName();


	/**
	 * Get subcommand aliases
	 *
	 * @return Collection of String - the subcommand aliases
	 */
	Collection<String> getAliases();


	/**
	 * Retrieve the usage string for a subcommand
	 *
	 * @return the usage string retrieved for a subcommand
	 */
	String getUsage();


	/**
	 * Display a subcommand usage string to a command sender (player or console)
	 *
	 * @param sender the command sender for whom to display message
	 */
	void displayUsage(final CommandSender sender);


	/**
	 * Retrieve the message identifier that is associated with the subcommand help message
	 *
	 * @return MessageId - the message identifier that is associated with the subcommand description
	 */
	MessageId getDescription();


	/**
	 * Retrieve the minimum number of arguments required for the subcommand
	 *
	 * @return int - the minimum number of arguments required for the subcommand
	 */
	int getMinArgs();


	/**
	 * Retrieve the maximum number of arguments allowed for the subcommand
	 *
	 * @return int - the maximum number of arguments allowed for the subcommand
	 */
	int getMaxArgs();

}
