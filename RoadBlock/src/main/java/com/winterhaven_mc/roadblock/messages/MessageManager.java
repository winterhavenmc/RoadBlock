package com.winterhaven_mc.roadblock.messages;

import com.winterhaven_mc.roadblock.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import util.AbstractMessageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implements message manager for RoadBlock plugin.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public final class MessageManager extends AbstractMessageManager {


	/**
	 * Constructor
	 * @param plugin reference to main class
	 */
	public MessageManager(PluginMain plugin) {

		// call super class constructor
		//noinspection unchecked
		super(plugin, MessageId.class);
	}


	@Override
	protected Map<String,String> getDefaultReplacements(CommandSender recipient) {

		Map<String,String> replacements = new HashMap<>();
		replacements.put("%PLAYER_NAME%",recipient.getName());
		replacements.put("%WORLD_NAME%",ChatColor.stripColor(getWorldName(recipient)));
		replacements.put("%TOOL_NAME%", ChatColor.stripColor(getToolName()));
		replacements.put("%QUANTITY%","1");
		replacements.put("%MATERIAL%","unknown");

		return replacements;
	}


	/**
	 *  Send message to player
	 *
	 * @param recipient			player receiving message
	 * @param messageId			message identifier in messages file
	 */
	public final void sendMessage(final CommandSender recipient,
								  final MessageId messageId) {

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
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
								  final Integer quantity) {

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// set passed quantity in replacements map
		replacements.put("%QUANTITY%",quantity.toString());

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
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

		// get default replacements map
		Map<String,String> replacements = getDefaultReplacements(recipient);

		// set name of passed material in replacements map
		replacements.put("%MATERIAL%",material.toString());

		// send message to recipient
		//noinspection unchecked
		sendMessage(recipient, messageId, replacements);
	}


	/**
	 * Get custom tool name from language file
	 * @return the custom tool name string
	 */
	public final String getToolName() {

		// get tool name from language file
		String toolName = messages.getString("tool_info.TOOL_NAME");

		// if fetching tool name failed, use plugin name
		if (toolName == null) {
			toolName = plugin.getName() + " Tool";
		}

		// replace alt color code character
		toolName = ChatColor.translateAlternateColorCodes('&', toolName);
		return toolName;
	}


	/**
	 * Get custom tool lore from language file
	 * @return the custom tool lore as a List of String
	 */
	public final List<String> getToolLore() {
		List<String> lore = messages.getStringList("tool_info.TOOL_LORE");
		int lineNumber = 0;
		while (lineNumber < lore.size()) {
			lore.set(lineNumber, ChatColor.translateAlternateColorCodes('&',lore.get(lineNumber)));
			lineNumber++;
		}
		return lore;
	}


}
