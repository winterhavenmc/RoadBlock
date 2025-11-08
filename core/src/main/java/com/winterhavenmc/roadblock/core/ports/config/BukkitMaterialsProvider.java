package com.winterhavenmc.roadblock.core.ports.config;

import com.winterhavenmc.roadblock.core.util.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;


public final class BukkitMaterialsProvider implements MaterialsProvider
{
	private final Supplier<Set<Material>> materialsSupplier;


	public BukkitMaterialsProvider(final Plugin plugin)
	{
		this.materialsSupplier = () -> BukkitMaterialsProvider.getValidMaterials(plugin);
	}


	@Override
	public Supplier<Set<Material>> getSupplier()
	{
		return this.materialsSupplier;
	}


	/**
	 * Check if a material is a valid road block material
	 *
	 * @param material the material type to test for valid road block material
	 * @return {@code true} if the material is a configured road block material, {@code false} if it is not
	 */
	@Override
	public boolean isRoadBlockMaterial(final Material material)
	{
		return material != null && this.materialsSupplier.get().contains(material);
	}


	@Override
	public boolean contains(final Material material)
	{
		return this.materialsSupplier.get().contains(material);
	}


	/**
	 * Check if block is a valid road block material
	 *
	 * @param block the block to test for valid configured road block material
	 * @return {@code true} if the block material is a configured road block material, {@code false} if it is not
	 */
	@Override
	public boolean isRoadBlockMaterial(final Block block)
	{
		return block != null && contains(block.getType());
	}


	/**
	 * Parse valid road block materials from config file
	 */
	static Set<Material> getValidMaterials(Plugin plugin)
	{
		final Collection<String> materialStringList =
				new HashSet<>(Config.MATERIALS.getStringList(plugin.getConfig()));

		final HashSet<Material> returnSet = new HashSet<>();

		Material matchMaterial = null;

		for (String string : materialStringList)
		{
			// try to split on colon
			if (!string.isEmpty())
			{
				String[] materialElements = string.split("\\s*:\\s*");

				// try to match material
				if (materialElements.length > 0)
				{
					matchMaterial = Material.matchMaterial(materialElements[0]);
				}
			}

			// if matching material found, add to returnSet
			if (matchMaterial != null)
			{
				returnSet.add(matchMaterial);
			}
		}
		return returnSet;
	}

}
