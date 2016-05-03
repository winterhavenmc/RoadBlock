package com.winterhaven_mc.roadblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
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
	
	// set entity target cancel reasons
	private static final Set<EntityTargetEvent.TargetReason> cancelReasons =
			Collections.unmodifiableSet(new HashSet<EntityTargetEvent.TargetReason>(Arrays.asList(
					EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
					EntityTargetEvent.TargetReason.RANDOM_TARGET,
					EntityTargetEvent.TargetReason.UNKNOWN
				)));
	
	/**
	 * constructor method for <code>EventListener</code> class
	 * @param	plugin		A reference to this plugin's main class
	 */
	EventListener(final PluginMain plugin) {
		
		// reference to main
		this.plugin = plugin;
		
		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	@EventHandler
	void onPlayerInteract(final PlayerInteractEvent event) {
		
		//NOTE: do not check for cancelled event here
		
		Player player = event.getPlayer();

		ItemStack playerItem = event.getItem();
		
		Action action = event.getAction();
		
		// get clicked block
		Block clickedBlock = event.getClickedBlock();
		
		// if event is air/block click with RoadBlock tool, begin tool use procedure
		if (RoadBlockTool.isTool(playerItem) && !action.equals(Action.PHYSICAL)) {

			// if world is not enabled, send message and return
			if (!plugin.blockManager.worldEnabled(player.getWorld())) {
				plugin.messageManager.sendPlayerMessage(event.getPlayer(), "TOOL_FAIL_WORLD_DISABLED");
				return;
			}
			
			// if clicked block is tool transparent material, try to find non-air block along line of sight
			if (clickedBlock == null || RoadBlockTool.toolTransparentMaterials.contains(clickedBlock.getType())) {
				
				// RH says this can sometimes throw an exception, so using try..catch block
				try {
					clickedBlock = player.getTargetBlock(RoadBlockTool.toolTransparentMaterials, 100);
				} catch (Exception e) {
					plugin.getLogger().info("player.getTargetBlock() threw an exception.");
					plugin.getLogger().info(e.getLocalizedMessage());
				}
			}

			// if no block detected, stop here
			if (clickedBlock == null) {
				return;
			}

			// if clicked block is air, the actual clicked block was too far away
			if (clickedBlock.getType().equals(Material.AIR)) {
				plugin.messageManager.sendPlayerMessage(player, "TOOL_FAIL_DISTANCE_EXCEEDED");
				return;
			}
			
			// cancel event to prevent breaking blocks with road block tool
			event.setCancelled(true);
			
			// if tool is in off-hand, do nothing and return
			if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
				return;
			}

			// if player does not have roadblock.set permission, do nothing and return
			if (!player.hasPermission("roadblock.set")) {
				plugin.messageManager.sendPlayerMessage(player, "TOOL_FAIL_USE_PERMISSION");
				return;
			}

			// if block clicked is not in list of road block materials, send message and return
			if (!plugin.blockManager.getRoadBlockMaterials().contains(clickedBlock.getType())) {
				plugin.messageManager.sendPlayerMessage(player, "TOOL_FAIL_INVALID_MATERIAL",clickedBlock.getType());
				return;
			}

			// if clicked block is highlighted, unhighlight all blocks for player
			if (plugin.blockManager.isHighlighted(player, clickedBlock.getLocation())) {
				plugin.blockManager.unHighlightBlocks(player);
			}

			// get road block locations attached to clicked block
			HashSet<Location> locationSet = 
					new HashSet<Location>(plugin.blockManager.getFill(clickedBlock.getLocation()));

			int quantity = locationSet.size();

			// if right click, protect blocks		
			if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {

				// highlight blocks
				plugin.blockManager.highlightBlocks(player, locationSet, Material.EMERALD_BLOCK);

				// store blocks
				plugin.blockManager.storeLocations(locationSet);

				// send player successful protect message
				plugin.messageManager.sendPlayerMessage(player, "TOOL_SUCCESS_PROTECT",quantity);
			}

			// if left click, unprotect blocks
			else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)) {

				// highlight blocks
				plugin.blockManager.highlightBlocks(player, locationSet, Material.REDSTONE_BLOCK);

				// remove blocks from storage
				plugin.blockManager.removeLocations(locationSet);

				// send player successful unprotect message
				plugin.messageManager.sendPlayerMessage(player, "TOOL_SUCCESS_UNPROTECT",quantity);
			}
		}
	}
	
	
	/**
	 * Event handler for PlayerItemHeldEvent<br>
	 * Unhighlight blocks when player changes held item from road block tool
	 * @param event
	 */
	@EventHandler
	void onPlayerChangeItem(final PlayerItemHeldEvent event) {
		
		Player player = event.getPlayer();
		
		ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
		
		if (previousItem != null 
				&& previousItem.getType().equals(Material.GOLD_PICKAXE)) {	
			plugin.blockManager.unHighlightBlocks(player);
		}
	}


	/**
	 * Event handler for PlayerGameModeChangeEvent<br>
	 * Unhighlight blocks when player changes gamemode
	 * @param event
	 */
	@EventHandler
	void onPlayerChangeGameMode(final PlayerGameModeChangeEvent event) {
		
		plugin.blockManager.unHighlightBlocks(event.getPlayer());
	}


	/**
	 * Item drop event handler
	 * @param event
	 */
	@EventHandler
	void onPlayerDropItem(final PlayerDropItemEvent event) {
		
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
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
		}
	}


	/**
	 * Event listener for PlayerQuitEvent<br>
	 * Remove player from highlighted blocks hashmap
	 * @param event
	 */
	@EventHandler
	void onPlayerLogout(final PlayerQuitEvent event) {
		
		plugin.blockManager.removePlayerHighlightMap(event.getPlayer());	
	}


	/**
	 * Event listener for BlockBreakEvent
	 * @param event
	 */
	@EventHandler
	void onBlockBreak(final BlockBreakEvent event) {
		
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if (plugin.blockManager.isRoadBlock(block)) {
			
			if (!player.hasPermission("roadblock.break")) {
				event.setCancelled(true);
				plugin.messageManager.sendPlayerMessage(player, "TOOL_FAIL_USE_BLOCK_BREAK_PERMISSION");
				return;
			}
			plugin.dataStore.deleteRecord(block.getLocation());
			plugin.messageManager.sendPlayerMessage(player, "TOOL_SUCCESS_BREAK_BLOCK");
			player.sendMessage("Road block protection removed.");
		}
	}
	
	
	/**
	 * Event listener for BlockExplodeEvent
	 * Prevent road blocks from being exploded by block explosions
	 * @param event
	 */
	@EventHandler
	void onBlockExplode(final BlockExplodeEvent event) {
		
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
	void onEntityExplode(final EntityExplodeEvent event) {
		
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
	void onEntityChangeBlock(final EntityChangeBlockEvent event) {
		
		if (plugin.blockManager.isRoadBlock(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	
	@EventHandler
	void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event) {

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
	 * Piston extend event handler<br>
	 * Prevent extending pistons from effecting road blocks
	 * @param event
	 */
	@EventHandler
	void onPistonExtend(final BlockPistonExtendEvent event) {

		// get list of blocks affected by piston 
		ArrayList<Block> blocks = new ArrayList<Block>(event.getBlocks());
		
		// iterate through block list checking for road blocks
		for (Block block : blocks) {
			
			// if block is a death chest, cancel event
			if (plugin.blockManager.isRoadBlock(block)) {
				event.setCancelled(true);
				
				// break the piston
				event.getBlock().breakNaturally();
			}
		}
	}

	
	/**
	 * Piston extend event handler<br>
	 * Prevent extending pistons from effecting death chests
	 * @param event
	 */
	@EventHandler
	void onPistonRetract(final BlockPistonRetractEvent event) {
		
		// get list of blocks affected by piston 
		ArrayList<Block> blocks = new ArrayList<Block>(event.getBlocks());
		
		// iterate through block list checking for road blocks
		for (Block block : blocks) {
			
			// if block is a death chest, cancel event
			if (plugin.blockManager.isRoadBlock(block)) {
				event.setCancelled(true);
				
				// break the piston
				event.getBlock().breakNaturally();
			}
		}
	}

}
