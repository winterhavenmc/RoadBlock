package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.PluginMain;


public enum SubcommandType {

	HELP() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new HelpCommand(plugin, subcommandRegistry));
		}
	},

	MATERIALS() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new MaterialsCommand(plugin));
		}
	},

	RELOAD() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new ReloadCommand(plugin));
		}
	},

	SHOW() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new ShowCommand(plugin));
		}
	},

	STATUS() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new StatusCommand(plugin));
		}
	},

	TOOL() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new ToolCommand(plugin));
		}
	};


	abstract void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry);

}
