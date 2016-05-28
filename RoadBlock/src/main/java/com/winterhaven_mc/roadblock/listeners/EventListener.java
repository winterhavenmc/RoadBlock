package com.winterhaven_mc.roadblock.listeners;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.highlights.HighlightStyle;
import com.winterhaven_mc.roadblock.utilities.RoadBlockTool;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;


/**
 * Implements player event listener for <code>RoadBlock</code> events.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public final class EventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;
	
	// set entity target cancel reasons
	private static final Set<EntityTargetEvent.TargetReason> cancelReasons =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
					EntityTargetEvent.TargetReason.RANDOM_TARGET,
					EntityTargetEvent.TargetReason.UNKNOWN
			)));
	
	/**
	 * constructor method for <code>EventListener</code> class
	 * @param	plugin		A reference to this plugin's main class
	 */
	public EventListener(final PluginMain plugin) {
		
		// reference to main
		this.plugin = plugin;
		
		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	@EventHandler
	final void onPlayerInteract(final PlayerInteractEvent event) {
		
		//NOTE: do not check for cancelled event here
		
		final Player player = event.getPlayer();

		final ItemStack playerItem = event.getItem();
		
		final Action action = event.getAction();
		
		// get clicked block
		Block clickedBlock = event.getClickedBlock();
		
		// if event is air/block click with RoadBlock tool, begin tool use procedure
		if (RoadBlockTool.isTool(playerItem) && !action.equals(Action.PHYSICAL)) {

			// if world is not enabled, send message and return
			if (!plugin.worldManager.isEnabled(player.getWorld())) {
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

			// FOR 1.9 ONLY
			//			// if tool is in off-hand, do nothing and return
			//			if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
			//				return;
			//			}

			// if player does not have roadblock.set permission, do nothing and return
			if (!player.hasPermission("roadblock.set")) {
				plugin.messageManager.sendPlayerMessage(player, "TOOL_FAIL_USE_PERMISSION");
				plugin.messageManager.playerSound(player, "TOOL_FAIL_USE_PERMISSION");
				return;
			}

			// if block clicked is not in list of road block materials, send message and return
			if (!plugin.blockManager.getRoadBlockMaterials().contains(clickedBlock.getType())) {
				plugin.messageManager.sendPlayerMessage(player, "TOOL_FAIL_INVALID_MATERIAL",clickedBlock.getType());
				plugin.messageManager.playerSound(player, "TOOL_FAIL_INVALID_MATERIAL");
				return;
			}

			// get road block locations attached to clicked block
			HashSet<Location> locationSet = 
					new HashSet<>(plugin.blockManager.getFill(clickedBlock.getLocation()));

			int quantity = locationSet.size();

			// if right click, protect blocks		
			if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {

				// highlight blocks
				plugin.highlightManager.highlightBlocks(player,locationSet,HighlightStyle.PROTECT);

				// store blocks
				plugin.blockManager.storeLocations(locationSet);

				// send player successful protect message
				plugin.messageManager.sendPlayerMessage(player, "TOOL_SUCCESS_PROTECT",quantity);
				plugin.messageManager.playerSound(player, "TOOL_SUCCESS_PROTECT");
			}

			// if left click, unprotect blocks
			else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)) {

				// highlight blocks
				plugin.highlightManager.highlightBlocks(player,locationSet,HighlightStyle.UNPROTECT);

				// remove blocks from storage
				plugin.blockManager.removeLocations(locationSet);

				// send player successful unprotect message
				plugin.messageManager.sendPlayerMessage(player, "TOOL_SUCCESS_UNPROTECT",quantity);
				plugin.messageManager.playerSound(player, "TOOL_SUCCESS_UNPROTECT");
			}
		}
	}
	
	
	/**
	 * Event handler for PlayerItemHeldEvent<br>
	 * Unhighlight blocks when player changes held item from road block tool
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onPlayerChangeItem(final PlayerItemHeldEvent event) {
		
		final Player player = event.getPlayer();
		
		final ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());
		
		if (previousItem != null 
				&& RoadBlockTool.isTool(previousItem)) {
			plugin.highlightManager.unHighlightBlocks(player);
		}
	}


	/**
	 * Event handler for PlayerGameModeChangeEvent<br>
	 * Unhighlight blocks when player changes gamemode
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onPlayerChangeGameMode(final PlayerGameModeChangeEvent event) {
		plugin.highlightManager.unHighlightBlocks(event.getPlayer());
	}


	/**
	 * Item drop event handler
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onPlayerDropItem(final PlayerDropItemEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// get dropped item
		final ItemStack droppedItem = event.getItemDrop().getItemStack();
		
		// if dropped item is not a road block tool, do nothing and return
		if (!RoadBlockTool.isTool(droppedItem)) {
			return;
		}
		
		// remove dropped item
		event.getItemDrop().remove();
		
		// tool drop sound to player
		if (plugin.getConfig().getBoolean("sound-effects")) {
			Player player = event.getPlayer();
			plugin.messageManager.playerSound(player, "TOOL_DROP");
		}
	}


	/**
	 * Event listener for PlayerQuitEvent<br>
	 * Remove player from highlighted blocks hashmap
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onPlayerQuit(final PlayerQuitEvent event) {
		
		// remove player from highlight map
		plugin.highlightManager.removePlayerFromMap(event.getPlayer());	
	}


	/**
	 * Event listener for BlockBreakEvent
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onBlockBreak(final BlockBreakEvent event) {
		
		final Block block = event.getBlock();
		final Player player = event.getPlayer();
		
		if (plugin.blockManager.isRoadBlock(block)) {
			
			if (!player.hasPermission("roadblock.break")) {
				event.setCancelled(true);
				plugin.messageManager.sendPlayerMessage(player, "TOOL_FAIL_USE_BLOCK_BREAK_PERMISSION");
				return;
			}
			plugin.blockManager.removeLocation(block.getLocation());
			plugin.messageManager.sendPlayerMessage(player, "TOOL_SUCCESS_BREAK_BLOCK");
			player.sendMessage("Road block protection removed.");
		}
	}
	
	
	/**
	 * Event listener for BlockExplodeEvent
	 * Prevent road blocks from being exploded by block explosions
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onBlockExplode(final BlockExplodeEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}
		
		// get list of exploded blocks
		final List<Block> blocks = new ArrayList<>(event.blockList());
		
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
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onEntityExplode(final EntityExplodeEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// get list of exploded blocks
		final List<Block> blocks = new ArrayList<>(event.blockList());
		
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
	final void onEntityChangeBlock(final EntityChangeBlockEvent event) {
		
		// if event block is a RoadBlock, cancel event
		if (plugin.blockManager.isRoadBlock(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	
	@EventHandler
	final void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event) {

		// check that target is a player
		if (event.getTarget() != null && event.getTarget() instanceof Player) {
			
			// get targeted player
			final Player player = (Player) event.getTarget();
			
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
				final EntityTargetEvent.TargetReason reason = event.getReason();
				
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
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onPistonExtend(final BlockPistonExtendEvent event) {

		// get list of blocks affected by piston 
		final ArrayList<Block> blocks = new ArrayList<>(event.getBlocks());
		
		// iterate through block list checking for road blocks
		for (Block block : blocks) {
			
			// if block is a road block, cancel event and break piston
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
	 * @param event the event handled by this method
	 */
	@EventHandler
	final void onPistonRetract(final BlockPistonRetractEvent event) {
		
		// get list of blocks affected by piston 
		final ArrayList<Block> blocks = new ArrayList<>(event.getBlocks());
		
		// iterate through block list checking for road blocks
		for (Block block : blocks) {
			
			// if block is a road block, cancel event and break piston
			if (plugin.blockManager.isRoadBlock(block)) {
				event.setCancelled(true);
				
				// break the piston
				event.getBlock().breakNaturally();
			}
		}
	}

}
