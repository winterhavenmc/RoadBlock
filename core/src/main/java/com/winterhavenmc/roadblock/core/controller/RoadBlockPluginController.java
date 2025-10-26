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

package com.winterhavenmc.roadblock.core.controller;

import com.winterhavenmc.roadblock.core.commands.CommandDispatcher;
import com.winterhavenmc.roadblock.core.context.CommandCtx;
import com.winterhavenmc.roadblock.core.context.ListenerCtx;
import com.winterhavenmc.roadblock.core.context.MetricsCtx;
import com.winterhavenmc.roadblock.core.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.listeners.BlockEventListener;
import com.winterhavenmc.roadblock.core.listeners.EntityEventListener;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.roadblock.core.storage.BlockManager;
import com.winterhavenmc.roadblock.core.util.MetricsHandler;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;


public final class RoadBlockPluginController implements PluginController
{
	public MessageBuilder messageBuilder;
	public BlockManager blockManager;
	public HighlightManager highlightManager;
	public CommandManager commandManager;
	public BlockEventListener blockEventListener;
	public EntityEventListener entityEventListener;


	@Override
	public void startUp(final JavaPlugin plugin, final ConnectionProvider connectionProvider)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		messageBuilder = MessageBuilder.create(plugin);

		// instantiate block manager
		blockManager = new BlockManager(plugin, connectionProvider);

		// instantiate highlight manager
		highlightManager = new HighlightManager(plugin);

		// instantiate context containers to inject dependencies
		CommandCtx commandCtx = new CommandCtx(plugin, messageBuilder, blockManager, highlightManager);
		ListenerCtx listenerCtx = new ListenerCtx(plugin, messageBuilder, blockManager, highlightManager);
		MetricsCtx metricsCtx = new MetricsCtx(plugin, blockManager);

		// instantiate command manager
		commandDispatcher = new CommandDispatcher(commandCtx);

		// instantiate event listeners
		blockEventListener = new BlockEventListener(listenerCtx);
		entityEventListener = new EntityEventListener(listenerCtx);

		// bStats
		new MetricsHandler(metricsCtx);
	}


	@Override
	public void shutDown()
	{
		// close datastore
		blockManager.close();
	}

}
