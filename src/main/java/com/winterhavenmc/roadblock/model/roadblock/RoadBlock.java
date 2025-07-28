package com.winterhavenmc.roadblock.model.roadblock;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.model.blocklocation.BlockLocation;
import org.bukkit.World;
import org.bukkit.block.Block;

import static com.winterhavenmc.roadblock.model.roadblock.RoadBlockReason.*;


/**
 * Represents the result of attempting to identify a roadblock at a given location.
 * This is a sealed hierarchy with variants for valid and invalid block locations.
 */
public sealed interface RoadBlock permits RoadBlock.Valid, RoadBlock.Invalid
{
	sealed interface Valid extends RoadBlock permits RoadBlock.Protected, RoadBlock.Unprotected { }
	record Invalid(RoadBlockReason reason) implements RoadBlock { }
	record Protected(BlockLocation blockLocation) implements Valid { }
	record Unprotected(BlockLocation blockLocation) implements Valid { }


	static RoadBlock of(final BlockLocation.Valid blockLocation, final PluginMain plugin)
	{
		World world = plugin.getServer().getWorld(blockLocation.worldUid());
		if (world == null) return new RoadBlock.Invalid(WORLD_NULL);

		Block block = world.getBlockAt(blockLocation.blockX(), blockLocation.blockY(), blockLocation.blockZ());
		if (plugin.blockManager.isRoadBlock(block)) return new Protected(blockLocation);
		else if (plugin.blockManager.isRoadBlockMaterial(block.getType())) return new RoadBlock.Unprotected(blockLocation);
		else return new Invalid(BLOCK_MATERIAL);
	}

}
