package com.winterhaven_mc.roadblock;

import com.winterhaven_mc.util.LanguageHandler;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.WorldManager;
import com.winterhaven_mc.util.YamlSoundConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;


@SuppressWarnings("unused")
public final class PluginMain extends JavaPlugin {

    public LanguageHandler languageHandler;
    public WorldManager worldManager;
    public SoundConfiguration soundConfig;
//    protected BlockManager blockManager;
//    protected HighlightManager highlightManager;

    public PluginMain() {
        super();
    }


    protected PluginMain(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }


    @Override
    public void onEnable() {

        // install default config.yml if not present
        saveDefaultConfig();

        // instantiate language manager
        languageHandler = new LanguageHandler(this);

        // instantiate world manager
        worldManager = new WorldManager(this);

        // instantiate sound configuration
        soundConfig = new YamlSoundConfiguration(this);

        // instantiate block manager
//        blockManager = new BlockManager(this);

        // instantiate highlight manager
//        highlightManager = new HighlightManager(this);

        // instantiate command manager
//        new CommandManager(this);

        // instantiate event listener
//        new EventListener(this);

    }

}
