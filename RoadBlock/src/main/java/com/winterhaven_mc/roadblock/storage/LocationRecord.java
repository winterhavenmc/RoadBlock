package com.winterhaven_mc.roadblock.storage;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;


public class LocationRecord {

	private final String worldName;
	private final UUID worldUid;
	private final int blockX;
	private final int blockY;
	private final int blockZ;
	private final int chunkX;
	private final int chunkZ;


	public LocationRecord(final Location location) {

		World world = location.getWorld();
		if (world != null) {
			this.worldName = location.getWorld().getName();
			this.worldUid = location.getWorld().getUID();
		}
		else {
			this.worldName = null;
			this.worldUid = null;
		}
		this.blockX = location.getBlockX();
		this.blockY = location.getBlockY();
		this.blockZ = location.getBlockZ();
		this.chunkX = location.getChunk().getX();
		this.chunkZ = location.getChunk().getZ();
	}

	public Location getLocation() {

		JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
		World world = plugin.getServer().getWorld(worldUid);
		return new Location(world, blockX, blockY, blockZ);
	}

	public String getWorldName() {
		return worldName;
	}

	public UUID getWorldUid() {
		return worldUid;
	}

	public int getBlockX() {
		return blockX;
	}

	public int getBlockY() {
		return blockY;
	}

	public int getBlockZ() {
		return blockZ;
	}

	public int getChunkX() {
		return chunkX;
	}

	public int getChunkZ() {
		return chunkZ;
	}

}
