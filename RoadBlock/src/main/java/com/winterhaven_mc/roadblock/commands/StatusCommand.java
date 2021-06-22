package com.winterhaven_mc.roadblock.commands;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.messages.Message;
import com.winterhaven_mc.roadblock.sounds.SoundId;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.roadblock.messages.MessageId.*;


public class StatusCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	StatusCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("status");
		this.setUsage("/roadblock status");
		this.setDescription(COMMAND_HELP_STATUS);
		this.setMaxArgs(0);
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> argsList) {

		// check that sender has permission for status command
		if (!sender.hasPermission("roadblock.status")) {
			Message.create(sender, COMMAND_FAIL_STATUS_PERMISSION).send(plugin.LanguageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs()) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send(plugin.LanguageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		String versionString = this.plugin.getDescription().getVersion();
		sender.sendMessage(ChatColor.DARK_GRAY + "["
				+ ChatColor.YELLOW + plugin.getName() + ChatColor.DARK_GRAY + "] "
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + versionString);

		if (plugin.debug) {
			sender.sendMessage(ChatColor.DARK_RED + "DEBUG: true");
		}

		if (plugin.profile) {
			sender.sendMessage(ChatColor.DARK_RED + "PROFILE: true");
		}

		if (plugin.getConfig().getBoolean("display-total")) {
			sender.sendMessage(ChatColor.GREEN + "Total blocks protected: "
					+ ChatColor.RESET + plugin.blockManager.getBlockTotal() + " blocks");
		}

		sender.sendMessage(ChatColor.GREEN + "Spread distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("spread-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Show distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("show-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "No place height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("no-place-height") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Player on road height: "
				+ ChatColor.RESET + plugin.getConfig().getInt("on-road-height") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Mob targeting distance: "
				+ ChatColor.RESET + plugin.getConfig().getInt("target-distance") + " blocks");

		sender.sendMessage(ChatColor.GREEN + "Snow plow: "
				+ ChatColor.RESET + plugin.getConfig().getString("snow-plow"));

		sender.sendMessage(ChatColor.GREEN + "Enabled worlds: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());

		return true;
	}
}
