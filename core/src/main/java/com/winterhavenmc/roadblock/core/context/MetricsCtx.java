package com.winterhavenmc.roadblock.core.context;

import com.winterhavenmc.roadblock.core.storage.BlockManager;

import org.bukkit.plugin.Plugin;


public record MetricsCtx(Plugin plugin, BlockManager blockManager) { }
