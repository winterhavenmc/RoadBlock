package com.winterhaven_mc.roadblock.utilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.winterhaven_mc.roadblock.PluginMain;


/**
 * Implements message manager for <code>LodeStar</code>.
 * 
 * @author      Tim Savage
 * @version		1.0
 *  
 */
public class MessageManager {

	// reference to main class
	private final PluginMain plugin;
	
	// custom message file handler
	private ConfigAccessor messages;
	
	// custom sound file handler
	private ConfigAccessor sounds;
	
	// cooldown hash amp
	private ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> messageCooldownMap;
	
	// currently selected language
	private String language;

	MultiverseCore mvCore;
	Boolean mvEnabled = false;


	/**
	 * Constructor method for class
	 * 
	 * @param plugin
	 */
	public MessageManager(final PluginMain plugin) {
		
		// create pointer to main class
		this.plugin = plugin;

		// install localization files
        this.installLocalizationFiles();
		
		// get configured language
		this.language = languageFileExists(plugin.getConfig().getString("language"));

		// instantiate custom configuration manager for language file
		this.messages = new ConfigAccessor(plugin, "language" + File.separator + this.language + ".yml");
		
		// instantiate custom configuration manager for sound file
		this.sounds = new ConfigAccessor(plugin, "sounds.yml");
		
		// install sound file
		this.sounds.saveDefaultConfig();

		// initialize messageCooldownMap
		this.messageCooldownMap = new ConcurrentHashMap<UUID,ConcurrentHashMap<String,Long>>();
		
		// get reference to Multiverse-Core if installed
		mvCore = (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
		if (mvCore != null && mvCore.isEnabled()) {
			plugin.getLogger().info("Multiverse-Core detected.");
			this.mvEnabled = true;
		}
    }

    public void sendPlayerMessage(final CommandSender sender, final String messageId) {
    	sendPlayerMessage(sender, messageId, 1, null);
    }
    
    public void sendPlayerMessage(final CommandSender sender, final String messageId, final int quantity) {
    	sendPlayerMessage(sender, messageId, quantity, null);
    }
    
    public void sendPlayerMessage(final CommandSender sender, final String messageId, final Material material) {
    	sendPlayerMessage(sender, messageId, 1, material);
    }
    

	/**
	 *  Send message to player
	 * 
	 * @param sender			player receiving message
	 * @param messageId			message identifier in messages file
	 * @param quantity			quantity
	 */
    public void sendPlayerMessage(final CommandSender sender, final String messageId, 
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
		};

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

		// if Multiverse is installed, use Multiverse world alias for world name
		if (mvEnabled && this.mvCore.getMVWorldManager().getMVWorld(worldName) != null) {

			// if Multiverse alias is not blank, set world name to alias
			if (!this.mvCore.getMVWorldManager().getMVWorld(worldName).getAlias().isEmpty()) {
				worldName = this.mvCore.getMVWorldManager().getMVWorld(worldName).getAlias();
			}
		}

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
     * Play sound effect for action
     * @param sender
     * @param soundId
     */
	public void playerSound(final CommandSender sender, final String soundId) {
	
		// if command sender is in game, play sound effect
		if (sender instanceof Player) {
			playerSound((Player)sender,soundId);
		}
	}


	/**
	 * Play sound effect for action
	 * @param player
	 * @param soundId
	 */
	void playerSound(final Player player, final String soundId) {
		
		// if sound effects are disabled in config, do nothing and return
		if (!plugin.getConfig().getBoolean("sound-effects")) {
			return;
		}
		
		// if sound is set to enabled in config file
		if (sounds.getConfig().getBoolean(soundId + ".enabled")) {
			
			// get sound name from config file
			String soundName = sounds.getConfig().getString(soundId + ".sound");
	
			// get sound volume from config file
			float volume = (float) sounds.getConfig().getDouble(soundId + ".volume");
			
			// get sound pitch from config file
			float pitch = (float) sounds.getConfig().getDouble(soundId + ".pitch");
	
			// get player only setting from config file
			boolean playerOnly = sounds.getConfig().getBoolean(soundId + ".player-only");
	
			try {
				// if sound is set player only, use player.playSound()
				if (playerOnly) {
					player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
				}
				// else use world.playSound() so other players in vicinity can hear
				else {
					player.getWorld().playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
				}
			} catch (IllegalArgumentException e) {
				plugin.getLogger().warning("An error occured while trying to play the sound '" + soundName 
						+ "'. You probably need to update the sound name in your config.yml file.");
			}
		}
	}

	
	/**
	 * Add entry to message cooldown map
	 * @param player
	 * @param messageId
	 */
	private void putMessageCooldown(final Player player, final String messageId) {
		
    	ConcurrentHashMap<String, Long> tempMap = new ConcurrentHashMap<String, Long>();
    	tempMap.put(messageId, System.currentTimeMillis());
    	messageCooldownMap.put(player.getUniqueId(), tempMap);
	}


	/**
	 * get entry from message cooldown map
	 * @param player
	 * @param messageId
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
	public void reload() {
		
		// reinstall message files if necessary
		installLocalizationFiles();
		
		// get currently configured language
		String newLanguage = languageFileExists(plugin.getConfig().getString("language"));
		
		// if configured language has changed, instantiate new messages object
		if (!newLanguage.equals(this.language)) {
			this.messages = new ConfigAccessor(plugin, "language" + File.separator + newLanguage + ".yml");
			this.language = newLanguage;
			plugin.getLogger().info("New language " + this.language + " enabled.");
		}
		
		// reload language file
		messages.reloadConfig();
		
		// reinstall sound file if necessary
		sounds.saveDefaultConfig();
		
		// reload sound file
		sounds.reloadConfig();
	}

	
	/**
	 * Install localization files from <em>language</em> directory in jar 
	 */
	private void installLocalizationFiles() {
	
		List<String> filelist = new ArrayList<String>();
	
		// get the absolute path to this plugin as URL
		URL pluginURL = plugin.getServer().getPluginManager().getPlugin(plugin.getName()).getClass().getProtectionDomain().getCodeSource().getLocation();
	
		// read files contained in jar, adding language/*.yml files to list
		ZipInputStream zip;
		try {
			zip = new ZipInputStream(pluginURL.openStream());
			while (true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null) {
					break;
				}
				String name = e.getName();
				if (name.startsWith("language" + '/') && name.endsWith(".yml")) {
					filelist.add(name);
				}
			}
		} catch (IOException e1) {
			plugin.getLogger().warning("Could not read language files from jar.");
		}
	
		// iterate over list of language files and install from jar if not already present
		for (String filename : filelist) {
			// this check prevents a warning message when files are already installed
			if (new File(plugin.getDataFolder() + File.separator + filename).exists()) {
				continue;
			}
			plugin.saveResource(filename, false);
			plugin.getLogger().info("Installed localization file:  " + filename);
		}
	}


	/**
	 * Determine if a language file exists
	 * @param language
	 * @return
	 */
	private String languageFileExists(final String language) {
		
		// check if localization file for configured language exists, if not then fallback to en-US
		File languageFile = new File(plugin.getDataFolder() 
				+ File.separator + "language" 
				+ File.separator + language + ".yml");
		
		if (languageFile.exists()) {
			return language;
	    }
		plugin.getLogger().info("Language file " + language + ".yml does not exist. Defaulting to en-US.");
		return "en-US";
	}


	/**
	 * Get custom tool name from language file
	 * @return
	 */
	String getToolName() {
		return ChatColor.translateAlternateColorCodes('&',messages.getConfig().getString("TOOL_NAME"));
	}


	/**
	 * Get custom tool lore from language file
	 * @return
	 */
	List<String> getToolLore() {
		List<String> lore = messages.getConfig().getStringList("TOOL_LORE");
		int lineNumber = 0;
		while (lineNumber < lore.size()) {
			lore.set(lineNumber, ChatColor.translateAlternateColorCodes('&',lore.get(lineNumber)));
			lineNumber++;
		}
		return lore;
	}

}
