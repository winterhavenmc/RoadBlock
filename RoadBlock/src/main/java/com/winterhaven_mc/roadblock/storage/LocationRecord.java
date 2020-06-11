package com.winterhaven_mc.roadblock.storage;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;


public class LocationRecord {

	private final String worldName;
	private final UUID worldUid;
	private final int blockX;
	private final int blockY;
	private final int blockZ;
	private final int chunkX;
	private final int chunkZ;


	/**
	 * Class constructor
	 * @param location location for which to create location record
	 */
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


	/**
	 * Get location represented by location record
	 * @return location
	 */
	public Location getLocation() {

		// get reference to instance of main plugin class
		JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());

		// get reference to world object using uid
		World world = plugin.getServer().getWorld(worldUid);

		// return new location object
		return new Location(world, blockX, blockY, blockZ);
	}


	/**
	 * Get world name in location record
	 * @return String - world name in location record
	 */
	public String getWorldName() {
		return worldName;
	}


	/**
	 * get world uid
	 * @return UUID - the world uid in location record
	 */
	public UUID getWorldUid() {
		return worldUid;
	}


	/**
	 * get block x coordinate for location
	 * @return int - block x coordinate for location
	 */
	public int getBlockX() {
		return blockX;
	}


	/**
	 * get block y coordinate for location
	 * @return int - block y coordinate for location
	 */
	public int getBlockY() {
		return blockY;
	}


	/**
	 * get block z coordinate for location
	 * @return int - block z coordinate for location
	 */
	public int getBlockZ() {
		return blockZ;
	}


	/**
	 * get chunk x coordinate for location
	 * @return int - chunk x coordinate for location
	 */
	public int getChunkX() {
		return chunkX;
	}


	/**
	 * get chunk z coordinate for location
	 * @return int - chunk z coordinate for location
	 */
	public int getChunkZ() {
		return chunkZ;
	}


	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LocationRecord that = (LocationRecord) o;
		return blockX == that.blockX &&
				blockY == that.blockY &&
				blockZ == that.blockZ &&
				worldUid.equals(that.worldUid);
	}


	@Override
	public int hashCode() {
		return Objects.hash(worldUid, blockX, blockY, blockZ);
	}
}
