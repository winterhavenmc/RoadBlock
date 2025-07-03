package com.winterhavenmc.roadblock.block_location;

import org.bukkit.Location;

import java.util.UUID;

public sealed interface BlockLocation permits ValidBlockLocation, InvalidBlockLocation
{
	static BlockLocation of(Location location)
	{
		if (location == null) return new InvalidBlockLocation("The location was null");
		else if (location.getWorld() == null) return new InvalidBlockLocation("The location world was null.");
		else if (!location.isWorldLoaded()) return new InvalidBlockLocation("The location world is not loaded.");

		return new ValidBlockLocation(location.getWorld().getName(), location.getWorld().getUID(),
				location.getBlockX(), location.getBlockY(), location.getBlockZ(),
				location.getChunk().getX(), location.getChunk().getZ());
	}

	static ValidBlockLocation of(String worldName, UUID worldUid, int blockX, int blockY, int blockZ, int chunkX, int chunkZ)
	{
		return new ValidBlockLocation(worldName, worldUid, blockX, blockY, blockZ, chunkX, chunkZ);
	}

}
