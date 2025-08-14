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

package com.winterhavenmc.roadblock.util;

import com.winterhavenmc.roadblock.PluginMain;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Set;


public final class RoadBlockTool
{
	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private final static NamespacedKey PERSISTENT_KEY = new NamespacedKey(plugin, "TOOL");

	public static final Material DEFAULT_MATERIAL = Material.GOLDEN_PICKAXE;

	public final static Set<Material> toolTransparentMaterials = Set.of(
			Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.SNOW,
			Material.SHORT_GRASS, Material.TALL_GRASS, Material.VINE);


	/**
	 * Private constructor to prevent instantiation of this utility class
	 */
	private RoadBlockTool()
	{
		throw new AssertionError();
	}


	/**
	 * Create an item stack with configured tool material, name and lore
	 *
	 * @return RoadBlock tool item stack
	 */
	public static ItemStack create()
	{
		return plugin.messageBuilder.itemForge().createItem("TOOL").orElse(null);
	}


	/**
	 * Test if an item stack is a RoadBlock tool
	 *
	 * @param itemStack the ItemStack to be tested
	 * @return true if item is a RoadBlock tool, false if it is not
	 */
	public static boolean isTool(final ItemStack itemStack)
	{
		// if item stack is null or does not have metadata, return false
		if (itemStack == null || !itemStack.hasItemMeta())
		{
			return false;
		}

		// if item stack does not have persistent data tag, return false
		else return Objects.requireNonNull(itemStack.getItemMeta())
					.getPersistentDataContainer().has(PERSISTENT_KEY, PersistentDataType.BYTE);
	}

}
