package com.winterhaven_mc.roadblock.storage;

import org.bukkit.Location;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("UnusedReturnValue")
public class LocationRecordCache {

	// block cache
	private final Map<LocationRecord, CacheStatus> blockMap = new ConcurrentHashMap<>();


	/**
	 * Private class constructor
	 */
	private LocationRecordCache() { }


	/**
	 * Singleton helper class
	 */
	private static class SingletonHelper {
		private static final LocationRecordCache INSTANCE = new LocationRecordCache();
	}


	/**
	 * Static factory for class
	 * @return instance of this singleton class
	 */
	static LocationRecordCache getInstance() {
		return SingletonHelper.INSTANCE;
	}


	CacheStatus get(LocationRecord key) {
		return blockMap.get(key);
	}

	CacheStatus get(Location key) {
		return blockMap.get(new LocationRecord(key));
	}

	CacheStatus put(LocationRecord key, CacheStatus value) {
		return blockMap.put(key, value);
	}

	CacheStatus put(Location key, CacheStatus value) {
		return blockMap.put(new LocationRecord(key), value);
	}

	CacheStatus remove(LocationRecord key) {
		return blockMap.remove(key);
	}

	CacheStatus remove(Location key) {
		return blockMap.remove(new LocationRecord(key));
	}

	boolean remove(LocationRecord key, CacheStatus value) {
		return blockMap.remove(key, value);
	}

	boolean remove(Location key, CacheStatus value) {
		return blockMap.remove(new LocationRecord(key), value);
	}

	boolean containsKey(LocationRecord key) {
		return blockMap.containsKey(key);
	}

	boolean containsKey(Location key) {
		return blockMap.containsKey(new LocationRecord(key));
	}

	Set<LocationRecord> keySet() {
		return blockMap.keySet();
	}

}
