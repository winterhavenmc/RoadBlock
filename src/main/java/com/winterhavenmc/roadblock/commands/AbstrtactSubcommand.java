/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.roadblock.commands;

import com.winterhavenmc.roadblock.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;


abstract class AbstrtactSubcommand implements Subcommand
{
	protected String name;
	protected Collection<String> aliases = new LinkedHashSet<>();
	protected String usageString;
	protected MessageId description;
	protected String permissionNode;
	protected int minArgs;
	protected int maxArgs;


	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Collection<String> getAliases()
	{
		return aliases;
	}

	@Override
	public String getUsage()
	{
		return usageString;
	}

	@Override
	public void displayUsage(final CommandSender sender)
	{
		sender.sendMessage(usageString);
	}

	@Override
	public MessageId getDescription()
	{
		return description;
	}

	@Override
	public String getPermissionNode()
	{
		return this.permissionNode;
	}

	@Override
	public int getMinArgs()
	{
		return minArgs;
	}

	@Override
	public int getMaxArgs()
	{
		return maxArgs;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args)
	{
		return Collections.emptyList();
	}

}
