package com.winterhavenmc.roadblock.core.ports.config;

import com.winterhavenmc.roadblock.core.util.Config;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public interface MaterialsProvider
{
	Supplier<Set<Material>> getSupplier();

	/**
	 * Check if a material is a valid road block material
	 *
	 * @param material the material type to test for valid road block material
	 * @return {@code true} if the material is a configured road block material, {@code false} if it is not
	 */
	@SuppressWarnings("unused")
	boolean isRoadBlockMaterial(Material material);

	boolean contains(Material material);

	/**
	 * Check if block is a valid road block material
	 *
	 * @param block the block to test for valid configured road block material
	 * @return {@code true} if the block material is a configured road block material, {@code false} if it is not
	 */
	boolean isRoadBlockMaterial(Block block);
}
