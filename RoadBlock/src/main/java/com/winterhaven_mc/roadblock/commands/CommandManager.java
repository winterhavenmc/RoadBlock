package com.winterhaven_mc.roadblock.commands;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.highlights.HighlightStyle;
import com.winterhaven_mc.roadblock.messages.Message;
import com.winterhaven_mc.roadblock.sounds.SoundId;
import com.winterhaven_mc.roadblock.utilities.RoadBlockTool;

import com.winterhaven_mc.util.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.winterhaven_mc.roadblock.messages.Macro.*;
import static com.winterhaven_mc.roadblock.messages.MessageId.*;


public final class CommandManager implements CommandExecutor, TabCompleter {

	private final PluginMain plugin;

	private final static ChatColor helpColor = ChatColor.YELLOW;
	private final static ChatColor usageColor = ChatColor.GOLD;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public CommandManager(final PluginMain plugin) {

		// reference to main class
		this.plugin = plugin;

		// register this class as command executor
		//noinspection ConstantConditions
		plugin.getCommand("roadblock").setExecutor(this);

		// register this class as tab completer
		//noinspection ConstantConditions
		plugin.getCommand("roadblock").setTabCompleter(this);
	}


	/**
	 * Tab completer for RoadBlock commands
	 */
	@Override
	public List<String> onTabComplete(final CommandSender sender,
									  final Command command,
									  final String alias,
									  final String[] args) {

		List<String> returnList = new ArrayList<>();

		// return list of valid matching subcommands
		if (args.length == 1) {
			for (Subcommand subcommand : Subcommand.values()) {
				if (sender.hasPermission("roadblock." + subcommand.toString())
						&& subcommand.toString().startsWith(args[0].toLowerCase())) {
					returnList.add(subcommand.toString());
				}
			}
		}
		else if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
			for (Subcommand subcommand : Subcommand.values()) {
				if (sender.hasPermission("roadblock." + subcommand.toString())
						&& subcommand.toString().startsWith(args[1].toLowerCase())) {
					returnList.add(subcommand.toString());
				}
			}
		}
		return returnList;
	}


	@Override
	public final boolean onCommand(final CommandSender sender, final Command command,
								   final String label, final String[] args) {

		final int minArgs = 1;
		final int maxArgs = 2;

		// check min arguments
		if (args.length < minArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_UNDER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return false;
		}

		// check max arguments
		if (args.length > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return false;
		}

		// get subcommand
		String subcommand = args[0];

		// status command
		if (subcommand.equalsIgnoreCase("status")) {
			return statusCommand(sender, args);
		}

		// reload command
		if (subcommand.equalsIgnoreCase("reload")) {
			return reloadCommand(sender, args);
		}

		// show command
		if (subcommand.equalsIgnoreCase("show")) {
			return showCommand(sender, args);
		}

		// tool command
		if (subcommand.equalsIgnoreCase("tool")) {
			return toolCommand(sender, args);
		}

		// help command
		if (subcommand.equalsIgnoreCase("help")) {
			return helpCommand(sender, args);
		}

		Message.create(sender, COMMAND_FAIL_INVALID_COMMAND).send();
		plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		return false;
	}


	/**
	 * Display plugin status
	 *
	 * @param sender the command sender
	 * @return always returns {@code true}, to prevent usage message
	 */
	private boolean statusCommand(final CommandSender sender, String[] args) {

		// check that sender has permission for status command
		if (!sender.hasPermission("roadblock.status")) {
			Message.create(sender, COMMAND_FAIL_STATUS_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		}

		// set argument limits
		int maxArgs = 1;

		// check max arguments
		if (args.length > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return false;
		}

		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_GRAY + "["
				+ ChatColor.YELLOW + plugin.getName() + ChatColor.DARK_GRAY + "] "
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + versionString);

		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		if (plugin.profile) {
			sender.sendMessage(ChatColor.DARK_RED + "PROFILE: true");
		}

		if (plugin.getConfig().getBoolean("display-total")) {
			sender.sendMessage(ChatColor.GREEN + "Total blocks protected: "
					+ ChatColor.RESET + plugin.blockManager.getBlockTotal() + " blocks");
		}

		sender.sendMessage(ChatColor.GREEN + "Spread distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("spread-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Show distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("show-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "No place height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("no-place-height") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Player on road height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("on-road-height") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Mob targeting distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("target-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Snow plow: "
				+ ChatColor.RESET + plugin.getConfig().getString("snow-plow"));

		sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());
		return true;
	}


	/**
	 * Reload configuration
	 *
	 * @param sender the command sender
	 * @return always returns {@code true}, to prevent usage message
	 */
	private boolean reloadCommand(final CommandSender sender, String[] args) {

		// check that sender has permission for reload command
		if (!sender.hasPermission("roadblock.reload")) {
			Message.create(sender, COMMAND_FAIL_RELOAD_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// set argument limits
		int maxArgs = 1;

		// check max arguments
		if (args.length > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return false;
		}

		// re-install config file if necessary
		plugin.saveDefaultConfig();

		// reload config file
		plugin.reloadConfig();

		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");

		// update profile field
		plugin.profile = plugin.getConfig().getBoolean("profile");

		// update road block materials list
		plugin.blockManager.reload();

		// reload messages
		LanguageManager.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload enabled worlds
		plugin.worldManager.reload();

		// send player success message
		Message.create(sender, COMMAND_SUCCESS_RELOAD).send();
		return true;
	}


	/**
	 * Highlight blocks that are within specified distance of player location
	 *
	 * @param sender the command sender
	 * @param args   command arguments
	 * @return always returns {@code true}, to prevent (bukkit) usage message
	 */
	private boolean showCommand(final CommandSender sender, String[] args) {

		// sender must be player
		if (!(sender instanceof Player)) {
			Message.create(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// get player from sender
		final Player player = (Player) sender;

		// check player permissions
		if (!player.hasPermission("roadblock.show")) {
			Message.create(sender, COMMAND_FAIL_SHOW_PERMISSION).send();
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// get show distance from config
		int distance = plugin.getConfig().getInt("show-distance");

		// if argument passed, try to parse string to int
		if (args.length == 2) {
			try {
				distance = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException nfe) {
				// send player integer parse error message and return
				Message.create(sender, COMMAND_FAIL_SET_INVALID_INTEGER).send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);

				player.sendMessage("§6/roadblock show <distance>");
				return true;
			}
		}

		// get set of block locations within distance of player location
		Set<Location> locations = plugin.blockManager.selectNearbyBlocks(player.getLocation(), distance);

		// highlight blocks
		plugin.highlightManager.highlightBlocks(player, locations, HighlightStyle.PROTECT);

		// send player success message
		Message.create(player, COMMAND_SUCCESS_SHOW).setMacro(QUANTITY, locations.size()).send();

		// if any blocks highlighted, play sound
		if (locations.size() > 0) {
			plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_SHOW);
		}

		return true;
	}


	/**
	 * Place a tool in player inventory
	 *
	 * @param sender the command sender
	 * @return always returns {@code true}, to prevent usage message
	 */
	private boolean toolCommand(final CommandSender sender, String[] args) {

		// sender must be player
		if (!(sender instanceof Player)) {
			Message.create(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// cast sender to player
		final Player player = (Player) sender;

		// check player permissions
		if (!player.hasPermission("roadblock.tool")) {
			Message.create(sender, COMMAND_FAIL_TOOL_PERMISSION).send();
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// set argument limits
		int maxArgs = 1;

		// check max arguments
		if (args.length > maxArgs) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return false;
		}

		// create road block tool itemStack
		final ItemStack rbTool = RoadBlockTool.create();

		// put tool in player's inventory
		final HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(rbTool);

		// if no room in inventory, send message
		if (!noFit.isEmpty()) {
			Message.create(sender, COMMAND_FAIL_TOOL_INVENTORY_FULL).send();
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// play success sound
		plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_TOOL);
		return true;
	}


	/**
	 * Display help message for commands
	 *
	 * @param sender the command sender
	 * @param args   the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage message
	 */
	private boolean helpCommand(final CommandSender sender, final String[] args) {

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission("roadblock.help")) {
			Message.create(sender, COMMAND_FAIL_HELP_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		String command = "help";

		if (args.length > 1) {
			command = args[1];
		}

		String helpMessage = "That is not a valid command.";

		if (command.equalsIgnoreCase("status")) {
			helpMessage = "Displays current configuration settings.";
		}
		if (command.equalsIgnoreCase("reload")) {
			helpMessage = "Reloads the configuration without needing to restart the server.";
		}
		if (command.equalsIgnoreCase("show")) {
			helpMessage = "Highlights protected RoadBlocks within configured radius.";
		}
		if (command.equalsIgnoreCase("tool")) {
			helpMessage = "Places a RoadBlock tool in player inventory.";
		}
		if (command.equalsIgnoreCase("help")) {
			helpMessage = "Displays help for RoadBlock commands.";
		}
		sender.sendMessage(helpColor + helpMessage);
		displayUsage(sender, command);
		return true;
	}


	/**
	 * Display command usage
	 *
	 * @param sender        the command sender
	 * @param passedCommand the command for which to display usage string
	 */
	private void displayUsage(final CommandSender sender, final String passedCommand) {

		String command = passedCommand;

		if (command.isEmpty() || command.equalsIgnoreCase("help")) {
			command = "all";
		}
		if ((command.equalsIgnoreCase("status")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("roadblock.status")) {
			sender.sendMessage(usageColor + "/roadblock status");
		}
		if ((command.equalsIgnoreCase("reload")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("roadblock.reload")) {
			sender.sendMessage(usageColor + "/roadblock reload");
		}
		if ((command.equalsIgnoreCase("show")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("roadblock.show")) {
			sender.sendMessage(usageColor + "/roadblock show");
		}
		if ((command.equalsIgnoreCase("tool")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("roadblock.tool")) {
			sender.sendMessage(usageColor + "/roadblock tool");
		}
		if ((command.equalsIgnoreCase("help")
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("roadblock.help")) {
			sender.sendMessage(usageColor + "/roadblock help [command]");
		}
	}

}
