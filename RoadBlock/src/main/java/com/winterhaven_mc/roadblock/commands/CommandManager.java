package com.winterhaven_mc.roadblock.commands;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.highlights.HighlightStyle;
import com.winterhaven_mc.roadblock.storage.DataStoreFactory;
import com.winterhaven_mc.roadblock.utilities.RoadBlockTool;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Set;


public final class CommandManager implements CommandExecutor {
	
	private final PluginMain plugin;

	
	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	public CommandManager(final PluginMain plugin) {
		
		this.plugin = plugin;		
		plugin.getCommand("roadblock").setExecutor(this);
	}


	@Override
	public final boolean onCommand(final CommandSender sender, final Command command, 
			final String label, final String[] args) {

		final int maxArgs = 2;

		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_ARGS_COUNT_OVER");
			plugin.soundManager.playerSound(sender, "COMMAND_FAIL");
			return false;
		}
		
		String subcommand = "status";
		
		if (args.length > 0) {
			subcommand = args[0];
		}
		
		// status command
		if (subcommand.equalsIgnoreCase("status")) {			
			return statusCommand(sender);
		}
		
		// reload command
		if (subcommand.equalsIgnoreCase("reload")) {
			return reloadCommand(sender);
		}

		// show command
		if (subcommand.equalsIgnoreCase("show")) {
			return showCommand(sender, args);
		}

		// tool command
		if (subcommand.equalsIgnoreCase("tool")) {
			return toolCommand(sender);
		}
		
		plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_INVALID_COMMAND");
		plugin.soundManager.playerSound(sender, "COMMAND_FAIL");
		return false;
	}
	
	
	/**
	 * Display plugin status
	 * @param sender the command sender
	 * @return always returns {@code true}, to prevent usage message
	 */
	private boolean statusCommand(final CommandSender sender) {
		
		// check that sender has permission for status command
		if (!sender.hasPermission("roadblock.status")) {
			plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_STATUS_PERMISSION");
			plugin.soundManager.playerSound(sender, "COMMAND_FAIL");
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

		sender.sendMessage(ChatColor.GREEN + "No place height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("no-place-height") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Player on road height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("on-road-height") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Mob targeting distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("target-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " 
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());
		return true;
	}
	
	
	/**
	 * Reload configuration
	 * @param sender the command sender
	 * @return always returns {@code true}, to prevent usage message
	 */
	private boolean reloadCommand(final CommandSender sender) {
		
		// check that sender has permission for reload command
		if (!sender.hasPermission("roadblock.reload")) {
			plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_RELOAD_PERMISSION");
			plugin.soundManager.playerSound(sender, "COMMAND_FAIL");
			return true;
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
		plugin.blockManager.updateMaterials();
		
		// reload messages
		plugin.messageManager.reload();

		// reload sounds
		plugin.soundManager.reload();
		
		// reload enabled worlds
		plugin.worldManager.reload();
		
		// reload datastore
		DataStoreFactory.reload();
		
		// send player success message
		plugin.messageManager.sendPlayerMessage(sender, "COMMAND_SUCCESS_RELOAD");
		return true;
	}


	/**
	 * Highlight blocks that are within specified distance of player location
	 * @param sender the command sender
	 * @param args command arguments
	 * @return always returns {@code true}, to prevent usage message
	 */
	private boolean showCommand(final CommandSender sender, String[] args) {

		// sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_CONSOLE");
			return true;
		}

		// get player from sender
		final Player player = (Player) sender;

		// check player permissions
		if (!player.hasPermission("roadblock.show")) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_SHOW_PERMISSION");
			plugin.soundManager.playerSound(player, "COMMAND_FAIL");
			return true;
		}

		// argument limits
		int minArgs = 1;
		int maxArgs = 2;

		// check min arguments
		if (args.length < minArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_ARGS_COUNT_UNDER");
			return false;
		}

		// check max arguments
		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_ARGS_COUNT_OVER");
			return false;
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
				plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_SET_INVALID_INTEGER");

				player.sendMessage("§6/roadblock show <distance>");
				return true;
			}
		}

		// get set of block locations within distance of player location
		Set<Location> locations = plugin.blockManager.selectNearbyBlocks(player.getLocation(), distance);

		// highlight blocks
		plugin.highlightManager.highlightBlocks(player, locations, HighlightStyle.PROTECT);

		// send player success message
		plugin.messageManager.sendPlayerMessage(player,"COMMAND_SUCCESS_SHOW", locations.size());

		return true;
	}


	/**
	 * Place a tool in player inventory
	 * @param sender the command sender
	 * @return always returns {@code true}, to prevent usage message
     */
	private boolean toolCommand(final CommandSender sender) {
		
		// sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_CONSOLE");
			return true;
		}
		
		// get player from sender
		final Player player = (Player) sender;
		
		// check player permissions
		if (!player.hasPermission("roadblock.tool")) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_TOOL_PERMISSION");
			plugin.soundManager.playerSound(player, "COMMAND_FAIL");
			return true;
		}
		
		// create road block tool itemStack
		final ItemStack rbTool = RoadBlockTool.create();
		
		// put tool in player's inventory
		final HashMap<Integer,ItemStack> noFit = player.getInventory().addItem(rbTool);
		
		if (!noFit.isEmpty()) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_TOOL_INVENTORY_FULL");
			plugin.soundManager.playerSound(player, "COMMAND_FAIL");
			return true;
		}
		
		// play success sound
		plugin.soundManager.playerSound(player, "COMMAND_SUCCESS_TOOL");
		return true;
	}

}
