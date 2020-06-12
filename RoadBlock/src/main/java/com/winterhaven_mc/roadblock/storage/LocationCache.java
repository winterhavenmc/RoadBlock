package com.winterhaven_mc.roadblock.storage;

import org.bukkit.Location;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("UnusedReturnValue")
public class LocationCache {

	// location cache map
	private final Map<Location, CacheStatus> LocationMap = new ConcurrentHashMap<>();


	/**
	 * private class constructor
	 */
	private LocationCache() { }


	/**
	 * singleton helper class
	 */
	private static class SingletonHelper {
		private static final LocationCache INSTANCE = new LocationCache();
	}


	/**
	 * Get instance of this singleton
	 *
	 * @return instance of this singleton
	 */
	public static LocationCache getInstance() {
		return SingletonHelper.INSTANCE;
	}


	CacheStatus get(Location key) {
		return LocationMap.get(key);
	}

	CacheStatus put(Location key, CacheStatus value) {
		return LocationMap.put(key, value);
	}

	CacheStatus remove(Location key) {
		return LocationMap.remove(key);
	}

	boolean containsKey(Location key) {
		return LocationMap.containsKey(key);
	}

	Set<Location> keySet() {
		return LocationMap.keySet();
	}

}
