package com.winterhavenmc.roadblock.core.util;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import com.winterhavenmc.roadblock.core.ports.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;

import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import org.bukkit.plugin.java.JavaPlugin;


public record PluginCtx(JavaPlugin plugin, MessageBuilder messageBuilder, MaterialsProvider materials,
                        BlockRepository blocks, HighlightManager highlightManager) { }
