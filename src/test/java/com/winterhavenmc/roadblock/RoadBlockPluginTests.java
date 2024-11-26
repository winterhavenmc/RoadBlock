package com.winterhavenmc.roadblock;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

import org.junit.jupiter.api.*;


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

}
