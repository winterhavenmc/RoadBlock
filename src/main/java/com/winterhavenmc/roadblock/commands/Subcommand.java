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
	 * Set a subcommand name
	 *
	 * @param name the name to set for a subcommand
	 */
	void setName(final String name);


	/**
	 * Get subcommand aliases
	 * @return Collection of String - the subcommand aliases
	 */
	Collection<String> getAliases();


	/**
	 * Set subcommand aliases
	 *
	 * @param aliases a Collection of String of the aliases to set for a subcommand
	 */
	void setAliases(final Collection<String> aliases);


	/**
	 * Add an alias to a subcommand
	 *
	 * @param alias the alias to add to a subcommand
	 */
	void addAlias(final String alias);


	/**
	 * Retrieve the usage string for a subcommand
	 *
	 * @return the usage string retrieved for a subcommand
	 */
	String getUsage();


	/**
	 * Set the usage string for a subcommand
	 *
	 * @param usageString the usage string to set for a subcommand
	 */
	void setUsage(final String usageString);


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
	 * Set the message identifier for the help message to associate with the subcommand
	 *
	 * @param messageId the message identifier for the help message to associate with the subcommand
	 */
	void setDescription(final MessageId messageId);


	/**
	 * Retrieve the minimum number of arguments required for the subcommand
	 *
	 * @return int - the minimum number of arguments required for the subcommand
	 */
	int getMinArgs();


	/**
	 * Set the minimum number of arguments required for the subcommand
	 *
	 * @param minArgs the minimum number of arguments required for the subcommand
	 */
	void setMinArgs(final int minArgs);


	/**
	 * Retrieve the maximum number of arguments allowed for the subcommand
	 *
	 * @return int - the maximum number of arguments allowed for the subcommand
	 */
	int getMaxArgs();


	/**
	 * Set the maximum number of arguments allowed for the subcommand
	 *
	 * @param maxArgs the maximum number of arguments allowed for the subcommand
	 */
	void setMaxArgs(final int maxArgs);

}
