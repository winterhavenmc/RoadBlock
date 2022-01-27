package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.sounds.SoundId;
import com.winterhavenmc.roadblock.messages.MessageId;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;


/**
 * A class that implements the reload subcommand
 */
final class ReloadCommand extends AbstractSubcommand {

	// reference to the plugin main class
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to the plugin main class
	 */
	ReloadCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("reload");
		this.setUsage("/roadblock reload");
		this.setDescription(MessageId.COMMAND_HELP_RELOAD);
		this.setMaxArgs(0);
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList) {

		// check that sender has permission for reload command
		if (!sender.hasPermission("roadblock.reload")) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_RELOAD_PERMISSION).send();
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
		plugin.messageBuilder.build(sender, MessageId.COMMAND_SUCCESS_RELOAD).send();

		return true;
	}

}
