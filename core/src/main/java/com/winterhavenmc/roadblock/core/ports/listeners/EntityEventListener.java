package com.winterhavenmc.roadblock.core.ports.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.*;

import java.util.Collection;

public interface EntityEventListener extends Listener
{
	/**
	 * Event handler for EntityExplodeEvent;
	 * prevents protected road blocks from being destroyed by entity explosions
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityExplode(EntityExplodeEvent event);

	/**
	 * Event handler for EntityChangeBlockEvent;
	 * stops entities from changing protected road blocks
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityChangeBlock(EntityChangeBlockEvent event);

	/**
	 * Event handler for EntityTargetLivingEntityEvent;
	 * cancels players being targeted by mobs if they are within configured height above a road block
	 * and mob is further away than configured target-distance
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event);

	/**
	 * Event handler for PlayerItemHeldEvent;
	 * unhighlights blocks when player changes held item from road block tool
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerChangeItem(PlayerItemHeldEvent event);

	/**
	 * Event handler for PlayerGameModeChangeEvent;
	 * unhighlights blocks when player changes gamemode
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerChangeGameMode(PlayerGameModeChangeEvent event);

	/**
	 * Event handler for PlayerDropItemEvent;
	 * removes custom tool from game when dropped
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onPlayerDropItem(PlayerDropItemEvent event);

	/**
	 * Event handler for PlayerInteractEvent;
	 * handles blocks clicked with RoadBlock tool
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerInteract(PlayerInteractEvent event);

	@EventHandler
	void onPlayerMove(PlayerMoveEvent event);

	/**
	 * Protect a collection of blocks
	 *
	 * @param player    the player invoking the protection of blocks
	 * @param locations Collection of Location of blocks to be protected
	 */
	void protectBlocks(Player player, Collection<Location> locations);

	/**
	 * Unprotect a collection of blocks
	 *
	 * @param player    the player invoking the unprotection of blocks
	 * @param locations Collection of Location of blocks to be unprotected
	 */
	void unprotectBlocks(Player player, Collection<Location> locations);
}
