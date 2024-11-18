package com.winterhavenmc.roadblock;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhavenmc.roadblock.sounds.SoundId;
import com.winterhavenmc.roadblock.util.Config;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoadBlockPluginTests {

	private ServerMock server;
	private PluginMain plugin;

	@BeforeAll
	public void setUp() {
		// Start the mock server
		server = MockBukkit.mock();

		// start the mock plugin
		plugin = MockBukkit.load(PluginMain.class);

	}

	@AfterAll
	public void tearDown() {
		// Stop the mock server
		MockBukkit.unmock();
	}

	@Nested
	@DisplayName("Test mock objects.")
	class MockingTests {

		@Test
		@DisplayName("server is not null.")
		void serverNotNull() {
			Assertions.assertNotNull(server, "server is null.");
		}

		@Test
		@DisplayName("plugin not null.")
		void pluginNotNull() {
			Assertions.assertNotNull(plugin, "plugin is null.");
		}

		@Test
		@DisplayName("plugin enabled.")
		void pluginEnabled() {
			Assertions.assertTrue(plugin.isEnabled(), "plugin not enabled.");
		}

		@Test
		@DisplayName("plugin data folder not null.")
		void pluginDataFolderNotNull() {
			Assertions.assertNotNull(plugin.getDataFolder(),"data folder is null.");
		}
	}

	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginMainObjectTests {

		@Test
		@DisplayName("message builder not null.")
		void messageBuilderNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder, "message builder is null.");
		}

		@Test
		@DisplayName("world manager not null.")
		void worldManagerNotNull() {
			Assertions.assertNotNull(plugin.worldManager, "world manager is null.");
		}

		@Test
		@DisplayName("sound config not null.")
		void soundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig,"sound config is null.");
		}
	}


	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ConfigTest {

		final Set<String> enumConfigKeyStrings = new HashSet<>();

		public ConfigTest() {
			for (Config configSetting : Config.values()) {
				this.enumConfigKeyStrings.add(configSetting.getKey());
			}
		}

		@Test
		@DisplayName("config not null.")
		void configNotNull() {
			Assertions.assertNotNull(plugin.getConfig(),
					"plugin config is null.");
		}

		@Test
		@DisplayName("test configured language.")
		void getLanguage() {
			Assertions.assertEquals("en-US", plugin.getConfig().getString("language"),
					"language does not equal 'en-US'");
		}

		@SuppressWarnings("unused")
		Set<String> configFileKeys() {
			return plugin.getConfig().getKeys(false);
		}

		@ParameterizedTest
		@DisplayName("file config key is contained in ConfigSetting enum.")
		@MethodSource("configFileKeys")
		void configFileKeyNotNull(String key) {
			Assertions.assertNotNull(key);
			Assertions.assertTrue(enumConfigKeyStrings.contains(key),
					"file config key is not contained in ConfigSetting enum.");
		}

		@ParameterizedTest
		@EnumSource(value = Config.class, mode = EnumSource.Mode.EXCLUDE, names = {"DEBUG", "PROFILE"} )
		@DisplayName("ConfigSetting enum matches config file key/value pairs.")
		void configFileKeysContainsEnumKey(Config configSetting) {
			Assertions.assertEquals(configSetting.getDefaultValue(), plugin.getConfig().getString(configSetting.getKey()),
					"ConfigSetting enum key '" + configSetting.getKey() + "' does not match config file key/value pair.");
		}
	}



	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("Test Sounds config.")
	class SoundTests {

		// collection of enum sound name strings
		final Collection<String> enumSoundNames = new HashSet<>();

		// class constructor
		SoundTests() {
			// add all SoundId enum values to collection
			for (SoundId SoundId : SoundId.values()) {
				enumSoundNames.add(SoundId.name());
			}
		}

		@Test
		@DisplayName("Sounds config is not null.")
		void soundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig);
		}

		@SuppressWarnings("unused")
		Collection<String> getConfigFileKeys() {
			return plugin.soundConfig.getSoundConfigKeys();
		}

		@ParameterizedTest
		@EnumSource(SoundId.class)
		@DisplayName("enum member soundId is contained in getConfig() keys.")
		void fileKeysContainsEnumValue(SoundId soundId) {
			Assertions.assertTrue(plugin.soundConfig.isValidSoundConfigKey(soundId.name()),
					"Enum value '" + soundId.name() + "' does not have matching key in sounds.yml.");
		}

		@ParameterizedTest
		@MethodSource("getConfigFileKeys")
		@DisplayName("config file key has matching key in enum sound names")
		void soundConfigEnumContainsAllFileSounds(String key) {
			Assertions.assertTrue(enumSoundNames.contains(key),
					"File key does not have matching key in enum sound names.");
		}

		@ParameterizedTest
		@MethodSource("getConfigFileKeys")
		@DisplayName("sound file key has valid bukkit sound name")
		void soundConfigFileHasValidBukkitSound(String key) {
			String bukkitSoundName = plugin.soundConfig.getBukkitSoundName(key);
			Assertions.assertTrue(plugin.soundConfig.isValidBukkitSoundName(bukkitSoundName),
					"File key '" + key + "' has invalid bukkit sound name: " + bukkitSoundName);
		}
	}

}
