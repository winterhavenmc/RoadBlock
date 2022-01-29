package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.messages.MessageId;
import com.winterhavenmc.roadblock.sounds.SoundId;
import com.winterhavenmc.roadblock.util.RoadBlockTool;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * A class that implements the tool subcommand
 */
final class ToolCommand extends SubcommandAbstract {

	// reference to the plugin main class
	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to the plugin main class
	 */
	ToolCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "tool";
		this.usageString = "/roadblock tool";
		this.description = MessageId.COMMAND_HELP_TOOL;
		this.maxArgs = 0;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList) {

		// sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// cast sender to player
		final Player player = (Player) sender;

		// check player permissions
		if (!player.hasPermission("roadblock.tool")) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_TOOL_PERMISSION).send();
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

		// create road block tool itemStack
		final ItemStack roadBlockTool = RoadBlockTool.create();

		// put tool in player's inventory
		final HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(roadBlockTool);

		// if no room in inventory, send message
		if (!noFit.isEmpty()) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_TOOL_INVENTORY_FULL).send();
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// play success sound
		plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_TOOL);

		return true;
	}
}
