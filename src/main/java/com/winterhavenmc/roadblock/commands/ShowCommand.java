package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.PluginMain;
import com.winterhavenmc.roadblock.highlights.HighlightStyle;
import com.winterhavenmc.roadblock.sounds.SoundId;
import com.winterhavenmc.roadblock.messages.Macro;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.winterhavenmc.roadblock.messages.MessageId.*;


public class ShowCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	ShowCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.setName("show");
		this.setUsage("/roadblock show <distance>");
		this.setDescription(COMMAND_HELP_SHOW);
		this.setMaxArgs(1);
	}


	@Override
	public boolean onCommand(CommandSender sender, List<String> argsList) {

		// sender must be player
		if (!(sender instanceof Player)) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_CONSOLE).send();
			return true;
		}

		// get player from sender
		final Player player = (Player) sender;

		// check player permissions
		if (!player.hasPermission("roadblock.show")) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_SHOW_PERMISSION).send();
			plugin.soundConfig.playSound(player, SoundId.COMMAND_FAIL);
			return true;
		}

		// check max arguments
		if (argsList.size() > getMaxArgs()) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_ARGS_COUNT_OVER).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			displayUsage(sender);
			return true;
		}

		// get show distance from config
		int distance = plugin.getConfig().getInt("show-distance");

		// if argument passed, try to parse string to int
		if (argsList.size() == 1) {
			try {
				distance = Integer.parseInt(argsList.get(0));
			}
			catch (NumberFormatException nfe) {
				// send player integer parse error message and return
				plugin.messageBuilder.build(sender, COMMAND_FAIL_SET_INVALID_INTEGER).send();
				plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);

				// display usage message for player
				displayUsage(player);
				return true;
			}
		}

		// get set of block locations within distance of player location
		Collection<Location> locations = plugin.blockManager.selectNearbyBlocks(player.getLocation(), distance);

		// highlight blocks
		plugin.highlightManager.highlightBlocks(player, locations, HighlightStyle.PROTECT);

		// send player success message
		plugin.messageBuilder.build(player, COMMAND_SUCCESS_SHOW).setMacro(Macro.QUANTITY, locations.size()).send();

		// if any blocks highlighted, play sound
		if (locations.size() > 0) {
			plugin.soundConfig.playSound(player, SoundId.COMMAND_SUCCESS_SHOW);
		}

		return true;
	}
}
