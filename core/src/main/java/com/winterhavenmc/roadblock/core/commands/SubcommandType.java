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

package com.winterhavenmc.roadblock.core.commands;


import com.winterhavenmc.roadblock.core.context.CommandCtx;

enum SubcommandType
{
	MATERIALS()
			{
				@Override
				Subcommand create(final CommandCtx ctx)
				{
					return new MaterialsSubcommand(ctx);
				}
			},

	RELOAD()
			{
				@Override
				Subcommand create(final CommandCtx ctx)
				{
					return new ReloadSubcommand(ctx);
				}
			},

	SHOW()
			{
				@Override
				Subcommand create(final CommandCtx ctx)
				{
					return new ShowSubcommand(ctx);
				}
			},

	STATUS()
			{
				@Override
				Subcommand create(final CommandCtx ctx)
				{
					return new StatusSubcommand(ctx);
				}
			},

	TOOL()
			{
				@Override
				Subcommand create(final CommandCtx ctx)
				{
					return new ToolSubcommand(ctx);
				}
			};


	/**
	 * Create an instance of the subcommand
	 *
	 * @return an instance of the subcommand
	 */
	abstract Subcommand create(final CommandCtx ctx);
}
