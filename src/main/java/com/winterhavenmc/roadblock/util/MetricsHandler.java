package com.winterhavenmc.roadblock.util;

import com.winterhavenmc.roadblock.PluginMain;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;


public class MetricsHandler {

	public MetricsHandler(PluginMain plugin) {

		Metrics metrics = new Metrics(plugin, 13919);

		// get number of currently deployed chests
		metrics.addCustomChart(new SingleLineChart("protected_blocks", () -> plugin.blockManager.getBlockTotal()));

		// pie chart of configured language
		metrics.addCustomChart(new SimplePie("language", () -> Config.LANGUAGE.getString(plugin.getConfig())));
	}

}
