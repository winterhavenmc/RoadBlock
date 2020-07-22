package com.winterhaven_mc.roadblock.commands;

import com.winterhaven_mc.roadblock.PluginMain;


public enum SubcommandType {

	HELP() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new HelpCommand(plugin, subcommandMap));
		}
	},

	RELOAD() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new ReloadCommand(plugin));
		}
	},

	SHOW() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new ShowCommand(plugin));
		}
	},

	STATUS() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new StatusCommand(plugin));
		}
	},

	TOOL() {
		@Override
		void register(final PluginMain plugin, final SubcommandMap subcommandMap) {
			subcommandMap.register(new ToolCommand(plugin));
		}
	};


	abstract void register(final PluginMain plugin, final SubcommandMap subcommandMap);

}
