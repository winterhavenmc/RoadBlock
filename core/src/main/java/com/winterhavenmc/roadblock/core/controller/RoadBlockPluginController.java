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

package com.winterhavenmc.roadblock.core;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;
import com.winterhavenmc.roadblock.core.commands.CommandManager;
import com.winterhavenmc.roadblock.core.highlights.HighlightManager;
import com.winterhavenmc.roadblock.core.listeners.BlockEventListener;
import com.winterhavenmc.roadblock.core.listeners.EntityEventListener;
import com.winterhavenmc.roadblock.core.ports.datastore.ConnectionProvider;
import com.winterhavenmc.roadblock.core.storage.BlockManager;
import com.winterhavenmc.roadblock.core.util.MetricsHandler;

import org.bukkit.plugin.java.JavaPlugin;


public final class RoadBlockPluginController implements PluginController
{
	public MessageBuilder messageBuilder;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
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

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(plugin);

		// instantiate world manager
		worldManager = new WorldManager(plugin);

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
