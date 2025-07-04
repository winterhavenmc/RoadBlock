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

package com.winterhavenmc.roadblock;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;
import com.winterhavenmc.roadblock.commands.CommandManager;
import com.winterhavenmc.roadblock.highlights.HighlightManager;
import com.winterhavenmc.roadblock.listeners.BlockEventListener;
import com.winterhavenmc.roadblock.listeners.EntityEventListener;
import com.winterhavenmc.roadblock.messages.Macro;
import com.winterhavenmc.roadblock.messages.MessageId;
import com.winterhavenmc.roadblock.storage.BlockManager;
import com.winterhavenmc.roadblock.util.MetricsHandler;
import org.bukkit.plugin.java.JavaPlugin;


public final class PluginMain extends JavaPlugin
{
	public MessageBuilder<MessageId, Macro> messageBuilder;
	public WorldManager worldManager;
	public SoundConfiguration soundConfig;
	public BlockManager blockManager;
	public HighlightManager highlightManager;


	@Override
	public void onEnable()
	{
		// install default config.yml if not present
		saveDefaultConfig();

		// instantiate message builder
		messageBuilder = new MessageBuilder<>(this);

		// instantiate sound configuration
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate block manager
		blockManager = new BlockManager(this);

		// instantiate highlight manager
		highlightManager = new HighlightManager(this);

		// instantiate command manager
		new CommandManager(this);

		// instantiate event listeners
		new BlockEventListener(this);
		new EntityEventListener(this);

		// bStats
		new MetricsHandler(this);
	}


	@Override
	public void onDisable()
	{
		// close datastore
		blockManager.close();
	}

}
