package com.winterhavenmc.roadblock.core.context;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import com.winterhavenmc.roadblock.core.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.storage.BlockManager;

import org.bukkit.plugin.java.JavaPlugin;


public record CommandCtx(JavaPlugin plugin, MessageBuilder messageBuilder,
                         BlockManager blockManager, HighlightManager highlightManager) { }
