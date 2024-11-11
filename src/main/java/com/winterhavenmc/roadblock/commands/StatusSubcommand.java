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
import com.winterhavenmc.roadblock.sounds.SoundId;

import com.winterhavenmc.roadblock.messages.MessageId;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;


final class StatusSubcommand extends AbstrtactSubcommand {

	private final PluginMain plugin;


	StatusSubcommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "status";
		this.usageString = "/roadblock status";
		this.description = MessageId.COMMAND_HELP_STATUS;
		this.permissionNode = "roadblock.status";
		this.maxArgs = 0;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList) {

		// check that sender has permission for status command
		if (!sender.hasPermission(permissionNode)) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_STATUS_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs()) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// display plugin info and config settings
		displayPluginVersion(sender);
		displayDebugSetting(sender);
		displayProfileSetting(sender);
		displayTotalBlocksProtected(sender);
		displaySpreadDistanceSetting(sender);
		displayShowDistanceSetting(sender);
		displayNoPlaceHeightSetting(sender);
		displayPlayerOnRoadHeightSetting(sender);
		displayMobTargetDistanceSetting(sender);
		displaySnowPlowSetting(sender);
		displaySpeedBoostSetting(sender);
		displayEnabledWorlds(sender);

		return true;
	}


	private void displayPluginVersion(final CommandSender sender) {
		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_GRAY + "["
				+ ChatColor.YELLOW + plugin.getName() + ChatColor.DARK_GRAY + "] "
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + versionString);
	}


	private void displayDebugSetting(final CommandSender sender) {
		if (plugin.getConfig().getBoolean("debug")) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}
	}


	private void displayProfileSetting(final CommandSender sender) {
		if (plugin.getConfig().getBoolean("profile")) {
			sender.sendMessage(ChatColor.DARK_RED + "PROFILE: true");
		}
	}


	private void displayTotalBlocksProtected(final CommandSender sender) {
		if (plugin.getConfig().getBoolean("display-total")) {
			sender.sendMessage(ChatColor.GREEN + "Total blocks protected: "
					+ ChatColor.RESET + plugin.blockManager.getBlockTotal() + " blocks");
		}
	}


	private void displaySpreadDistanceSetting(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Spread distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("spread-distance") + " blocks");
	}


	private void displayShowDistanceSetting(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Show distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("show-distance") + " blocks");
	}


	private void displayNoPlaceHeightSetting(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "No place height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("no-place-height") + " blocks");
	}


	private void displayPlayerOnRoadHeightSetting(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Player on road height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("on-road-height") + " blocks");
	}


	private void displayMobTargetDistanceSetting(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Mob targeting distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("target-distance") + " blocks");
	}


	private void displaySnowPlowSetting(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Snow plow: "
				+ ChatColor.RESET + plugin.getConfig().getString("snow-plow"));
	}


	private void displaySpeedBoostSetting(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Speed boost: "
				+ ChatColor.RESET + plugin.getConfig().getString("speed-boost"));
	}


	private void displayEnabledWorlds(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Enabled worlds: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());
	}

}
