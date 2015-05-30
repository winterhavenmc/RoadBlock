package com.winterhaven_mc.roadblock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.event.world.ChunkLoadEvent;
//import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;


/**
 * Implements player event listener for <code>RoadBlock</code> events.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
class EventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;
	
	HashSet<EntityTargetEvent.TargetReason> cancelReasons;
	
	/**
	 * constructor method for <code>EventListener</code> class
	 * @param	plugin		A reference to this plugin's main class
	 */
	EventListener(PluginMain plugin) {
		
		// reference to main
		this.plugin = plugin;
		
		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		// set entity target cancel reasons
		cancelReasons = new HashSet<EntityTargetEvent.TargetReason>();
		cancelReasons.add(EntityTargetEvent.TargetReason.CLOSEST_PLAYER);
		cancelReasons.add(EntityTargetEvent.TargetReason.RANDOM_TARGET);
		cancelReasons.add(EntityTargetEvent.TargetReason.UNKNOWN);
		
	}


	@EventHandler
	void onPlayerInteract(PlayerInteractEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		Player player = event.getPlayer();

		// if player does not have roadblock.set permission, do nothing and return
		if (!player.hasPermission("roadblock.set")) {
			player.sendMessage("You do not have permission to set road blocks!");
			return;
		}
		
		Block block = event.getClickedBlock();
		ItemStack playerItem = event.getItem();
		
		// if player item is null or air, do nothing and return
		if (playerItem == null || playerItem.getType().equals(Material.AIR)) {
			return;
		}
		
		// if player item is not tool-material, do nothing and return
		if (!playerItem.getType().equals(Material.matchMaterial(plugin.getConfig().getString("tool-material")))) {
			return;
		}
		
		// if player item does not have tool display name, do nothing and return		
		if (!playerItem.hasItemMeta() ||
				!playerItem.getItemMeta().getDisplayName().equals(plugin.messageManager.getToolName())) {
			return;
		}
		
		// cancel event to prevent breaking blocks with roadblock tool
		event.setCancelled(true);

		// if block clicked is not in list of road block materials, send message and return
		if (!plugin.blockManager.roadBlockMaterials.contains(block.getType())) {
			player.sendMessage("Not a valid road block material!");
			return;
		}

		// if clicked block is highlighted, unhighlight all blocks for player
		if (plugin.blockManager.isHighlighted(player, block.getLocation())) {
			plugin.blockManager.unHighlightBlocks(player);
		}
		
		// get road block locations attached to clicked block
		HashSet<Location> locationSet = new HashSet<Location>(plugin.blockManager.spreadLocation(player, block.getLocation()));
		
		if (plugin.debug) {
			plugin.getLogger().info(locationSet.size() + " blocks selected.");
		}
		
		// if right click, protect blocks		
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			
			// highlight blocks
			plugin.blockManager.highlightBlocks(player, locationSet, Material.EMERALD_BLOCK);

			// store blocks
			plugin.blockManager.storeLocations(locationSet);
		}
		
		// if left click, unprotect blocks
		else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {

			// highlight blocks
			plugin.blockManager.highlightBlocks(player, locationSet, Material.REDSTONE_BLOCK);

			// remove blocks from storage
			plugin.blockManager.removeLocations(locationSet);
		}
		
	}
	
	
	/**
	 * Event listener for BlockBreakEvent
	 * @param event
	 */
	@EventHandler
	void onBlockBreak(BlockBreakEvent event) {
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if (plugin.blockManager.isRoadBlock(block)) {
			
			if (!player.hasPermission("roadblock.break")) {
				event.setCancelled(true);
				player.sendMessage("You do not have permission to break road blocks.");
				return;
			}
			plugin.dataStore.deleteRecord(block.getLocation());
			player.sendMessage("Road block protection removed.");
		}
	}
	
	
	/**
	 * Event listener for BlockExplodeEvent
	 * Prevent road blocks from being exploded by block explosions
	 * @param event
	 */
	@EventHandler
	void onBlockExplode(BlockExplodeEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// get list of exploded blocks
		List<Block> blocks = new ArrayList<Block>(event.blockList());
		
		// remove any road blocks from event block list
		for (Block block : blocks) {
			if (plugin.blockManager.isRoadBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}

	
	/**
	 * Event listener for EntityExplodeEvent<br>
	 * Prevent road blocks from being exploded by entity explosions
	 * @param event
	 */
	@EventHandler
	void onEntityExplode(EntityExplodeEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// get list of exploded blocks
		List<Block> blocks = new ArrayList<Block>(event.blockList());
		
		// remove any road blocks from event block list
		for (Block block : blocks) {
			if (plugin.blockManager.isRoadBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}
	
	
	/**
	 * Event listener for EntityChangeBlockEvent<br>
	 * Stop entities from changing road blocks
	 */
	@EventHandler
	void onEntityChangeBlock(EntityChangeBlockEvent event) {
		
		if (plugin.blockManager.isRoadBlock(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	
	/**
	 * Event listener for PlayerQuitEvent<br>
	 * Remove player from highlighted blocks hashmap
	 * @param event
	 */
	@EventHandler
	void onPlayerLogout(PlayerQuitEvent event) {
		
		plugin.blockManager.removePlayerHighlightMap(event.getPlayer());	
	}
	
	
	@EventHandler
	void onPlayerChangeItem(PlayerItemHeldEvent event) {
		
		Player player = event.getPlayer();
		
		ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
		
		if (previousItem != null 
				&& previousItem.getType().equals(Material.GOLD_PICKAXE)) {	
			plugin.blockManager.unHighlightBlocks(player);
		}
	}
	
	@EventHandler
	void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {

		// check that target is a player
		if (event.getTarget() != null && event.getTarget() instanceof Player) {
			
			// get targeted player
			Player player = (Player) event.getTarget();
			
			// check that player is above a road block
			if (plugin.blockManager.isRoadBelowPlayer(player)) {
				
				// if entity to target distance is less than 
				// configured target distance (default 5 blocks),
				// do nothing and return, allowing player to be targeted 
				if (event.getEntity().getLocation()
						.distanceSquared(player.getLocation()) < Math.pow(plugin
						.getConfig().getInt("target-distance"),2)) {
					return;
				}
				
				// get target reason
				EntityTargetEvent.TargetReason reason = event.getReason();
				
				// if reason is in cancelReasons list, cancel event
				if (cancelReasons.contains(reason)) {
					event.setCancelled(true);
				}
			}
		}
	}
	

	/**
	 * Item drop event handler
	 * @param event
	 */
	@EventHandler
	void onItemDrop(PlayerDropItemEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// get dropped item
		ItemStack droppedItem = event.getItemDrop().getItemStack();
		
		// get configured tool material
		Material toolMaterial = Material.matchMaterial(plugin.getConfig().getString("tool-material"));
		
		// if configured tool material does not match a material, set to gold pickaxe
		if (toolMaterial == null) {
			toolMaterial = Material.GOLD_PICKAXE;
		}
		
		// if dropped item is not tool-material, do nothing and return 
		if (!droppedItem.getType().equals(toolMaterial)) {
			return;
		}
		
		// if dropped item does not have configured metadata, do nothing and return
		if (!droppedItem.hasItemMeta() 
			|| !droppedItem.getItemMeta().getDisplayName().equals(plugin.messageManager.getToolName())) {
			return;
		}
		
		// remove dropped item
		event.getItemDrop().remove();
		
		// if sound effects enabled, play item_break sound to player
		if (plugin.getConfig().getBoolean("sound-effects")) {
			Player player = event.getPlayer();
			player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
		}
	}

}
