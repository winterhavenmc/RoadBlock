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

package com.winterhavenmc.roadblock.core.highlights;

import com.winterhavenmc.roadblock.core.util.Config;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;


public enum HighlightStyle
{
	PROTECT(Material.EMERALD_BLOCK, Config.PROTECT_MATERIAL.toKey()),
	UNPROTECT(Material.REDSTONE_BLOCK, Config.UNPROTECT_MATERIAL.toKey());

	private final Material defaultMaterial;
	private final String configString;


	/**
	 * Class constructor
	 *
	 * @param defaultMaterial the material type to use as default
	 * @param configString    the configuration key for material type
	 */
	HighlightStyle(final Material defaultMaterial, final String configString)
	{
		this.defaultMaterial = defaultMaterial;
		this.configString = configString;
	}


	/**
	 * Get configured material type or default if not configured
	 *
	 * @return the material type that matches the configured string
	 */
	final Material getMaterial(final Plugin plugin)
	{
		// get configured material
		String materialString = plugin.getConfig().getString(this.configString);

		// if no configured material, return default material
		if (materialString == null)
		{
			return this.defaultMaterial;
		}

		// try to match material from configured string
		Material material = Material.matchMaterial(materialString);

		// if no matching material, return default material
		if (material == null)
		{
			material = this.defaultMaterial;
		}

		return material;
	}

}
