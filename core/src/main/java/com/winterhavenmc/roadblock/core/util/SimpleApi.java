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
package com.winterhavenmc.roadblock.core.util;

import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


@SuppressWarnings("unused")
public final class SimpleApi
{
	private final Plugin plugin;
	private final BlockRepository blocks;
	private final MaterialsProvider materials;


	/**
	 * Private constructor to prevent instantiation of this utility class
	 */
	public SimpleApi(final Plugin plugin, final BlockRepository blocks, final MaterialsProvider materials)
	{
		this.plugin = plugin;
		this.blocks = blocks;
		this.materials = materials;
	}


	/**
	 * Check if a material is in the configured list of RoadBlock materials
	 *
	 * @param material the material to check
	 * @return {@code true} if the material is in the configured list of RoadBlock materials, else {@code false}
	 */
	public boolean isRoadBlockMaterial(final Material material)
	{
		return materials.isRoadBlockMaterial(material);
	}


	/**
	 * Check if player location is within configured on-road-height distance above a road block
	 *
	 * @param player the player to check for road block proximity
	 * @return {@code true} if player is configured height or less above a road block, else {@code false}
	 */
	public boolean isAboveRoad(final Player player)
	{
		return blocks.isAboveRoad(player);
	}


	/**
	 * Check if location is above a protected road block, using configured no-place-height
	 *
	 * @param location the location to check road block proximity
	 * @return {@code true} if block is within configured no-place-height above a road block, else {@code false}
	 */
	public boolean isAboveRoad(final Location location)
	{
		return blocks.isAboveRoad(location, Config.NO_PLACE_HEIGHT.getInt(plugin.getConfig()));
	}


	/**
	 * Check if location is above a protected road block, overriding configured no-place-height with passed height
	 *
	 * @param location the location to test
	 * @param height   the number of blocks above a road block to consider
	 * @return {@code true} if block is within {@code height} above a road block, else {@code false}
	 */
	public boolean isAboveRoad(final Location location, final int height)
	{
		return blocks.isAboveRoad(location, height);
	}


	/**
	 * Check if a block can be placed at a location, using configured no-place-height
	 *
	 * @param location the location to check
	 * @return {@code true} if block can be placed at location, else {@code false}
	 */
	public boolean canPlace(final Location location)
	{
		// get configured no-place-height
		int height = Config.NO_PLACE_HEIGHT.getInt(plugin.getConfig());

		// check location with configured no-place-height
		return canPlace(location, height);
	}


	/**
	 * Check if a block can be placed at a location, overriding configured no-place-height with passed height
	 *
	 * @param location the location to check
	 * @param height   the height above road to consider not placeable
	 * @return {@code true} if the block can be place at location, else {@code false}
	 */
	public boolean canPlace(final Location location, final int height)
	{
		// return result of: block is a protected road block or block is within passed height above a road block
		return !(blocks.isRoadBlock(location.getBlock())
				|| blocks.isAboveRoad(location, height));
	}

}
