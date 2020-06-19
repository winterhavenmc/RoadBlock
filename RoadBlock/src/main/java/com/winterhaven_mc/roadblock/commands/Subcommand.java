package com.winterhaven_mc.roadblock.commands;

public enum Subcommand {

	RELOAD,
	SHOW,
	STATUS,
	TOOL,
	HELP;

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}

