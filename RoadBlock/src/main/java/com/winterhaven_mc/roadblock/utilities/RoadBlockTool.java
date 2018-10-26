package com.winterhaven_mc.roadblock.utilities;

import com.winterhaven_mc.roadblock.PluginMain;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public final class RoadBlockTool {
	
	private final static PluginMain plugin = PluginMain.instance;
	
	public final static Set<Material> toolTransparentMaterials = 
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					Material.AIR,
					Material.SNOW,
					Material.TALL_GRASS
				)));
	
	
	/**
	 * Create an item stack with configured tool material, name and lore
	 * @return RoadBlock tool item stack
	 */
	public static ItemStack create() {
		
		// get configured material
		Material material = Material.matchMaterial(plugin.getConfig().getString("tool-material"));
		
		// if no matching material found, use default GOLD_PICKAXE
		if (material == null) {
			material = Material.GOLDEN_PICKAXE;
		}
		// create item stack of configured tool material
		final ItemStack itemStack = new ItemStack(material);
		
		// get item stack metadata
		final ItemMeta metaData = itemStack.getItemMeta();
		
		// set display name to configured tool name
		metaData.setDisplayName(plugin.messageManager.getItemName());
		
		// set lore to configured tool lore
		//noinspection unchecked
		metaData.setLore(plugin.messageManager.getItemLore());

		// hide item stack attributes and enchants
		metaData.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		metaData.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		metaData.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		// set item stack metadata
		itemStack.setItemMeta(metaData);

		// return item stack
		return itemStack;
	}
	
	
	/**
	 * Test if an item stack is a RoadBlock tool
	 * @param itemStack the ItemStack to be tested
	 * @return true if item is a RoadBlock tool, false if it is not
	 */
	public static boolean isTool(final ItemStack itemStack) {
		
		// if passed item stack is null, return false
		if (itemStack == null) {
			return false;
		}
		
		// if player item does not have configured tool display name, return false
		if (!itemStack.hasItemMeta()) {
			return false;
		}

		if (!(itemStack.getItemMeta().getDisplayName()).equals(plugin.messageManager.getItemName())) {
			return false;
		}

		// if item stack is not configured tool-material, return false
		//noinspection RedundantIfStatement
		if (!itemStack.getType().equals(Material.matchMaterial(plugin.getConfig().getString("tool-material")))) {
			return false;
		}

		// return true
		return true;
	}

}
