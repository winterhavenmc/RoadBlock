package com.winterhaven_mc.roadblock.commands;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.sounds.SoundId;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.roadblock.messages.MessageId.*;
import static com.winterhaven_mc.roadblock.messages.MessageId.COMMAND_SUCCESS_RELOAD;


public class ReloadCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	ReloadCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("reload");
		this.setUsage("/roadblock reload");
		this.setDescription(COMMAND_HELP_RELOAD);
		this.setMaxArgs(0);
	}


	@Override
	public boolean onCommand(CommandSender sender, List<String> argsList) {

		// check that sender has permission for reload command
		if (!sender.hasPermission("roadblock.reload")) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_RELOAD_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs()) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// re-install config file if necessary
		plugin.saveDefaultConfig();

		// reload config file
		plugin.reloadConfig();

		// update road block materials list
		plugin.blockManager.reload();

		// reload messages
		plugin.messageBuilder.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload enabled worlds
		plugin.worldManager.reload();

		// send player success message
		plugin.messageBuilder.build(sender, COMMAND_SUCCESS_RELOAD).send();

		return true;
	}

}
