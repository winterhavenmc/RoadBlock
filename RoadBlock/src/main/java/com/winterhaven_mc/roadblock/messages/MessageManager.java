package com.winterhaven_mc.roadblock.messages;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.util.LanguageManager;
import com.winterhaven_mc.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implements message manager for RoadBlock plugin.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public final class MessageManager {

	// reference to main class
	private final PluginMain plugin;

	// cooldown hash map
	private final ConcurrentHashMap<UUID, EnumMap<MessageId,Long>> messageCooldownMap;

	// message file helper
	private LanguageManager languageManager;

	// configuration object for messages
	private YamlConfiguration messages;


	/**
	 * Constructor method for class
	 * 
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// create pointer to main class
		this.plugin = plugin;

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<>();

		// instantiate messageFileHelper
		this.languageManager = new LanguageManager(plugin);

		// load messages from file
		this.messages = languageManager.loadMessages();
	}


	public final void sendPlayerMessage(final CommandSender sender,
										final MessageId messageId) {
		sendPlayerMessage(sender, messageId, 1, null);
	}


	@SuppressWarnings("unused")
	public final void sendPlayerMessage(final CommandSender sender,
										final MessageId messageId,
										final int quantity) {
		sendPlayerMessage(sender, messageId, quantity, null);
	}

	
	@SuppressWarnings({"SameParameterValue", "unused"})
	public final void sendPlayerMessage(final CommandSender sender,
										final MessageId messageId,
										final Material material) {
		sendPlayerMessage(sender, messageId, 1, material);
	}


	/**
	 *  Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			quantity
	 * @param material			material type to be referenced in message
	 */
	@SuppressWarnings("WeakerAccess")
	public final void sendPlayerMessage(final CommandSender sender,
	                                    final MessageId messageId,
										final Integer quantity,
										final Material material) {

		// if message is not enabled in messages file, do nothing and return
		if (!isEnabled(messageId)) {
			return;
		}

		// set substitution variable defaults			
		String playerName = sender.getName();
		String worldName = plugin.getServer().getWorlds().get(0).getName();
		String materialName = "unknown";

		if (material != null) {
			materialName = material.toString();
		}

		// if sender is a player...
		if (sender instanceof Player) {

			Player player = (Player) sender;

			// get message cooldown time remaining
			long lastDisplayed = getMessageCooldown(player,messageId);

			// get message repeat delay
			int messageRepeatDelay = getRepeatDelay(messageId);

			// if message has repeat delay value and was displayed to player more recently, do nothing and return
			if (lastDisplayed > System.currentTimeMillis() - messageRepeatDelay * 1000) {
				return;
			}

			// if repeat delay value is greater than zero, add entry to messageCooldownMap
			if (messageRepeatDelay > 0) {
				putMessageCooldown(player,messageId);
			}

			// assign player dependent variables
			playerName = player.getName();
			worldName = player.getWorld().getName();
		}

		// get message from file
		String message = getMessage(messageId);

		// get world name from worldManager
		worldName = plugin.worldManager.getWorldName(worldName);
		
		// do variable substitutions
		if (message.contains("%")) {
			message = StringUtil.replace(message,"%PLAYER_NAME%",playerName);
			message = StringUtil.replace(message,"%WORLD_NAME%", ChatColor.stripColor(worldName));
			message = StringUtil.replace(message,"%TOOL_NAME%",ChatColor.stripColor(getToolName()));
			message = StringUtil.replace(message,"%QUANTITY%", quantity.toString());
			message = StringUtil.replace(message,"%MATERIAL%", materialName);
		}

		// send message to player
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
	}


	/**
	 * Add entry to message cooldown map
	 * @param player the player whose uuid will be added as a key to the cooldown map
	 * @param messageId the message id to use as a key in the cooldown map
	 */
	private void putMessageCooldown(final Player player, final MessageId messageId) {

		final EnumMap<MessageId, Long> tempMap = new EnumMap<>(MessageId.class);
		tempMap.put(messageId, System.currentTimeMillis());
		messageCooldownMap.put(player.getUniqueId(), tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player the player whose uuid will be used to retrieve a message expire time from the cooldown map
	 * @param messageId the message id to use as a key to retrieve a message expire time from the cooldown map
	 * @return cooldown expire time
	 */
	private long getMessageCooldown(final Player player, final MessageId messageId) {

		// check if player is in message cooldown hashmap
		if (messageCooldownMap.containsKey(player.getUniqueId())) {

			// check if messageID is in player's cooldown hashmap
			if (messageCooldownMap.get(player.getUniqueId()).containsKey(messageId)) {

				// return cooldown time
				return messageCooldownMap.get(player.getUniqueId()).get(messageId);
			}
		}
		return 0L;
	}


	/**
	 * Check if message is enabled
	 * @param messageId message identifier to check
	 * @return true if message is enabled, false if not
	 */
	private boolean isEnabled(MessageId messageId) {
		return !messages.getBoolean("messages." + messageId.toString() + ".enabled");
	}


	/**
	 * get message repeat delay from language file
	 * @param messageId message identifier to retrieve message delay
	 * @return int message repeat delay in seconds
	 */
	private int getRepeatDelay(MessageId messageId) {
		return messages.getInt("messages." + messageId.toString() + ".repeat-delay");
	}


	/**
	 * get message text from language file
	 * @param messageId message identifier to retrieve message text
	 * @return String message text
	 */
	private String getMessage(MessageId messageId) {
		return messages.getString("messages." + messageId.toString() + ".text");
	}


	/**
	 * Reload custom messages file
	 */
	public final void reload() {

		// reload messages
		this.messages = languageManager.loadMessages();
	}


	/**
	 * Get custom tool name from language file
	 * @return the custom tool name string
	 */
	public final String getToolName() {
		return ChatColor.translateAlternateColorCodes('&',messages.getString("TOOL_NAME"));
	}


	/**
	 * Get custom tool lore from language file
	 * @return the custom tool lore as a List of String
	 */
	public final List<String> getToolLore() {
		List<String> lore = messages.getStringList("TOOL_LORE");
		int lineNumber = 0;
		while (lineNumber < lore.size()) {
			lore.set(lineNumber, ChatColor.translateAlternateColorCodes('&',lore.get(lineNumber)));
			lineNumber++;
		}
		return lore;
	}

}
