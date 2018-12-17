define(["common/AxisDateSifter", "common/LineChart"], function (sifter, lineChart) {

	var scaleYAxis = function (data) {
		var scale = {};
		var max = Math.max.apply(null, data);
		var min = Math.min.apply(null, data);
		var end = (parseInt(parseInt(max, 10) / 10) + 1) * 10;
		scale.start = (parseInt(parseInt(min, 10) / 10) - 1) * 10;
		scale.step = parseInt((end - scale.start) / 100 * 10);
		scale.end = (parseInt((end - scale.start) / scale.step + 1)) * scale.step + scale.start;
		return scale;
	};

	var chart = {};
	chart.createChart = function (container, result, name, color, values, yAxisData) {
		sifter.setRange(
				dhtmlx.Date.str_to_date("%d/%m/%Y")(result[0].date),
				dhtmlx.Date.str_to_date("%d/%m/%Y")(result[result.length - 1].date)
				);

		var scale = scaleYAxis(yAxisData.data);

		var seriesChart = lineChart.createLineChart(
				container,
				name,
				color,
				function (unit) {
					return sifter.siftDates(unit);
				},
				function (obj) {
					return dhtmlx.Date.str_to_date("%d/%m/%Y")(obj.date)
				},
				{
					start: dhtmlx.Date.str_to_date("%d/%m/%Y")(result[0].date),
					end: dhtmlx.Date.str_to_date("%d/%m/%Y")(result[result.length - 1].date),
					next: function (d) {
						return dhtmlx.Date.add(d, 1, "day");
					}
				},
				{
					values: values,
					valign: "bottom",
					align: "center",
					width: 100,
					layout: "x",
					toggle: false},
				{
					title: yAxisData.title,
					lines: false,
					start: scale.start,
					step: scale.step,
					end: scale.end
				}
		);

		chart.addSeries = function (series) {
			lineChart.addSeries(seriesChart, series);
		};

		chart.render = function () {
			lineChart.render(seriesChart, result);
		};

	};

	return chart;
});