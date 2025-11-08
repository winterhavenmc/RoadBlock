package com.winterhavenmc.roadblock.core.context;

import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;

import org.bukkit.plugin.Plugin;


public record MetricsCtx(Plugin plugin, BlockRepository blocks) { }
