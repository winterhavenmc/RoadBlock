/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.roadblock.adapters.listeners.bukkit;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.roadblock.core.ports.listeners.BlockEventListener;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.core.util.Macro;
import com.winterhavenmc.roadblock.core.util.MessageId;
import com.winterhavenmc.roadblock.core.util.Config;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Implements player event listeners for RoadBlock events.
 */
public final class BukkitBlockEventListener implements BlockEventListener
{
	private final Plugin plugin;
	private final MessageBuilder messageBuilder;
	private final BlockRepository blocks;

	private final Set<String> pathMaterialNames = Set.of(
			"GRASS_PATH",
			"LEGACY_GRASS_PATH",
			"DIRT_PATH");


	/**
	 * Class constructor for BlockEventListener class
	 */
	public BukkitBlockEventListener(final Plugin plugin,
	                                final MessageBuilder messageBuilder,
	                                final BlockRepository blocks)
	{
		this.plugin = plugin;
		this.messageBuilder = messageBuilder;
		this.blocks = blocks;
		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Event handler for BlockPlaceEvent;
	 * prevents placing blocks on top of road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	@Override
	public void onBlockPlace(final BlockPlaceEvent event)
	{
		// get configured no-place-height
		final int height = Config.NO_PLACE_HEIGHT.getInt(plugin.getConfig());

		// get block placed
		final Block placedBlock = event.getBlockPlaced();

		// get player
		final Player player = event.getPlayer();

		// check if block below placed block is protected grass path, to prevent converting to regular dirt
		final Block blockBelow = placedBlock.getRelative(BlockFace.DOWN);
		if (pathMaterialNames.contains(blockBelow.getType().toString()) && blocks.isRoadBlock(blockBelow))
		{
			event.setCancelled(true);
			messageBuilder.compose(player, MessageId.BLOCK_PLACE_FAIL_GRASS_PATH).send();
			return;
		}

		// check if block placed is configured distance above a road block
		if (blocks.isAboveRoad(placedBlock.getLocation(), height))
		{
			event.setCancelled(true);
			messageBuilder.compose(player, MessageId.BLOCK_PLACE_FAIL_ABOVE_ROAD).send();
		}
	}


	/**
	 * Event handler for BlockMultiPlaceEvent;
	 * prevents placing blocks on top of road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	@Override
	public void onBlockMultiPlace(final BlockMultiPlaceEvent event)
	{
		// get configured no-place-height
		final int height = Config.NO_PLACE_HEIGHT.getInt(plugin.getConfig());

		// get list of blocks that will be replaced
		final List<BlockState> replacedBlocks = event.getReplacedBlockStates();

		// get event player
		final Player player = event.getPlayer();

		// iterate through blocks and check if any are above a road block
		for (BlockState blockState : replacedBlocks)
		{
			// if block is above a road block, cancel event and send player message
			if (blocks.isAboveRoad(blockState.getLocation(), height))
			{
				event.setCancelled(true);
				messageBuilder.compose(player, MessageId.BLOCK_PLACE_FAIL_ABOVE_ROAD).send();
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
	@Override
	public void onBlockBreak(final BlockBreakEvent event)
	{
		// get block being broken
		final Block block = event.getBlock();

		// get player
		final Player player = event.getPlayer();

		// check if block is a protected road block
		if (blocks.isRoadBlock(block))
		{
			// if player does not have override permission, cancel event and send player message
			if (!player.hasPermission("roadblock.break"))
			{
				event.setCancelled(true);
				messageBuilder.compose(player, MessageId.TOOL_FAIL_USE_BLOCK_BREAK_PERMISSION)
						.setMacro(Macro.WORLD, player.getWorld())
						.send();
				return;
			}

			// player does have override permission; remove protection from block and send player message
			blocks.removeBlockLocations(Set.of(block.getLocation()));
			messageBuilder.compose(player, MessageId.TOOL_SUCCESS_BREAK_BLOCK).send();
		}
	}


	/**
	 * Event handler for BlockExplodeEvent;
	 * prevents protected road blocks from being destroyed by block explosions
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	@Override
	public void onBlockExplode(final BlockExplodeEvent event)
	{
		// get collection of exploded blocks
		final Collection<Block> eventBlocks = new ArrayList<>(event.blockList());

		// remove any road blocks from event block list
		for (Block block : eventBlocks)
		{
			if (this.blocks.isRoadBlock(block))
			{
				event.blockList().remove(block);
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
	@Override
	public void onPistonExtend(final BlockPistonExtendEvent event)
	{
		// get list of blocks affected by piston
		final Collection<Block> eventBlocks = new ArrayList<>(event.getBlocks());

		// iterate through block list checking for road blocks
		for (Block block : eventBlocks)
		{
			// if block is a road block, cancel event and break piston
			if (this.blocks.isRoadBlock(block))
			{
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
	@Override
	public void onPistonRetract(final BlockPistonRetractEvent event)
	{
		// get collection of blocks affected by piston
		final Collection<Block> eventBlocks = new ArrayList<>(event.getBlocks());

		// iterate through block list checking for road blocks
		for (Block block : eventBlocks)
		{
			// if block is a road block, cancel event and break piston
			if (this.blocks.isRoadBlock(block))
			{
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
	@Override
	public void onBlockForm(final BlockFormEvent event)
	{
		// if configured false, do nothing and return
		if (!Config.SNOW_PLOW.getBoolean(plugin.getConfig()))
		{
			return;
		}

		// get event block
		Block block = event.getBlock();

		// if formed block is above road block, cancel event
		if (blocks.isAboveRoad(block.getLocation(), 1))
		{
			event.setCancelled(true);
		}
	}

}
