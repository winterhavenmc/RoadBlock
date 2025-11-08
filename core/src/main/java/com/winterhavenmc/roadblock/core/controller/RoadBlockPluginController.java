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
import com.winterhavenmc.roadblock.core.context.MetricsCtx;
import com.winterhavenmc.roadblock.core.ports.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.ports.datastore.BlockRepository;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.roadblock.core.ports.config.MaterialsProvider;
import com.winterhavenmc.roadblock.core.util.MetricsHandler;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;


public final class RoadBlockPluginController implements PluginController
{
	public MessageBuilder messageBuilder;
	public BlockRepository blocks;
	public MaterialsProvider materials;
	public HighlightManager highlightManager;
	public CommandDispatcher commandDispatcher;
	private ConnectionProvider connectionProvider;


	@Override
	public void startUp(final JavaPlugin plugin,
	                    final MessageBuilder messageBuilder,
	                    final HighlightManager highlightManager,
	                    final ConnectionProvider connectionProvider,
	                    final MaterialsProvider materials)
	{
		this.connectionProvider = connectionProvider;
		this.blocks = connectionProvider.blocks();
		this.materials = materials;

		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// instantiate message builder
		this.messageBuilder = messageBuilder;

		// instantiate highlight manager
		this.highlightManager = highlightManager;

		// instantiate context containers to inject dependencies
		CommandCtx commandCtx = new CommandCtx(plugin, messageBuilder, materials, blocks, highlightManager);
		MetricsCtx metricsCtx = new MetricsCtx(plugin, blocks);

		// instantiate command manager
		commandDispatcher = new CommandDispatcher(commandCtx);

		// bStats
		new MetricsHandler(metricsCtx);
	}


	@Override
	public void shutDown()
	{
		// close datastore
		connectionProvider.close();
	}

}
