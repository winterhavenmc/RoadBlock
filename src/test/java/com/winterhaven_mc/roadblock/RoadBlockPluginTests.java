package com.winterhaven_mc.roadblock;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhaven_mc.roadblock.sounds.SoundId;
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
		void ServerNotNull() {
			Assertions.assertNotNull(server, "server is null.");
		}

		@Test
		@DisplayName("plugin not null.")
		void PluginNotNull() {
			Assertions.assertNotNull(plugin, "plugin is null.");
		}

		@Test
		@DisplayName("plugin enabled.")
		void PluginEnabled() {
			Assertions.assertTrue(plugin.isEnabled(), "plugin not enabled.");
		}

		@Test
		@DisplayName("plugin data folder not null.")
		void PluginDataFolderNotNull() {
			Assertions.assertNotNull(plugin.getDataFolder(),"data folder is null.");
		}
	}

	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginMainObjectTests {

		@Test
		@DisplayName("language handler not null.")
		void messageBuilderNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder, "language handler is null.");
		}

		@Test
		@DisplayName("message builder not null.")
		void MessageBuilderNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder, "message builder is null.");
		}

		@Test
		@DisplayName("world manager not null.")
		void WorldManagerNotNull() {
			Assertions.assertNotNull(plugin.worldManager, "world manager is null.");
		}

		@Test
		@DisplayName("sound config not null.")
		void SoundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig,"sound config is null.");
		}
	}


	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ConfigTest {

		Set<String> enumConfigKeyStrings = new HashSet<>();

		public ConfigTest() {
			for (ConfigSetting configSetting : ConfigSetting.values()) {
				this.enumConfigKeyStrings.add(configSetting.getKey());
			}
		}

		@Test
		@DisplayName("config not null.")
		void ConfigNotNull() {
			Assertions.assertNotNull(plugin.getConfig(),
					"plugin config is null.");
		}

		@Test
		@DisplayName("test configured language.")
		void GetLanguage() {
			Assertions.assertEquals("en-US", plugin.getConfig().getString("language"),
					"language does not equal 'en-US'");
		}

		@SuppressWarnings("unused")
		Set<String> ConfigFileKeys() {
			return plugin.getConfig().getKeys(false);
		}

		@ParameterizedTest
		@DisplayName("file config key is contained in ConfigSetting enum.")
		@MethodSource("ConfigFileKeys")
		void ConfigFileKeyNotNull(String key) {
			Assertions.assertNotNull(key);
			Assertions.assertTrue(enumConfigKeyStrings.contains(key),
					"file config key is not contained in ConfigSetting enum.");
		}

		@ParameterizedTest
		@EnumSource(ConfigSetting.class)
		@DisplayName("ConfigSetting enum matches config file key/value pairs.")
		void ConfigFileKeysContainsEnumKey(ConfigSetting configSetting) {
			Assertions.assertEquals(configSetting.getValue(), plugin.getConfig().getString(configSetting.getKey()),
					"ConfigSetting enum key '" + configSetting.getKey() + "' does not match config file key/value pair.");
		}
	}



	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	@DisplayName("Test Sounds config.")
	class SoundTests {

		// collection of enum sound name strings
		Collection<String> enumSoundNames = new HashSet<>();

		// class constructor
		SoundTests() {
			// add all SoundId enum values to collection
			for (com.winterhaven_mc.roadblock.sounds.SoundId SoundId : SoundId.values()) {
				enumSoundNames.add(SoundId.name());
			}
		}

		@Test
		@DisplayName("Sounds config is not null.")
		void SoundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig);
		}

		@SuppressWarnings("unused")
		Collection<String> GetConfigFileKeys() {
			return plugin.soundConfig.getSoundConfigKeys();
		}

		@ParameterizedTest
		@EnumSource(SoundId.class)
		@DisplayName("enum member soundId is contained in getConfig() keys.")
		void FileKeysContainsEnumValue(SoundId soundId) {
			Assertions.assertTrue(plugin.soundConfig.isValidSoundConfigKey(soundId.name()),
					"Enum value '" + soundId.name() + "' does not have matching key in sounds.yml.");
		}

		@ParameterizedTest
		@MethodSource("GetConfigFileKeys")
		@DisplayName("config file key has matching key in enum sound names")
		void SoundConfigEnumContainsAllFileSounds(String key) {
			Assertions.assertTrue(enumSoundNames.contains(key),
					"File key does not have matching key in enum sound names.");
		}

		@ParameterizedTest
		@MethodSource("GetConfigFileKeys")
		@DisplayName("sound file key has valid bukkit sound name")
		void SoundConfigFileHasValidBukkitSound(String key) {
			String bukkitSoundName = plugin.soundConfig.getBukkitSoundName(key);
			Assertions.assertTrue(plugin.soundConfig.isValidBukkitSoundName(bukkitSoundName),
					"File key '" + key + "' has invalid bukkit sound name: " + bukkitSoundName);
		}
	}

}
