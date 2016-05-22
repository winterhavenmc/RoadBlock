package com.winterhaven_mc.roadblock;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.winterhaven_mc.roadblock.storage.DataStoreFactory;
import com.winterhaven_mc.roadblock.utilities.RoadBlockTool;


public final class CommandManager implements CommandExecutor {
	
	private final PluginMain plugin;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public CommandManager(final PluginMain plugin) {
		
		this.plugin = plugin;		
		plugin.getCommand("roadblock").setExecutor(this);
	}

	
	public final boolean onCommand(final CommandSender sender, final Command command, 
			final String label, final String[] args) {

		final int maxArgs = 1;

		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_ARGS_COUNT_OVER");
			plugin.messageManager.playerSound(sender, "COMMAND_FAIL");
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
		
		// tool command
		if (subcommand.equalsIgnoreCase("tool")) {
			return toolCommand(sender);
		}
		
		plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_INVALID_COMMAND");
		plugin.messageManager.playerSound(sender, "COMMAND_FAIL");
		return false;
	}
	
	
	/**
	 * Display plugin status
	 * @param sender
	 * @return
	 */
	private final boolean statusCommand(final CommandSender sender) {
		
		// check that sender has permission for status command
		if (!sender.hasPermission("roadblock.status")) {
			plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_STATUS_PERMISSION");
			plugin.messageManager.playerSound(sender, "COMMAND_FAIL");
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
		
		sender.sendMessage(ChatColor.GREEN + "Spread distance: " 
				+ ChatColor.RESET + plugin.getConfig().getInt("spread-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Mob targeting distance: " 
				+ ChatColor.RESET + plugin.getConfig().getInt("target-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Enabled Worlds: " 
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());
		return true;
	}
	
	
	/**
	 * Reload configuration
	 * @param sender
	 * @return
	 */
	private final boolean reloadCommand(final CommandSender sender) {
		
		// check that sender has permission for reload command
		if (!sender.hasPermission("roadblock.reload")) {
			plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_RELOAD_PERMISSION");
			plugin.messageManager.playerSound(sender, "COMMAND_FAIL");
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
		
		// reload enabled worlds
		plugin.worldManager.reload();
		
		// reload datastore
		DataStoreFactory.reload();
		
		// send player success message
		plugin.messageManager.sendPlayerMessage(sender, "COMMAND_SUCCESS_RELOAD");
		return true;
	}
	
	
	private final boolean toolCommand(final CommandSender sender) {
		
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
			plugin.messageManager.playerSound(player, "COMMAND_FAIL");
			return true;
		}
		
		// create road block tool itemStack
		final ItemStack rbTool = RoadBlockTool.create();
		
		// put tool in player's inventory
		final HashMap<Integer,ItemStack> noFit = player.getInventory().addItem(rbTool);
		
		if (!noFit.isEmpty()) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_TOOL_INVENTORY_FULL");
			plugin.messageManager.playerSound(player, "COMMAND_FAIL");
			return true;
		}
		
		// play success sound
		plugin.messageManager.playerSound(player, "COMMAND_SUCCESS_TOOL");
		return true;
	}

}
