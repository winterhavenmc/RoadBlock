package com.winterhaven_mc.roadblock.utilities;

import com.winterhaven_mc.roadblock.PluginMain;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;


public final class RoadBlockTool {

	private final static PluginMain plugin = PluginMain.instance;

	private final static NamespacedKey toolKey = new NamespacedKey(plugin, "isTool");

	public final static Set<Material> toolTransparentMaterials =
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
					Material.AIR,
					Material.SNOW,
					Material.TALL_GRASS
			)));


	/**
	 * Create an item stack with configured tool material, name and lore
	 *
	 * @return RoadBlock tool item stack
	 */
	public static ItemStack create() {

		// get configured material
		//noinspection ConstantConditions
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
		//noinspection ConstantConditions
		metaData.setDisplayName(plugin.messageManager.getItemName());

		// set lore to configured tool lore
		metaData.setLore(plugin.messageManager.getItemLore());

		// hide item stack attributes and enchants
		metaData.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		metaData.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		metaData.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		// set persistent data in item metadata
		metaData.getPersistentDataContainer().set(toolKey, PersistentDataType.BYTE, (byte) 1);

		// set item stack metadata
		itemStack.setItemMeta(metaData);

		// return item stack
		return itemStack;
	}


	/**
	 * Create an item stack with configured tool material, name and lore
	 *
	 * @param material material type to use for new tool
	 * @param itemName name to use for new tool
	 * @param itemLore lore to use for new tool
	 * @return RoadBlock tool item stack
	 */
	public static ItemStack create(final Material material, final String itemName, final List<String> itemLore) {

		// check for null parameters
		Objects.requireNonNull(material);
		Objects.requireNonNull(itemName);
		Objects.requireNonNull(itemLore);

		// create item stack of configured tool material
		final ItemStack itemStack = new ItemStack(material);

		// get item stack metadata
		final ItemMeta metaData = itemStack.getItemMeta();

		// set display name to configured tool name
		//noinspection ConstantConditions
		metaData.setDisplayName(ChatColor.RESET + itemName);

		// set lore to configured tool lore
		metaData.setLore(itemLore);

		// set unbreakable
		metaData.setUnbreakable(true);

		// hide item stack attributes and enchants
		metaData.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		metaData.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		metaData.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		// set persistent data in item metadata
		metaData.getPersistentDataContainer().set(toolKey, PersistentDataType.BYTE, (byte) 1);

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
		//noinspection ConstantConditions,RedundantIfStatement
		if (!itemStack.getItemMeta().getPersistentDataContainer().has(toolKey, PersistentDataType.BYTE)) {
			return false;
		}

		// return true
		return true;
	}

}
