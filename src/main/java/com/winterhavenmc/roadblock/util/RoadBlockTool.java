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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public final class RoadBlockTool {

	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private final static NamespacedKey PERSISTENT_KEY = new NamespacedKey(plugin, "isTool");

	public final static Set<Material> toolTransparentMaterials = Set.of(
			Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.SNOW,
			Material.GRASS, Material.TALL_GRASS, Material.VINE );


	/**
	 * Private constructor to prevent instantiation of this utility class
	 */
	private RoadBlockTool() {
		throw new AssertionError();
	}


	/**
	 * Create an item stack with configured tool material, name and lore
	 *
	 * @return RoadBlock tool item stack
	 */
	public static ItemStack create() {

		// initialize material
		Material material = null;

		// get configured material
		String materialString = plugin.getConfig().getString("tool-material");
		if (materialString != null) {
			material = Material.matchMaterial(materialString);
		}

		// if no matching material found, use default GOLDEN_PICKAXE
		if (material == null) {
			material = Material.GOLDEN_PICKAXE;
		}

		// create item stack of configured tool material
		final ItemStack itemStack = new ItemStack(material);

		// get item stack metadata
		final ItemMeta metaData = itemStack.getItemMeta();

		// set display name to configured tool name
		assert metaData != null;
		metaData.setDisplayName(plugin.messageBuilder.getItemName());

		// set lore to configured tool lore
		metaData.setLore(plugin.messageBuilder.getItemLore());

		// hide item stack attributes and enchants
		metaData.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		metaData.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		metaData.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		// set persistent data in item metadata
		metaData.getPersistentDataContainer().set(PERSISTENT_KEY, PersistentDataType.BYTE, (byte) 1);

		// set item stack metadata
		itemStack.setItemMeta(metaData);

		// return item stack
		return itemStack;
	}


	/**
	 * Test if an item stack is a RoadBlock tool
	 *
	 * @param itemStack the ItemStack to be tested
	 * @return true if item is a RoadBlock tool, false if it is not
	 */
	public static boolean isTool(final ItemStack itemStack) {

		// if item stack is null, return false
		if (itemStack == null) {
			return false;
		}

		// if item stack does not have meta data, return false
		if (!itemStack.hasItemMeta()) {
			return false;
		}

		// if item stack does not have persistent data tag, return false
		//noinspection RedundantIfStatement
		if (!Objects.requireNonNull(itemStack.getItemMeta())
				.getPersistentDataContainer().has(PERSISTENT_KEY, PersistentDataType.BYTE)) {
			return false;
		}

		// return true
		return true;
	}

}
