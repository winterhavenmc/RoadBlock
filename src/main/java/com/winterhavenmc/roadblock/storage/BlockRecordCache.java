package com.winterhavenmc.roadblock.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("UnusedReturnValue")
final class BlockRecordCache {

	// block cache
	private final Map<BlockRecord, CacheStatus> blockMap = new ConcurrentHashMap<>();


	/**
	 * Private class constructor
	 */
	private BlockRecordCache() { }


	/**
	 * Singleton helper class
	 */
	private static class SingletonHelper {
		private static final BlockRecordCache INSTANCE = new BlockRecordCache();
	}


	/**
	 * Static factory for class
	 * @return instance of this singleton class
	 */
	static BlockRecordCache getInstance() {
		return SingletonHelper.INSTANCE;
	}


	CacheStatus get(final BlockRecord key) {
		return blockMap.get(key);
	}

	CacheStatus put(final BlockRecord key, final CacheStatus value) {
		return blockMap.put(key, value);
	}

	CacheStatus remove(final BlockRecord key) {
		return blockMap.remove(key);
	}

	boolean containsKey(final BlockRecord key) {
		return blockMap.containsKey(key);
	}

	Set<BlockRecord> keySet() {
		return blockMap.keySet();
	}

}
