package com.winterhaven_mc.roadblock.messages;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.utilities.AbstractMessageManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;


/**
 * Implements message manager for RoadBlock plugin.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public final class MessageManager extends AbstractMessageManager {

	// inherit constructor from super class
	public MessageManager(PluginMain plugin) {
		super(plugin);
	}


	/**
	 *  Send message to player
	 *
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 */
	public final void sendMessage(final CommandSender recipient,
								  final MessageId messageId) {

		sendMessage(recipient, messageId, 1, null);
	}


	/**
	 *  Send message to player
	 *
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			quantity
	 */
	public final void sendMessage(final CommandSender recipient,
								  final MessageId messageId,
								  final int quantity) {

		sendMessage(recipient, messageId, quantity, null);
	}


	/**
	 *  Send message to player
	 *
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param material			material type to be referenced in message
	 */
	public final void sendMessage(final CommandSender recipient,
								  final MessageId messageId,
								  final Material material) {

		sendMessage(recipient, messageId, 1, material);
	}


	/**
	 *  Send message to player
	 *
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			quantity
	 * @param material			material type to be referenced in message
	 */
	@SuppressWarnings("WeakerAccess")
	public final void sendMessage(final CommandSender recipient,
	                                    final MessageId messageId,
										final Integer quantity,
										final Material material) {


		// if message is not enabled in messages file, do nothing and return
		if (!isEnabled(messageId)) {
			if (plugin.debug) {
				plugin.getLogger().info("Message " + messageId.toString() + "disabled, not displayed.");
			}
			return;
		}

		// if message is not cooled, do nothing and return
		if (!isCooled(recipient,messageId)) {
			return;
		}

		String materialName = "unknown";

		// if passed material is not null, set materialName
		if (material != null) {
			materialName = material.toString();
		}

		// get recipient's current world name
		String worldName = getWorldName(recipient);

		// use Multiverse alias for world name if available
		worldName = plugin.worldManager.getWorldName(worldName);

		// get message from file
		String message = getMessage(messageId);

		// do variable substitutions *
		if (message.contains("%")) {
			message = replace(message,"%PLAYER_NAME%",recipient.getName());
			message = replace(message,"%WORLD_NAME%", ChatColor.stripColor(worldName));
			message = replace(message,"%TOOL_NAME%",ChatColor.stripColor(getToolName()));
			message = replace(message,"%QUANTITY%", quantity.toString());
			message = replace(message,"%MATERIAL%", materialName);
		}

		// send message to player
		recipient.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
	}

}
