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

package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.highlights.HighlightStyle;
import com.winterhavenmc.roadblock.messages.Macro;
import com.winterhavenmc.roadblock.messages.MessageId;
import com.winterhavenmc.roadblock.sounds.SoundId;

import com.winterhavenmc.roadblock.util.Config;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * A class that implements the show subcommand
 */
final class ShowSubcommand extends AbstrtactSubcommand {

	// reference to plugin main class
	private final PluginMain plugin;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to the plugin main class
	 */
	ShowSubcommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "show";
		this.usageString = "/roadblock show <distance>";
		this.description = MessageId.COMMAND_HELP_SHOW;
		this.permissionNode = "roadblock.show";
		this.maxArgs = 1;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList) {

		// sender must be player
		if (!(sender instanceof final Player player)) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// check player permissions
		if (!player.hasPermission(permissionNode)) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_SHOW_PERMISSION).send();
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs()) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// get show distance from config
		int distance = Config.SHOW_DISTANCE.getInt(plugin);

		// if argument passed, try to parse string to int
		if (argsList.size() == 1) {
			try {
				distance = Integer.parseInt(argsList.get(0));
			}
			catch (NumberFormatException nfe) {
				// send player integer parse error message and return
				plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_SET_INVALID_INTEGER).send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);

				// display usage message for player
				displayUsage(player);
				return true;
			}
		}

		// get set of block locations within distance of player location
		Collection<Location> locations = plugin.blockManager.selectNearbyBlocks(player.getLocation(), distance);

		// highlight blocks
		plugin.highlightManager.highlightBlocks(player, locations, HighlightStyle.PROTECT);

		// send player success message
		plugin.messageBuilder.build(player, MessageId.COMMAND_SUCCESS_SHOW).setMacro(Macro.QUANTITY, locations.size()).send();

		// if any blocks highlighted, play sound
		if (!locations.isEmpty()) {
			plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_SHOW);
		}

		return true;
	}
}
