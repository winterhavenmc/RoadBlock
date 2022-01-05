package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.sounds.SoundId;
import com.winterhavenmc.roadblock.messages.MessageId;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.*;


public class MaterialsCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	MaterialsCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("materials");
		this.setUsage("/roadblock materials");
		this.setDescription(MessageId.COMMAND_HELP_MATERIALS);
		this.setMaxArgs(0);
	}

	@Override
	public boolean onCommand(final CommandSender sender, final List<String> argsList) {

		// check that sender has permission for status command
		if (!sender.hasPermission("roadblock.materials")) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_MATERIALS_PERMISSION).send();
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

		List<Material> materialsSorted = new ArrayList<>(plugin.blockManager.getRoadBlockMaterials());

		materialsSorted.sort(Comparator.comparing(Enum::toString));

		sender.sendMessage(ChatColor.GREEN + "Configured materials: "
				+ ChatColor.RESET + materialsSorted);

		return true;
	}
}
