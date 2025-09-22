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

package com.winterhavenmc.roadblock.core.listeners;

import com.winterhavenmc.library.messagebuilder.ItemForge;
import com.winterhavenmc.roadblock.core.PluginController;
import com.winterhavenmc.roadblock.core.highlights.HighlightStyle;
import com.winterhavenmc.roadblock.core.util.Macro;
import com.winterhavenmc.roadblock.core.util.MessageId;
import com.winterhavenmc.roadblock.core.util.SoundId;
import com.winterhavenmc.roadblock.core.util.Config;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EntityEventListener implements Listener
{
	private final PluginController.ListenerContextContainer ctx;
	private final Set<EntityTargetEvent.TargetReason> cancelReasons = Set.of(
			EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
			EntityTargetEvent.TargetReason.RANDOM_TARGET,
			EntityTargetEvent.TargetReason.UNKNOWN);
	private final static Set<Material> toolTransparentMaterials = Set.of(
			Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.SNOW,
			Material.SHORT_GRASS, Material.TALL_GRASS, Material.VINE);


	/**
	 * Class constructor for EntityEventListener
	 */
	public EntityEventListener(final PluginController.ListenerContextContainer ctx)
	{
		this.ctx = ctx;
		ctx.plugin().getServer().getPluginManager().registerEvents(this, ctx.plugin());
	}


	/**
	 * Event handler for EntityExplodeEvent;
	 * prevents protected road blocks from being destroyed by entity explosions
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityExplode(final EntityExplodeEvent event)
	{
		// get collection of exploded blocks
		final Collection<Block> blocks = new ArrayList<>(event.blockList());

		// remove any road blocks from event block list
		for (Block block : blocks)
		{
			if (ctx.blockManager().isRoadBlock(block))
			{
				event.blockList().remove(block);
			}
		}
	}


	/**
	 * Event handler for EntityChangeBlockEvent;
	 * stops entities from changing protected road blocks
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityChangeBlock(final EntityChangeBlockEvent event)
	{
		// if event block is a RoadBlock, cancel event
		if (ctx.blockManager().isRoadBlock(event.getBlock()))
		{
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
	void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event)
	{
		// if configured target distance is zero or negative, do nothing and return (feature is disabled)
		if (Config.TARGET_DISTANCE.getInt(ctx.plugin().getConfig()) <= 0)
		{
			return;
		}

		// check that target is a player
		if (event.getTarget() != null && event.getTarget() instanceof final Player player)
		{
			// check that player is above a road block
			if (ctx.blockManager().isAboveRoad(player))
			{
				// if entity to target distance is less than configured target distance,
				// do nothing and return, allowing player to be targeted
				if (event.getEntity().getLocation()
						.distanceSquared(player.getLocation()) < Math.pow(Config.TARGET_DISTANCE.getInt(ctx.plugin().getConfig()), 2))
				{
					return;
				}

				// get target reason
				final EntityTargetEvent.TargetReason reason = event.getReason();

				// if reason is in cancelReasons list, cancel event
				if (cancelReasons.contains(reason))
				{
					event.setCancelled(true);
				}
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
	void onPlayerChangeItem(final PlayerItemHeldEvent event)
	{
		final Player player = event.getPlayer();

		final ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());

		if (ItemForge.isCustomItem(previousItem))
		{
			ctx.highlightManager().unHighlightBlocks(player);
		}
	}


	/**
	 * Event handler for PlayerGameModeChangeEvent;
	 * unhighlights blocks when player changes gamemode
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerChangeGameMode(final PlayerGameModeChangeEvent event)
	{
		ctx.highlightManager().unHighlightBlocks(event.getPlayer());
	}


	/**
	 * Event handler for PlayerDropItemEvent;
	 * removes custom tool from game when dropped
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onPlayerDropItem(final PlayerDropItemEvent event)
	{
		// if dropped item is a road block tool, remove dropped item
		if (ItemForge.isCustomItem(event.getItemDrop().getItemStack()))
		{
			event.getItemDrop().remove();
			ctx.soundConfig().playSound(event.getPlayer(), SoundId.TOOL_DROP);
		}
	}


	/**
	 * Event handler for PlayerInteractEvent;
	 * handles blocks clicked with RoadBlock tool
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerInteract(final PlayerInteractEvent event)
	{
		//NOTE: do not check for cancelled event here; long distance clicks are considered cancelled

		final Player player = event.getPlayer();

		final ItemStack playerItem = event.getItem();

		final Action action = event.getAction();

		// get clicked block
		Block clickedBlock = event.getClickedBlock();

		// if world is not enabled, send message and return
		if (!ctx.worldManager().isEnabled(player.getWorld()))
		{
			event.setCancelled(true);
			ctx.messageBuilder().compose(player, MessageId.TOOL_FAIL_WORLD_DISABLED)
					.setMacro(Macro.WORLD, player.getWorld())
					.send();
			return;
		}

		// if event is air/block click with RoadBlock tool, begin tool use procedure
		if (ItemForge.isCustomItem(playerItem) && !action.equals(Action.PHYSICAL))
		{
			// if clicked block is tool transparent material, try to find non-air block along line of sight
			if (clickedBlock == null || toolTransparentMaterials.contains(clickedBlock.getType()))
			{
				// RH says this can sometimes throw an exception, so using try...catch block
				try
				{
					clickedBlock = player.getTargetBlock(toolTransparentMaterials, 100);
				}
				catch (Exception exception)
				{
					ctx.plugin().getLogger().info("player.getTargetBlock() threw an exception.");
					ctx.plugin().getLogger().info(exception.getLocalizedMessage());
				}
			}

			// if no clicked block detected, do nothing and return
			if (clickedBlock == null)
			{
				return;
			}

			// if clicked block is air, the actual clicked block was too far away
			if (clickedBlock.getType().equals(Material.AIR))
			{
				ctx.messageBuilder().compose(player, MessageId.TOOL_FAIL_DISTANCE_EXCEEDED).send();
				return;
			}

			// cancel event to prevent breaking blocks with road block tool
			event.setCancelled(true);

			// if player does not have roadblock.set permission, do nothing and return
			if (!player.hasPermission("roadblock.set"))
			{
				ctx.soundConfig().playSound(player, SoundId.TOOL_FAIL_USE_PERMISSION);
				ctx.messageBuilder().compose(player, MessageId.TOOL_FAIL_USE_PERMISSION)
						.setMacro(Macro.WORLD, player.getWorld())
						.send();
				return;
			}

			// if block clicked is not in list of road block materials, send message and return
			if (!ctx.blockManager().getRoadBlockMaterials().contains(clickedBlock.getType()))
			{
				ctx.soundConfig().playSound(player, SoundId.TOOL_FAIL_INVALID_MATERIAL);
				ctx.messageBuilder().compose(player, MessageId.TOOL_FAIL_INVALID_MATERIAL)
						.setMacro(Macro.MATERIAL, clickedBlock.getType())
						.send();
				return;
			}

			// get road block locations attached to clicked block
			final Collection<Location> locations = new HashSet<>(ctx.blockManager().getFill(clickedBlock.getLocation()));

			// if right click, protect blocks
			if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.RIGHT_CLICK_AIR))
			{
				protectBlocks(player, locations);
			}

			// if left click, unprotect blocks
			else if (action.equals(Action.LEFT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_AIR))
			{
				unprotectBlocks(player, locations);
			}
		}
	}


	@EventHandler
	final void onPlayerMove(final PlayerMoveEvent event)
	{
		// if speed boost is configured false, do nothing and return
		if (!Config.SPEED_BOOST.getBoolean(ctx.plugin().getConfig()))
		{
			return;
		}

		// get player for event
		final Player player = event.getPlayer();

		// if player is not above road, do nothing and return
		if (!ctx.blockManager().isAboveRoad(player))
		{
			return;
		}

		// if player movement is head movement only, do nothing and return
		if (event.getFrom().equals(event.getTo()))
		{
			return;
		}

		// speed boost attributes
		boolean ambient = false;
		boolean particles = false;
		boolean icon = false;

		// if player already has speed boost, get existing attributes
		if (player.hasPotionEffect(PotionEffectType.SPEED))
		{
			PotionEffect currentEffect = player.getPotionEffect(PotionEffectType.SPEED);
			assert currentEffect != null;
			ambient = currentEffect.isAmbient();
			particles = currentEffect.hasParticles();
			icon = currentEffect.hasIcon();
		}

		// speed boost potion effect
		PotionEffect speedBoost = new PotionEffect(PotionEffectType.SPEED, 10, 1, ambient, particles, icon);

		// apply speed boost to player
		player.addPotionEffect(speedBoost);
	}


	/**
	 * Protect a collection of blocks
	 *
	 * @param player    the player invoking the protection of blocks
	 * @param locations Collection of Location of blocks to be protected
	 */
	void protectBlocks(final Player player, final Collection<Location> locations)
	{
		// highlight blocks
		ctx.highlightManager().highlightBlocks(player, locations, HighlightStyle.PROTECT);

		// store blocks
		int count = ctx.blockManager().storeBlockLocations(locations);

		// send player successful protect message
		ctx.soundConfig().playSound(player, SoundId.TOOL_SUCCESS_PROTECT);
		ctx.messageBuilder().compose(player, MessageId.TOOL_SUCCESS_PROTECT)
				.setMacro(Macro.QUANTITY, count)
				.send();
	}


	/**
	 * Unprotect a collection of blocks
	 *
	 * @param player    the player invoking the unprotection of blocks
	 * @param locations Collection of Location of blocks to be unprotected
	 */
	void unprotectBlocks(final Player player, final Collection<Location> locations)
	{
		// highlight blocks
		ctx.highlightManager().highlightBlocks(player, locations, HighlightStyle.UNPROTECT);

		// remove blocks from storage
		int result = ctx.blockManager().removeBlockLocations(locations);

		// send player successful unprotect message
		ctx.soundConfig().playSound(player, SoundId.TOOL_SUCCESS_UNPROTECT);
		ctx.messageBuilder().compose(player, MessageId.TOOL_SUCCESS_UNPROTECT)
				.setMacro(Macro.QUANTITY, result)
				.send();
	}

}
