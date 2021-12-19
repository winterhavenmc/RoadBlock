package com.winterhaven_mc.roadblock.commands;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.messages.Message;
import com.winterhaven_mc.roadblock.sounds.SoundId;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.*;

import static com.winterhaven_mc.roadblock.messages.MessageId.*;


public class MaterialsCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	MaterialsCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("materials");
		this.setUsage("/roadblock materials");
		this.setDescription(COMMAND_HELP_MATERIALS);
		this.setMaxArgs(0);
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> argsList) {

		// check that sender has permission for status command
		if (!sender.hasPermission("roadblock.materials")) {
			Message.create(sender, COMMAND_FAIL_MATERIALS_PERMISSION).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs()) {
			Message.create(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		List<Material> materialsSorted = new ArrayList<>(plugin.blockManager.getRoadBlockMaterials());

		materialsSorted.sort(Comparator.comparing(Enum::toString));

		sender.sendMessage(ChatColor.GREEN + "Configured materials: "
				+ ChatColor.RESET + materialsSorted.toString());

		return true;
	}
}
