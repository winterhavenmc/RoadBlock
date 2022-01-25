package com.winterhavenmc.roadblock.listeners;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.highlights.HighlightStyle;
import com.winterhavenmc.roadblock.sounds.SoundId;
import com.winterhavenmc.roadblock.storage.BlockRecord;
import com.winterhavenmc.roadblock.util.RoadBlockTool;

import com.winterhavenmc.roadblock.messages.Macro;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.winterhavenmc.roadblock.messages.MessageId.*;


/**
 * Implements player event listeners for RoadBlock events.
 */
public final class EventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// static set of entity target cancel reasons
	private static final Set<EntityTargetEvent.TargetReason> cancelReasons = Set.of(
					EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
					EntityTargetEvent.TargetReason.RANDOM_TARGET,
					EntityTargetEvent.TargetReason.UNKNOWN );

	private final Set<String> pathMaterialNames = Set.of(
			"GRASS_PATH",
			"LEGACY_GRASS_PATH",
			"DIRT_PATH" );


	/**
	 * Class constructor for EventListener class
	 *
	 * @param plugin reference to this plugin's main class
	 */
	public EventListener(final PluginMain plugin) {

		// reference to main
		this.plugin = plugin;

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Event handler for PlayerInteractEvent;
	 * handles blocks clicked with RoadBlock tool
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerInteract(final PlayerInteractEvent event) {

		//NOTE: do not check for cancelled event here; long distance clicks are considered cancelled

		final Player player = event.getPlayer();

		final ItemStack playerItem = event.getItem();

		final Action action = event.getAction();

		// get clicked block
		Block clickedBlock = event.getClickedBlock();

		// if event is air/block click with RoadBlock tool, begin tool use procedure
		if (RoadBlockTool.isTool(playerItem) && !action.equals(Action.PHYSICAL)) {

			// if world is not enabled, send message and return
			if (!plugin.worldManager.isEnabled(player.getWorld())) {
				plugin.messageBuilder.build(player, TOOL_FAIL_WORLD_DISABLED)
						.setMacro(Macro.WORLD, player.getWorld())
						.send();
				event.setCancelled(true);
				return;
			}

			// if clicked block is tool transparent material, try to find non-air block along line of sight
			if (clickedBlock == null || RoadBlockTool.toolTransparentMaterials.contains(clickedBlock.getType())) {

				// RH says this can sometimes throw an exception, so using try...catch block
				try {
					clickedBlock = player.getTargetBlock(RoadBlockTool.toolTransparentMaterials, 100);
				}
				catch (Exception e) {
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
				plugin.messageBuilder.build(player, TOOL_FAIL_DISTANCE_EXCEEDED).send();
				return;
			}

			// cancel event to prevent breaking blocks with road block tool
			event.setCancelled(true);

			// if player does not have roadblock.set permission, do nothing and return
			if (!player.hasPermission("roadblock.set")) {
				plugin.messageBuilder.build(player, TOOL_FAIL_USE_PERMISSION).send();
				plugin.soundConfig.playSound(player, SoundId.TOOL_FAIL_USE_PERMISSION);
				return;
			}

			// if block clicked is not in list of road block materials, send message and return
			if (!plugin.blockManager.getRoadBlockMaterials().contains(clickedBlock.getType())) {
				plugin.messageBuilder.build(player, TOOL_FAIL_INVALID_MATERIAL)
						.setMacro(Macro.MATERIAL, clickedBlock.getType())
						.send();
				plugin.soundConfig.playSound(player, SoundId.TOOL_FAIL_INVALID_MATERIAL);
				return;
			}

			// get road block locations attached to clicked block
			final HashSet<Location> locationSet =
					new HashSet<>(plugin.blockManager.getFill(clickedBlock.getLocation()));

			final int quantity = locationSet.size();

			// create set of location records from locationSet
			Set<BlockRecord> blockRecords = new HashSet<>();
			for (Location location : locationSet) {
				blockRecords.add(new BlockRecord(location));
			}

			// if right click, protect blocks
			if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR)) {

				// highlight blocks
				plugin.highlightManager.highlightBlocks(player, locationSet, HighlightStyle.PROTECT);

				// store blocks
				plugin.blockManager.storeLocations(blockRecords);

				// send player successful protect message
				plugin.messageBuilder.build(player, TOOL_SUCCESS_PROTECT)
						.setMacro(Macro.QUANTITY, quantity)
						.send();
				plugin.soundConfig.playSound(player, SoundId.TOOL_SUCCESS_PROTECT);
			}

			// if left click, unprotect blocks
			else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR)) {

				// highlight blocks
				plugin.highlightManager.highlightBlocks(player, locationSet, HighlightStyle.UNPROTECT);

				// remove blocks from storage
				plugin.blockManager.removeLocations(blockRecords);

				// send player successful unprotect message
				plugin.messageBuilder.build(player, TOOL_SUCCESS_UNPROTECT)
						.setMacro(Macro.QUANTITY, quantity)
						.send();
				plugin.soundConfig.playSound(player, SoundId.TOOL_SUCCESS_UNPROTECT);
			}
		}
	}


	/**
	 * Event handler for PlayerItemHeldEvent;
	 * unhighlights blocks when player changes held item from road block tool
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerChangeItem(final PlayerItemHeldEvent event) {

		final Player player = event.getPlayer();

		final ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());

		if (RoadBlockTool.isTool(previousItem)) {
			plugin.highlightManager.unHighlightBlocks(player);
		}
	}


	/**
	 * Event handler for PlayerGameModeChangeEvent;
	 * unhighlights blocks when player changes gamemode
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerChangeGameMode(final PlayerGameModeChangeEvent event) {
		plugin.highlightManager.unHighlightBlocks(event.getPlayer());
	}


	/**
	 * Event handler for PlayerDropItemEvent;
	 * removes custom tool from game when dropped
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onPlayerDropItem(final PlayerDropItemEvent event) {

		// get dropped item
		final ItemStack droppedItem = event.getItemDrop().getItemStack();

		// if dropped item is not a road block tool, do nothing and return
		if (!RoadBlockTool.isTool(droppedItem)) {
			return;
		}

		// remove dropped item
		event.getItemDrop().remove();

		// play tool drop sound for player
		plugin.soundConfig.playSound(event.getPlayer(), SoundId.TOOL_DROP);
	}


	/**
	 * Event handler for BlockPlaceEvent;
	 * prevents placing blocks on top of road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockPlace(final BlockPlaceEvent event) {

		// get configured no-place-height
		final int height = plugin.getConfig().getInt("no-place-height");

		// get block placed
		final Block placedBlock = event.getBlockPlaced();

		// get player
		final Player player = event.getPlayer();

		// check if block below placed block is protected grass path, to prevent converting to regular dirt
		final Block blockBelow = placedBlock.getRelative(BlockFace.DOWN);
		if (pathMaterialNames.contains(blockBelow.getType().toString()) && plugin.blockManager.isRoadBlock(blockBelow)) {
			event.setCancelled(true);
			plugin.messageBuilder.build(player, BLOCK_PLACE_FAIL_GRASS_PATH).send();
			plugin.soundConfig.playSound(player, SoundId.BLOCK_PLACE_FAIL_GRASS_PATH);
			return;
		}

		// check if block placed is configured distance above a road block
		if (plugin.blockManager.isAboveRoad(placedBlock.getLocation(), height)) {
			event.setCancelled(true);
			plugin.messageBuilder.build(player, BLOCK_PLACE_FAIL_ABOVE_ROAD).send();
			plugin.soundConfig.playSound(player, SoundId.BLOCK_PLACE_FAIL_ABOVE_ROAD);
		}
	}


	/**
	 * Event handler for BlockMultiPlaceEvent;
	 * prevents placing blocks on top of road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockMultiPlace(final BlockMultiPlaceEvent event) {

		// get configured no-place-height
		final int height = plugin.getConfig().getInt("no-place-height");

		// get list of blocks that will be replaced
		final List<BlockState> replacedBlocks = event.getReplacedBlockStates();

		// get event player
		final Player player = event.getPlayer();

		// iterate through blocks and check if any are above a road block
		for (BlockState blockState : replacedBlocks) {

			// if block is above a road block, cancel event and send player message
			if (plugin.blockManager.isAboveRoad(blockState.getLocation(), height)) {
				event.setCancelled(true);
				plugin.messageBuilder.build(player, BLOCK_PLACE_FAIL_ABOVE_ROAD).send();
				plugin.soundConfig.playSound(player, SoundId.BLOCK_PLACE_FAIL_ABOVE_ROAD);
				break;
			}
		}
	}


	/**
	 * Event handler for BlockBreakEvent;
	 * prevents breaking protected road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockBreak(final BlockBreakEvent event) {

		// get block being broken
		final Block block = event.getBlock();

		// get player
		final Player player = event.getPlayer();

		// check if block is a protected road block
		if (plugin.blockManager.isRoadBlock(block)) {

			// if player does not have override permission, cancel event and send player message
			if (!player.hasPermission("roadblock.break")) {
				event.setCancelled(true);
				plugin.messageBuilder.build(player, TOOL_FAIL_USE_BLOCK_BREAK_PERMISSION).send();
				return;
			}

			// player does have override permission; remove protection from block and send player message
			plugin.blockManager.removeLocation(new BlockRecord(block.getLocation()));
			plugin.messageBuilder.build(player, TOOL_SUCCESS_BREAK_BLOCK).send();
		}
	}


	/**
	 * Event handler for BlockExplodeEvent;
	 * prevents protected road blocks from being destroyed by block explosions
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockExplode(final BlockExplodeEvent event) {

		// get collection of exploded blocks
		final Collection<Block> blocks = new ArrayList<>(event.blockList());

		// remove any road blocks from event block list
		for (Block block : blocks) {
			if (plugin.blockManager.isRoadBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}


	/**
	 * Event handler for EntityExplodeEvent;
	 * prevents protected road blocks from being destroyed by entity explosions
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityExplode(final EntityExplodeEvent event) {

		// get collection of exploded blocks
		final Collection<Block> blocks = new ArrayList<>(event.blockList());

		// remove any road blocks from event block list
		for (Block block : blocks) {
			if (plugin.blockManager.isRoadBlock(block)) {
				event.blockList().remove(block);
			}
		}
	}


	/**
	 * Event handler for EntityChangeBlockEvent;
	 * stops entities from changing protected road blocks
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityChangeBlock(final EntityChangeBlockEvent event) {

		// if event block is a RoadBlock, cancel event
		if (plugin.blockManager.isRoadBlock(event.getBlock())) {
			event.setCancelled(true);
		}
	}


	/**
	 * Event handler for EntityTargetLivingEntityEvent;
	 * cancels players being targeted by mobs if they are within configured height above a road block
	 * and mob is further away than configured target-distance
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event) {

		// if configured target distance is zero or negative, do nothing and return (feature is disabled)
		if (plugin.getConfig().getInt("target-distance") <= 0) {
			return;
		}

		// check that target is a player
		if (event.getTarget() != null && event.getTarget() instanceof Player) {

			// get targeted player
			final Player player = (Player) event.getTarget();

			// check that player is above a road block
			if (plugin.blockManager.isAboveRoad(player)) {

				// if entity to target distance is less than 
				// configured target distance,
				// do nothing and return, allowing player to be targeted 
				if (event.getEntity().getLocation()
						.distanceSquared(player.getLocation()) < Math.pow(plugin
						.getConfig().getInt("target-distance"), 2)) {
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
	 * Event handler for BlockPistonExtendEvent;
	 * prevents extending pistons from effecting road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onPistonExtend(final BlockPistonExtendEvent event) {

		// get list of blocks affected by piston
		final Collection<Block> blocks = new ArrayList<>(event.getBlocks());

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
	 * Event handler for BlockPistonRetractEvent;
	 * Prevents retracting pistons from effecting road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onPistonRetract(final BlockPistonRetractEvent event) {

		// get collection of blocks affected by piston
		final Collection<Block> blocks = new ArrayList<>(event.getBlocks());

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
	 * Event handler for BlockFormEvent;
	 * prevents snow from forming on road blocks if configured
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockForm(final BlockFormEvent event) {

		// if configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("snow-plow")) {
			return;
		}

		// get event block
		Block block = event.getBlock();

		// if formed block is above road block, cancel event
		if (plugin.blockManager.isAboveRoad(block.getLocation(), 1)) {
			event.setCancelled(true);
		}
	}

}
