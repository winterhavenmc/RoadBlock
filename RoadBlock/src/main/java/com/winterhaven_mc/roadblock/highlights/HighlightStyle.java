package com.winterhaven_mc.roadblock.highlights;

import org.bukkit.Material;

import com.winterhaven_mc.roadblock.PluginMain;

public enum HighlightStyle {

	PROTECT(Material.EMERALD_BLOCK,"protect-material"),
	UNPROTECT(Material.REDSTONE_BLOCK,"unprotect-material");
	
	private final static PluginMain plugin = PluginMain.instance;
	
	private Material defaultMaterial;
	private String configString;

	
	/**
	 * Class constructor
	 * @param defaultMaterial
	 * @param configString
	 */
	HighlightStyle(Material defaultMaterial, String configString) {
		this.defaultMaterial = defaultMaterial;
		this.configString = configString;
	}

	public Material getMaterial() {

		Material material = Material.matchMaterial(plugin.getConfig().getString(this.configString));

		if (material == null) {
			material = this.defaultMaterial;
		}
		return material;
	}

}
