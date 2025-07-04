/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.roadblock.storage;

import com.winterhavenmc.roadblock.model.RoadBlock;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("UnusedReturnValue")
final class BlockLocationCache
{
	// block cache
	private final Map<RoadBlock.BlockLocation, CacheStatus> blockMap = new ConcurrentHashMap<>();


	/**
	 * Private class constructor
	 */
	private BlockLocationCache()
	{
	}


	/**
	 * Singleton helper class
	 */
	private static class SingletonHelper
	{
		private static final BlockLocationCache INSTANCE = new BlockLocationCache();
	}


	/**
	 * Static factory for class
	 *
	 * @return instance of this singleton class
	 */
	static BlockLocationCache getInstance()
	{
		return SingletonHelper.INSTANCE;
	}


	CacheStatus get(final RoadBlock.BlockLocation key)
	{
		return blockMap.get(key);
	}

	CacheStatus put(final RoadBlock.BlockLocation key, final CacheStatus value)
	{
		return blockMap.put(key, value);
	}

	CacheStatus remove(final RoadBlock.BlockLocation key)
	{
		return blockMap.remove(key);
	}

	boolean containsKey(final RoadBlock.BlockLocation key)
	{
		return blockMap.containsKey(key);
	}

	Set<RoadBlock.BlockLocation> keySet()
	{
		return blockMap.keySet();
	}

}
