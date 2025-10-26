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

import com.winterhavenmc.roadblock.core.context.CommandCtx;
import com.winterhavenmc.roadblock.core.util.Macro;
import com.winterhavenmc.roadblock.core.util.MessageId;
import com.winterhavenmc.roadblock.core.util.SoundId;
import com.winterhavenmc.roadblock.core.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;


final class StatusSubcommand extends AbstrtactSubcommand
{
	private final CommandCtx ctx;


	StatusSubcommand(final CommandCtx ctx)
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
		displayStatusHeader(sender);
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
		displayStatusFooter(sender);

		return true;
	}


	private void displayStatusHeader(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_HEADER)
				.setMacro(Macro.PLUGIN, ctx.plugin().getDescription().getName())
				.send();
	}
	private void displayStatusFooter(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FOOTER)
				.setMacro(Macro.URL, "https://github.com/winterhavenmc/RoadBlock")
				.send();
	}

	private void displayPluginVersion(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_VERSION_SETTING)
				.setMacro(Macro.SETTING, ctx.plugin().getDescription().getVersion())
				.send();
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
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TOTAL_BLOCKS_PROTECTED)
					.setMacro(Macro.SETTING, ctx.blockManager().getBlockTotal())
					.send();
		}
	}


	private void displaySpreadDistanceSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SPREAD_DISTANCE_SETTING)
						.setMacro(Macro.SETTING, Config.SPREAD_DISTANCE.getInt(ctx.plugin().getConfig()));
	}


	private void displayShowDistanceSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SHOW_DISTANCE_SETTING)
				.setMacro(Macro.SETTING, Config.SHOW_DISTANCE.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displayNoPlaceHeightSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_NO_PLACE_HEIGHT_SETTING)
				.setMacro(Macro.SETTING, Config.NO_PLACE_HEIGHT.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displayPlayerOnRoadHeightSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_PLAYER_ON_ROAD_HEIGHT_SETTING)
				.setMacro(Macro.SETTING, Config.ON_ROAD_HEIGHT.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displayMobTargetDistanceSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_MOB_TARGETING_DISTANCE_SETTING)
				.setMacro(Macro.SETTING, Config.TARGET_DISTANCE.getInt(ctx.plugin().getConfig()))
				.send();
	}


	private void displaySnowPlowSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SNOW_PLOW_SETTING)
				.setMacro(Macro.SETTING, Config.SNOW_PLOW.getBoolean(ctx.plugin().getConfig()))
				.send();
	}


	private void displaySpeedBoostSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SPEED_BOOST_SETTING)
				.setMacro(Macro.SETTING, Config.SPEED_BOOST.getBoolean(ctx.plugin().getConfig()))
				.send();
	}


	private void displayEnabledWorlds(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS_SETTING)
				.setMacro(Macro.SETTING, ctx.worldManager().getEnabledWorldNames().toString())
				.send();
	}

}
