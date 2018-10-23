package com.winterhaven_mc.roadblock.utilities;

import com.winterhaven_mc.roadblock.PluginMain;
import com.winterhaven_mc.roadblock.messages.MessageId;
import com.winterhaven_mc.util.LanguageManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public abstract class AbstractMessageManager {

	// reference to main class
	protected PluginMain plugin;

	// cooldown hash map
	private Map<UUID,EnumMap<MessageId,Long>> messageCooldownMap;

	// message file helper
	private LanguageManager languageManager;

	// configuration object for messages
	private YamlConfiguration messages;


	/**
	 * Constructor method for class
	 *
	 * @param plugin reference to main class
	 */
	protected AbstractMessageManager(final PluginMain plugin) {

		// create pointer to main class
		this.plugin = plugin;

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<>();

		// instantiate language manager
		this.languageManager = new LanguageManager(plugin);

		// load messages from file
		this.messages = languageManager.loadMessages();
	}


	/**
	 * Add entry to message cooldown map
	 * @param player the player whose uuid will be added as a key to the cooldown map
	 * @param messageId the message id to use as a key in the cooldown map
	 */
	private void putMessageCooldown(final Player player, final MessageId messageId) {

		// create new EnumMap with MessageId as key type
		EnumMap<MessageId,Long> tempMap = new EnumMap<>(MessageId.class);

		// put current time in EnumMap with messageId as key
		tempMap.put(messageId, System.currentTimeMillis());

		// put EnumMap in cooldown map with PlayerUUID as key
		messageCooldownMap.put(player.getUniqueId(), tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player the player for whom to retrieve cooldown time
	 * @param messageId the message identifier for which retrieve cooldown time
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
	protected boolean isEnabled(MessageId messageId) {
		return messages.getBoolean("messages." + messageId.toString() + ".enabled");
	}


	/**
	 * get message repeat delay from language file
	 * @param messageId message identifier to retrieve message delay
	 * @return int message repeat delay in seconds
	 */
	private long getRepeatDelay(MessageId messageId) {
		return messages.getLong("messages." + messageId.toString() + ".repeat-delay");
	}


	/**
	 * get message text from language file
	 * @param messageId message identifier to retrieve message text
	 * @return String message text
	 */
	protected String getMessage(MessageId messageId) {
		return messages.getString("messages." + messageId.toString() + ".string");
	}


	protected String getWorldName(CommandSender recipient) {

		// get default world name
		String worldName = plugin.getServer().getWorlds().get(0).getName();

		// if sender is entity, return entity world name
		if (recipient instanceof Entity) {
			Entity entity = (Entity) recipient;
			worldName = entity.getWorld().getName();
		}

		// return worldName
		return worldName;
	}


	protected boolean isCooled(CommandSender recipient, MessageId messageId) {

		// if recipient is a player...
		if (recipient instanceof Player) {

			// cast sender to Player
			Player player = (Player) recipient;

			// get message cooldown time remaining
			long lastDisplayed = getMessageCooldown(player, messageId);

			// get message repeat delay
			long messageRepeatDelay = getRepeatDelay(messageId);

			// if message has repeat delay value and was displayed to player more recently, do nothing and return
			if (lastDisplayed > System.currentTimeMillis() - messageRepeatDelay * 1000) {
				return false;
			}

			// if repeat delay value is greater than zero, add entry to messageCooldownMap
			if (messageRepeatDelay > 0) {
				putMessageCooldown(player, messageId);
			}
		}
		return true;
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

		// get tool name from language file
		String toolName = messages.getString("tool_info.TOOL_NAME");

		// if fetching tool name failed, set to default name
		if (toolName == null) {
			toolName = "RoadBlock Tool";
			plugin.getLogger().warning("Could not fetch tool name from language file. Using default.");
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


	/**
	 * replace substrings within string.
	 */
	@SuppressWarnings("StringBufferMayBeStringBuilder")
	protected static String replace(final String s, final String sub, final String with) {
		int c = 0;
		int i = s.indexOf(sub, c);
		if (i == -1)
			return s;

		StringBuffer buf = new StringBuffer(s.length() + with.length());

		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (buf) {
			do {
				buf.append(s, c, i);
				buf.append(with);
				c = i + sub.length();
			} while ((i = s.indexOf(sub, c)) != -1);

			if (c < s.length())
				buf.append(s.substring(c));

			return buf.toString();
		}
	}

}
