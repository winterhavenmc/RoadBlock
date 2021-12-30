package com.winterhaven_mc.roadblock.commands;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.sounds.SoundId;
import com.winterhaven_mc.roadblock.util.RoadBlockTool;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.roadblock.messages.MessageId.*;


public class ToolCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	ToolCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("tool");
		this.setUsage("/roadblock tool");
		this.setDescription(COMMAND_HELP_TOOL);
		this.setMaxArgs(0);
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> argsList) {

		// sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_CONSOLE).send(plugin.languageHandler);
			return true;
		}

		// cast sender to player
		final Player player = (Player) sender;

		// check player permissions
		if (!player.hasPermission("roadblock.tool")) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_TOOL_PERMISSION).send(plugin.languageHandler);
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs()) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// create road block tool itemStack
		final ItemStack rbTool = RoadBlockTool.create();

		// put tool in player's inventory
		final HashMap<Integer, ItemStack> noFit = player.getInventory().addItem(rbTool);

		// if no room in inventory, send message
		if (!noFit.isEmpty()) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_TOOL_INVENTORY_FULL).send(plugin.languageHandler);
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// play success sound
		plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_TOOL);

		return true;
	}
}
