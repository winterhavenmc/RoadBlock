package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.PluginMain;


enum SubcommandType {

	MATERIALS() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new MaterialsCommand(plugin);
		}
	},

	RELOAD() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ReloadCommand(plugin);
		}
	},

	SHOW() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ShowCommand(plugin);
		}
	},

	STATUS() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new StatusCommand(plugin);
		}
	},

	TOOL() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ToolCommand(plugin);
		}
	};

	abstract Subcommand create(final PluginMain plugin);

}
