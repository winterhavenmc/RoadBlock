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

package com.winterhavenmc.roadblock.core.commands;

import com.winterhavenmc.roadblock.core.PluginController;
import com.winterhavenmc.roadblock.core.util.MessageId;
import com.winterhavenmc.roadblock.core.util.SoundId;
import com.winterhavenmc.roadblock.core.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;


final class StatusSubcommand extends AbstrtactSubcommand
{
	private final PluginController.CommandContextContainer ctx;


	StatusSubcommand(final PluginController.CommandContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "status";
		this.usageString = "/roadblock status";
		this.description = MessageId.COMMAND_HELP_STATUS;
		this.permissionNode = "roadblock.status";
		this.maxArgs = 0;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList)
	{
		// check that sender has permission for status command
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_STATUS_PERMISSION).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs())
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_ARGS_COUNT_OVER).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// display Bootstrap info and config settings
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


	private void displayPluginVersion(final CommandSender sender)
	{
		String versionString = ctx.plugin().getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_GRAY + "["
				+ ChatColor.YELLOW + ctx.plugin().getName() + ChatColor.DARK_GRAY + "] "
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + versionString);
	}


	private void displayDebugSetting(final CommandSender sender)
	{
		if (Config.DEBUG.getBoolean(ctx.plugin().getConfig()))
		{
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: " + Config.DEBUG.getBoolean(ctx.plugin().getConfig()));
		}
	}


	private void displayProfileSetting(final CommandSender sender)
	{
		if (Config.PROFILE.getBoolean(ctx.plugin().getConfig()))
		{
			sender.sendMessage(ChatColor.DARK_RED + "PROFILE: " + Config.PROFILE.getBoolean(ctx.plugin().getConfig()));
		}
	}


	private void displayTotalBlocksProtected(final CommandSender sender)
	{
		if (Config.DISPLAY_TOTAL.getBoolean(ctx.plugin().getConfig()))
		{
			sender.sendMessage(ChatColor.GREEN + "Total blocks protected: "
					+ ChatColor.RESET + ctx.blockManager().getBlockTotal() + " blocks");
		}
	}


	private void displaySpreadDistanceSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Spread distance: "
				+ ChatColor.RESET + Config.SPREAD_DISTANCE.getInt(ctx.plugin().getConfig()) + " blocks");
	}


	private void displayShowDistanceSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Show distance: "
				+ ChatColor.RESET + Config.SHOW_DISTANCE.getInt(ctx.plugin().getConfig()) + " blocks");
	}


	private void displayNoPlaceHeightSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "No place height: "
				+ ChatColor.RESET + Config.NO_PLACE_HEIGHT.getInt(ctx.plugin().getConfig()) + " blocks");
	}


	private void displayPlayerOnRoadHeightSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Player on road height: "
				+ ChatColor.RESET + Config.ON_ROAD_HEIGHT.getInt(ctx.plugin().getConfig()) + " blocks");
	}


	private void displayMobTargetDistanceSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Mob targeting distance: "
				+ ChatColor.RESET + Config.TARGET_DISTANCE.getInt(ctx.plugin().getConfig()) + " blocks");
	}


	private void displaySnowPlowSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Snow plow: "
				+ ChatColor.RESET + Config.SNOW_PLOW.getBoolean(ctx.plugin().getConfig()));
	}


	private void displaySpeedBoostSetting(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Speed boost: "
				+ ChatColor.RESET + Config.SPEED_BOOST.getBoolean(ctx.plugin().getConfig()));
	}


	private void displayEnabledWorlds(final CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + "Enabled worlds: "
				+ ChatColor.RESET + ctx.worldManager().getEnabledWorldNames().toString());
	}

}
