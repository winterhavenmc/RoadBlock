package com.winterhaven_mc.roadblock.messages;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.util.AbstractMessageManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
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
		replacements.put("%TOOL_NAME%", ChatColor.stripColor(getItemName()));
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

}
