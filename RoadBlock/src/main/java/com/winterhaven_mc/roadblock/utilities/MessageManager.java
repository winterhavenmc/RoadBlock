package com.winterhaven_mc.roadblock.utilities;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.util.ConfigAccessor;
import com.winterhaven_mc.util.LanguageManager;
import com.winterhaven_mc.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

	// language manager
	private LanguageManager languageManager;

	// custom message file handler
	private ConfigAccessor messages;

	// cooldown hash map
	private final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> messageCooldownMap;


	/**
	 * Constructor method for class
	 * 
	 * @param plugin reference to main class
	 */
	public MessageManager(final PluginMain plugin) {

		// create pointer to main class
		this.plugin = plugin;

		// instantiate language manager
		languageManager = new LanguageManager(plugin);

		// instantiate custom configuration manager for configured language file
		this.messages = new ConfigAccessor(plugin, languageManager.getFileName());

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<>();
	}


	public final void sendPlayerMessage(final CommandSender sender, final String messageId) {
		sendPlayerMessage(sender, messageId, 1, null);
	}


	public final void sendPlayerMessage(final CommandSender sender, final String messageId, final int quantity) {
		sendPlayerMessage(sender, messageId, quantity, null);
	}

	
	public final void sendPlayerMessage(final CommandSender sender, final String messageId, final Material material) {
		sendPlayerMessage(sender, messageId, 1, material);
	}


	/**
	 *  Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			quantity
	 */
	@SuppressWarnings("WeakerAccess")
	public final void sendPlayerMessage(final CommandSender sender, final String messageId,
										final Integer quantity, final Material material) {

		// if message is not enabled in messages file, do nothing and return
		if (!messages.getConfig().getBoolean(messageId + ".enabled")) {
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
			Long lastDisplayed = getMessageCooldown(player,messageId);

			// get message repeat delay
			int messageRepeatDelay = messages.getConfig().getInt(messageId + ".repeat-delay");

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
		String message = messages.getConfig().getString(messageId + ".text");

		if (message == null || message.isEmpty()) {
			message = messageId;
		}

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
	private void putMessageCooldown(final Player player, final String messageId) {

		final ConcurrentHashMap<String, Long> tempMap = new ConcurrentHashMap<>();
		tempMap.put(messageId, System.currentTimeMillis());
		messageCooldownMap.put(player.getUniqueId(), tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player the player whose uuid will be used to retrieve a message expire time from the cooldown map
	 * @param messageId the message id to use as a key to retrieve a message expire time from the cooldown map
	 * @return cooldown expire time
	 */
	private long getMessageCooldown(final Player player, final String messageId) {

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
	 * Reload custom messages file
	 */
	public final void reload() {

		// reload language file
		this.languageManager.reload(messages);
	}


	/**
	 * Get custom tool name from language file
	 * @return the custom tool name string
	 */
	final String getToolName() {
		return ChatColor.translateAlternateColorCodes('&',messages.getConfig().getString("TOOL_NAME"));
	}


	/**
	 * Get custom tool lore from language file
	 * @return the custom tool lore as a List of String
	 */
	final List<String> getToolLore() {
		List<String> lore = messages.getConfig().getStringList("TOOL_LORE");
		int lineNumber = 0;
		while (lineNumber < lore.size()) {
			lore.set(lineNumber, ChatColor.translateAlternateColorCodes('&',lore.get(lineNumber)));
			lineNumber++;
		}
		return lore;
	}

}
