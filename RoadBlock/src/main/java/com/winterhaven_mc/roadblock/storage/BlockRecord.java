package com.winterhaven_mc.roadblock.storage;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.UUID;


public class BlockRecord {

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
	public BlockRecord(final Location location) {

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
	 * Class constructor
	 *
	 * @param worldName location world name
	 * @param worldUid location world uid
	 * @param blockX location block x coordinate
	 * @param blockY location block y coordinate
	 * @param blockZ location block z coordinate
	 * @param chunkX location chunk x coordinate
	 * @param chunkZ location chunk z coordinate
	 */
	public BlockRecord(final String worldName,
					   final UUID worldUid,
					   final int blockX,
					   final int blockY,
					   final int blockZ,
					   final int chunkX,
					   final int chunkZ) {

		this.worldName = worldName;
		this.worldUid = worldUid;
		this.blockX = blockX;
		this.blockY = blockY;
		this.blockZ = blockZ;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
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
		BlockRecord that = (BlockRecord) o;
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
