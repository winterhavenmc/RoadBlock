package com.winterhavenmc.roadblock.model.blocklocation;

import org.bukkit.Location;
import java.util.UUID;

import static com.winterhavenmc.roadblock.model.blocklocation.BlockLocationReason.*;


/**
 * A sealed type representing either a valid or invalid block location.
 */
sealed public interface BlockLocation permits BlockLocation.Valid, BlockLocation.Invalid
{
	record Invalid(BlockLocationReason reason) implements BlockLocation { }
	record Valid(String worldName, UUID worldUid, int blockX, int blockY, int blockZ,
	             int chunkX, int chunkZ) implements BlockLocation { }


	static BlockLocation of(final Location location)
	{
		if (location == null) return new Invalid(LOCATION_NULL);
		else if (location.getWorld() == null) return new Invalid(WORLD_NULL);
		else if (!location.isWorldLoaded()) return new Invalid(WORLD_UNLOADED);
		else return new Valid(location.getWorld().getName(), location.getWorld().getUID(),
					location.getBlockX(), location.getBlockY(), location.getBlockZ(),
					location.getChunk().getX(), location.getChunk().getZ());
	}


	static BlockLocation of(final String worldName, final UUID worldUid,
	                        final int blockX, final int blockY, final int blockZ,
	                        final int chunkX, final int chunkZ)
	{
		if (worldName == null) return new Invalid(WORLD_NAME_NULL);
		else if (worldName.isBlank()) return new Invalid(WORLD_NAME_BLANK);
		else if (worldUid == null) return new Invalid(WORLD_UID_NULL);
		else return new Valid(worldName, worldUid, blockX, blockY, blockZ, chunkX, chunkZ);
	}
}
