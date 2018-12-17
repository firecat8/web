define(["jquery"], function ($) {
	var chart = {};
	chart.createLineChart = function (container, valueName,color, templateFunction, valueFinction, units, legend, yAxis) {

		var LineChart = container.attachChart({
			view: "line",
			container: container,
			value: valueName,
			tooltip: valueName,
			line: {
				color: color,
				width: 3
			},
			item: {
                radius: 0
			},
			xAxis: {
				lineColor: "#737373",
				lines: false,
				template: function (obj) {
					return templateFunction(obj.$unit);
				},
				value: function (obj) {
					return valueFinction(obj);
				},
				tooltip:function (obj) {
					return obj;
				},
				units: units
			},
			yAxis: yAxis,
			legend: legend
		});
		return LineChart;
	};

	chart.addSeries = function (chart, series) {
		$.each(series, function (index, item) {
			chart.addSeries(item);
		});
	};

	chart.render = function (chart, result) {
		chart.parse(JSON.stringify(result), "json");
		//chart.render();
	};

	return chart;
});