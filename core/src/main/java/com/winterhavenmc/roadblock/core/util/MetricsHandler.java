package com.winterhavenmc.roadblock.core.util;

import com.winterhavenmc.roadblock.core.context.MetricsCtx;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;


public final class MetricsHandler
{
	public MetricsHandler(final MetricsCtx ctx)
	{
		Metrics metrics = new Metrics(ctx.plugin(), 13919);

		// get number of currently deployed chests
		metrics.addCustomChart(new SingleLineChart("protected_blocks", () -> ctx.blocks().getTotalBlocks()));

		// pie chart of configured language
		metrics.addCustomChart(new SimplePie("language", () -> Config.LANGUAGE.getString(ctx.plugin().getConfig())));
	}

}
