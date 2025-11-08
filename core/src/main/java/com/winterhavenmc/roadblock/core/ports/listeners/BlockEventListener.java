package com.winterhavenmc.roadblock.core.ports.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public interface BlockEventListener extends Listener
{
	/**
	 * Event handler for BlockPlaceEvent;
	 * prevents placing blocks on top of road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockPlace(BlockPlaceEvent event);

	/**
	 * Event handler for BlockMultiPlaceEvent;
	 * prevents placing blocks on top of road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockMultiPlace(BlockMultiPlaceEvent event);

	/**
	 * Event handler for BlockBreakEvent;
	 * prevents breaking protected road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockBreak(BlockBreakEvent event);

	/**
	 * Event handler for BlockExplodeEvent;
	 * prevents protected road blocks from being destroyed by block explosions
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockExplode(BlockExplodeEvent event);

	/**
	 * Event handler for BlockPistonExtendEvent;
	 * prevents extending pistons from effecting road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onPistonExtend(BlockPistonExtendEvent event);

	/**
	 * Event handler for BlockPistonRetractEvent;
	 * Prevents retracting pistons from effecting road blocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onPistonRetract(BlockPistonRetractEvent event);

	/**
	 * Event handler for BlockFormEvent;
	 * prevents snow from forming on road blocks if configured
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onBlockForm(BlockFormEvent event);
}
