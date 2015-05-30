package com.winterhaven_mc.roadblock;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandManager implements CommandExecutor {
	
	private PluginMain plugin;
	private final String pluginName;

	public CommandManager(PluginMain plugin) {
		
		this.plugin = plugin;		
		plugin.getCommand("roadblock").setExecutor(this);
		pluginName = "[" + this.plugin.getName() + "] ";
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {


		int maxArgs = 1;

		if (args.length > maxArgs) {
			sender.sendMessage(ChatColor.RED + pluginName + "Too many arguments.");
			return false;
		}
		
		String subcmd = "";
		
		// if no arguments passed, set subcmd to status
		if (args.length < 1) {	
			subcmd = "status";
		}
		else {
			subcmd = args[0];
		}
		
		// status command
		if (subcmd.equalsIgnoreCase("status")) {			
			return statusCommand(sender);
		}
		
		// reload command
		if (subcmd.equalsIgnoreCase("reload")) {
			return reloadCommand(sender);
		}
		
		// tool command
		if (subcmd.equalsIgnoreCase("tool")) {
			return toolCommand(sender);
		}
		return false;
	}
	
	
	/**
	 * Display plugin status
	 * @param sender
	 * @return
	 */
	boolean statusCommand(CommandSender sender) {
		
		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_AQUA + pluginName + ChatColor.AQUA + "Version: " 
				+ ChatColor.RESET + versionString);
		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
		if (plugin.profile) {
			sender.sendMessage(ChatColor.DARK_RED + "PROFILE: true");
		}
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
	boolean reloadCommand(CommandSender sender) {
		
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
		
		// reload datastore
		DataStoreFactory.reload();
		
		sender.sendMessage(ChatColor.DARK_AQUA + pluginName 
				+ ChatColor.AQUA + "Configuration reloaded.");
		return true;
	}
	
	
	boolean toolCommand(CommandSender sender) {
		
		// sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-console");
			return true;
		}
		
		Player player = (Player) sender;
		
		// check player permissions
		if (!player.hasPermission("roadblock.tool")) {
			plugin.messageManager.sendPlayerMessage(sender,"permission-denied-tool");
			return true;
		}
		
		// create itemStack for tool
		ItemStack rbTool = new ItemStack(Material.GOLD_PICKAXE);
		
		// set tool display name and lore
		ItemMeta metaData = rbTool.getItemMeta();
		metaData.setDisplayName(plugin.messageManager.getToolName());
		metaData.setLore(plugin.messageManager.getToolLore());
		rbTool.setItemMeta(metaData);

		// put tool in player's inventory
		HashMap<Integer,ItemStack> noFit = player.getInventory().addItem(rbTool);
		
		if (!noFit.isEmpty()) {
			plugin.messageManager.sendPlayerMessage(sender,"command-fail-tool-inventory-full");
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
			return true;
		}
		// if sound effects enabled, play item_break sound to player
		if (plugin.getConfig().getBoolean("sound-effects")) {
			player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1, 1);
		}
		return true;
	}

}
