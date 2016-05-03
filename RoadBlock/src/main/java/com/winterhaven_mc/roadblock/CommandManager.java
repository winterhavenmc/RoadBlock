package com.winterhaven_mc.roadblock;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandManager implements CommandExecutor {
	
	private PluginMain plugin;

	public CommandManager(PluginMain plugin) {
		
		this.plugin = plugin;		
		plugin.getCommand("roadblock").setExecutor(this);
	}

	public boolean onCommand(final CommandSender sender, final Command command, 
			final String label, final String[] args) {


		int maxArgs = 1;

		if (args.length > maxArgs) {
			plugin.messageManager.sendPlayerMessage(sender, "COMMAND_FAIL_ARGS_COUNT_OVER");
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
	boolean statusCommand(final CommandSender sender) {
		
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
				+ ChatColor.RESET + plugin.blockManager.getEnabledWorlds().toString());
		return true;
	}
	
	
	/**
	 * Reload configuration
	 * @param sender
	 * @return
	 */
	boolean reloadCommand(final CommandSender sender) {
		
		// reload config file
		plugin.reloadConfig();
		
		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");
		
		// update profile field
		plugin.profile = plugin.getConfig().getBoolean("profile");
		
		// update enabledWorlds list
		plugin.blockManager.updateEnabledWorlds();
		
		// update road block materials list
		plugin.blockManager.updateMaterials();
		
		// reload messages
		plugin.messageManager.reload();
		
		// reload enabled worlds
		plugin.blockManager.updateEnabledWorlds();
		
		// reload datastore
		DataStoreFactory.reload();
		
		// send player success message
		plugin.messageManager.sendPlayerMessage(sender, "COMMAND_SUCCESS_RELOAD");
		return true;
	}
	
	
	boolean toolCommand(final CommandSender sender) {
		
		// sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_CONSOLE");
			return true;
		}
		
		// get player from sender
		Player player = (Player) sender;
		
		// check player permissions
		if (!player.hasPermission("roadblock.tool")) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_TOOL_PERMISSION");
			return true;
		}
		
		// create road block tool itemStack
		ItemStack rbTool = RoadBlockTool.create();
		
		// put tool in player's inventory
		HashMap<Integer,ItemStack> noFit = player.getInventory().addItem(rbTool);
		
		if (!noFit.isEmpty()) {
			plugin.messageManager.sendPlayerMessage(sender,"COMMAND_FAIL_TOOL_INVENTORY_FULL");
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			return true;
		}
		
		// if sound effects enabled, play sound for player
		if (plugin.getConfig().getBoolean("sound-effects")) {
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
		}
		return true;
	}

}
