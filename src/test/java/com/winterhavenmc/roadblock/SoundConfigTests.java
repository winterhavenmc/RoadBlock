package com.winterhavenmc.roadblock;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.winterhavenmc.roadblock.sounds.SoundId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SoundConfigTests {

	private PluginMain plugin;

	@BeforeEach
	public void setUp() {

		// Start the mock server
		MockBukkit.mock();

		// start the mock plugin
		plugin = MockBukkit.load(PluginMain.class);

	}

	@AfterEach
	public void tearDown() {
		// Stop the mock server
		MockBukkit.unmock();
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
			for (com.winterhavenmc.roadblock.sounds.SoundId SoundId : SoundId.values()) {
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
