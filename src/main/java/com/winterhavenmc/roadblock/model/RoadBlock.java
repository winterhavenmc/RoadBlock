package com.winterhavenmc.roadblock.model;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.util.Reason;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.UUID;

import static com.winterhavenmc.roadblock.model.RoadBlock.FailReason.*;


/**
 * Represents the result of attempting to identify a roadblock at a given location.
 * This is a sealed hierarchy with variants for valid and invalid block locations.
 */
public sealed interface RoadBlock permits RoadBlock.Valid, RoadBlock.Invalid
{
	sealed interface Valid extends RoadBlock permits RoadBlock.Protected, RoadBlock.Unprotected { }
	record Invalid(FailReason reason) implements RoadBlock { }
	record Protected(BlockLocation blockLocation) implements Valid { }
	record Unprotected(BlockLocation blockLocation) implements Valid { }


	static RoadBlock of(final BlockLocation.Valid blockLocation, final PluginMain plugin)
	{
		if (blockLocation instanceof BlockLocation.Invalid(FailReason reason)) return new RoadBlock.Invalid(reason);
		else if (blockLocation instanceof BlockLocation.Valid valid)
		{
			World world = plugin.getServer().getWorld(valid.worldUid);
			if (world != null && plugin.blockManager.isRoadBlock(world.getBlockAt(valid.blockX, valid.blockY, valid.blockZ)))
				return new Protected(blockLocation);
		}
		return new Unprotected(blockLocation);
	}


	/**
	 * A sealed type representing either a valid or invalid location.
	 */
	sealed interface BlockLocation permits BlockLocation.Valid, BlockLocation.Invalid
	{
		record Invalid(FailReason reason) implements BlockLocation { }
		record Valid(String worldName, UUID worldUid, int blockX, int blockY, int blockZ, int chunkX, int chunkZ) implements BlockLocation
		{
			public Location getLocation()
			{
				World world = Bukkit.getWorld(worldUid);
				return (world != null)
						? new Location(world, blockX, blockY, blockZ)
						: null;
			}
		}


		static BlockLocation of(final Location location)
		{
			if (location == null) return new Invalid(FailReason.LOCATION_NULL);
			else if (location.getWorld() == null) return new Invalid(FailReason.WORLD_NULL);
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


	/**
	 * The set of possible failure reasons when evaluating a block location.
	 * Each enum constant provides a human-readable explanation.
	 */
	enum FailReason implements Reason
	{
		LOCATION_NULL("The location was null."),
		WORLD_NULL("The location world was null."),
		WORLD_UNLOADED("The location world was not loaded."),
		WORLD_NAME_NULL("The world name was null."),
		WORLD_UID_NULL("The world UUID was null."),
		WORLD_NAME_BLANK("The world name was blank."),
		BLOCK_MATERIAL("The block is not a RoadBlock material."),
		;

		private final String message; //TODO: these will be message keys for a localized bundle


		/**
		 * Enum constructor assigns message String to constant field
		 *
		 * @param message a human-readable error message
		 */
		FailReason(String message)
		{
			this.message = message;
		}


		/**
		 * Gets a human-readable explanation for this failure.
		 *
		 * @return the failure description
		 */
		public String getMessage()
		{
			return message;
		}
	}

}
