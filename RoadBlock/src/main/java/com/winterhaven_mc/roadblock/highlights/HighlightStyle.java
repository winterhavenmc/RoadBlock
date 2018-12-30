package com.winterhaven_mc.roadblock.highlights;

import com.winterhaven_mc.roadblock.PluginMain;

import org.bukkit.Material;


public enum HighlightStyle {

	PROTECT(Material.EMERALD_BLOCK, "protect-material"),
	UNPROTECT(Material.REDSTONE_BLOCK, "unprotect-material");

	private final static PluginMain plugin = PluginMain.instance;

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


	final Material getMaterial() {

		Material material = Material.matchMaterial(plugin.getConfig().getString(this.configString));

		if (material == null) {
			material = this.defaultMaterial;
		}
		return material;
	}

}
