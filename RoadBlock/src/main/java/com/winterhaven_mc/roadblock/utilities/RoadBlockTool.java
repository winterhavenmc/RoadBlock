package com.winterhaven_mc.roadblock.utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.winterhaven_mc.roadblock.PluginMain;

public class RoadBlockTool {
	
	private final static PluginMain plugin = PluginMain.instance;
	
	public static final Set<Material> toolTransparentMaterials = 
			Collections.unmodifiableSet(new HashSet<Material>(Arrays.asList(
					Material.AIR,
					Material.SNOW,
					Material.LONG_GRASS
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
			material = Material.GOLD_PICKAXE;
		}
		// create item stack of configured tool material
		ItemStack itemStack = new ItemStack(material);
		
		// get item stack metadata
		ItemMeta metaData = itemStack.getItemMeta();
		
		// set display name to configured tool name
		metaData.setDisplayName(plugin.messageManager.getToolName());
		
		// set lore to configured tool lore
		metaData.setLore(plugin.messageManager.getToolLore());

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
	 * @param itemStack
	 * @return true if item is a RoadBlock tool, false if it is not
	 */
	public static boolean isTool(final ItemStack itemStack) {
		
		// if passed item stack is null, return false
		if (itemStack == null) {
			return false;
		}
		
		// if item stack is not configured tool-material, return false
		if (!itemStack.getType().equals(Material.matchMaterial(plugin.getConfig().getString("tool-material")))) {
			return false;
		}

		// if player item does not have configured tool display name, return false
		if (!itemStack.hasItemMeta() ||
				!itemStack.getItemMeta().getDisplayName().equals(plugin.messageManager.getToolName())) {
			return false;
		}
		
		// return true
		return true;
	}

}
