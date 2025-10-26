package com.winterhavenmc.roadblock.core.context;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import com.winterhavenmc.roadblock.core.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.storage.BlockManager;

import org.bukkit.plugin.Plugin;


public record ListenerCtx(Plugin plugin, MessageBuilder messageBuilder,
                          BlockManager blockManager, HighlightManager highlightManager) { }
