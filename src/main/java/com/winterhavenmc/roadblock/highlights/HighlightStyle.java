package com.winterhavenmc.roadblock.highlights;

import com.winterhavenmc.roadblock.PluginMain;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;


public enum HighlightStyle {

	PROTECT(Material.EMERALD_BLOCK, "protect-material"),
	UNPROTECT(Material.REDSTONE_BLOCK, "unprotect-material");

	private final static PluginMain plugin = JavaPlugin.getPlugin(PluginMain.class);

	private final Material defaultMaterial;
	private final String configString;


	/**
	 * Class constructor
	 *
	 * @param defaultMaterial the material type to use as default
	 * @param configString    the configuration key for material type
	 */
	HighlightStyle(final Material defaultMaterial, final String configString) {
		this.defaultMaterial = defaultMaterial;
		this.configString = configString;
	}


	/**
	 * Get a matching material type for the configured string
	 *
	 * @return the material type that matches the configured string
	 */
	final Material getMaterial() {

		// get configured material
		String materialString = plugin.getConfig().getString(this.configString);
		if (materialString == null) {
			return this.defaultMaterial;
		}

		// try to match configured material
		Material material = Material.matchMaterial(materialString);

		// if no matching material, use default material
		if (material == null) {
			material = this.defaultMaterial;
		}
		return material;
	}

}
