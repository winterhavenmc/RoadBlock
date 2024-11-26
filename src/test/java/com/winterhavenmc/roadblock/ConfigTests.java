package com.winterhavenmc.roadblock;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.winterhavenmc.roadblock.util.Config;
import org.bukkit.Material;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigTests {

	private PluginMain plugin;

	@BeforeEach
	public void setUp() {

		// Start the mock server
		@SuppressWarnings("unused")
		ServerMock server = MockBukkit.mock();

		// start the mock plugin
		plugin = MockBukkit.load(PluginMain.class);

	}

	@AfterEach
	public void tearDown() {
		// Stop the mock server
		MockBukkit.unmock();
	}

	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ConfigTest {

		final Set<String> enumConfigKeyStrings = new HashSet<>();

		public ConfigTest() {
			for (Config config : Config.values()) {
				this.enumConfigKeyStrings.add(config.asFileKey());
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
		@DisplayName("check file config key conforms to yaml naming convention (lower kebab case).")
		@MethodSource("configFileKeys")
		void configFileKeyMatchesCase(String key) {
			Assertions.assertNotNull(key,
					"string key passed is null.");
			Assertions.assertTrue(enumConfigKeyStrings.contains(key),
					"file config key is not contained in Config enum.");
			Assertions.assertEquals(key, Config.Case.LOWER_KEBAB.convert(key),
					"File config key does not conform to yaml naming convention (lower kebab case).");
		}

		@ParameterizedTest
		@EnumSource(Config.class)
		@DisplayName("Checking Enum members conform to Java constant naming convention (upper snake case).")
		void configEnumStringNamingConvention(Config config) {
			Assertions.assertEquals(config.name(), config.toUpperSnakeCase(),
					"Enum member name does not conform to Java constant naming convention (upper snake case).");
		}

		/**
		 * Test that Config enum members exist as keys in default config.yml.
		 * Note: Add any config settings that do not exist in default config.yml file to EXCLUDED names
		 *
		 * @param configSetting Config enum member
		 */
		@ParameterizedTest
		@EnumSource(value = Config.class, mode = EnumSource.Mode.EXCLUDE, names = {"DEBUG", "PROFILE", "STORAGE_TYPE"})
		@DisplayName("Config enum matches default config.yml file key/value pairs.")
		void configFileKeysContainsEnumKey(Config configSetting) {
			Assertions.assertEquals(configSetting.getDefaultString(), plugin.getConfig().getString(configSetting.asFileKey()),
					"ConfigSetting enum key '" + configSetting.asFileKey() + "' does not match config file key/value pair.");
		}
	}

	@Test
	@DisplayName("Test getDefaultString method")
	void configGetDefaultStringTest() {
		Assertions.assertEquals("GOLDEN_PICKAXE", Config.TOOL_MATERIAL.getDefaultString(),
				"The default tool material string is not correct.");
	}

	@Test
	@DisplayName("Test getDefaultObject method")
	void configGetDefaultObjectTest() {
		Assertions.assertInstanceOf(Material.class, Config.TOOL_MATERIAL.getDefaultObject(),
				"The default tool material object is not of the correct type.");
	}

	@Test
	@DisplayName("Test upper snake case methods")
	void configUpperSnakeCaseTest() {
		Assertions.assertEquals("TEST_STRING", Config.Case.UPPER_SNAKE.convert("test-string"),
				"The test string was not properly converted to upper snake case.");
		Assertions.assertEquals("TOOL_MATERIAL", Config.TOOL_MATERIAL.toUpperSnakeCase());
	}

	@Test
	@DisplayName("Test lower kebab case methods")
	void configLowerKebabCaseTest() {
		Assertions.assertEquals("test-string", Config.Case.LOWER_KEBAB.convert("TEST_STRING"),
				"The test string was not properly converted to upper snake case.");
		Assertions.assertEquals("tool-material", Config.TOOL_MATERIAL.toLowerKebabCase());
	}

	@Test
	@DisplayName("Test getBoolean method")
	void configGetBooleanTest() {
		Assertions.assertTrue(Config.SOUND_EFFECTS.getBoolean(plugin.getConfig()));
		Assertions.assertFalse(Config.DEBUG.getBoolean(plugin.getConfig()));
	}

	@Test
	@DisplayName("Test getInt method")
	void configGetIntTest() {
		Assertions.assertEquals(100, Config.SPREAD_DISTANCE.getInt(plugin.getConfig()));
	}

	@Test
	@DisplayName("Test getString method")
	void configGetStringTest() {
		Assertions.assertEquals("GOLDEN_PICKAXE", Config.TOOL_MATERIAL.getString(plugin.getConfig()));
	}

	@Test
	@DisplayName("Test getStringList method")
	void configGetStringListTest() {
		Assertions.assertEquals(Config.MATERIALS.getDefaultObject().toString(),
				Config.MATERIALS.getStringList(plugin.getConfig()).toString());
	}

	@Test
	@DisplayName("Test get Method")
	void configGetTest() {
		Assertions.assertInstanceOf(String.class, Config.LANGUAGE.get(plugin.getConfig()));
		Assertions.assertInstanceOf(Boolean.class, Config.DISPLAY_TOTAL.get(plugin.getConfig()));
		Assertions.assertInstanceOf(Integer.class, Config.SHOW_DISTANCE.get(plugin.getConfig()));
	}

	@Test
	@DisplayName("Test material set cache gets purged during reload")
	void configReloadTest() {
		Assertions.assertFalse(Config.materialCacheNull(),
				"material set cache is null at startup");
		Config.reload(plugin);
		Assertions.assertTrue(Config.materialCacheNull(),
				"material set cache is not null after reload");
		Set<Material> ignored = Config.MATERIALS.getMaterialSet(plugin.getConfig());
		Assertions.assertFalse(Config.materialCacheNull(),
				"material set cache is null after dereferencing config value");
	}

}
