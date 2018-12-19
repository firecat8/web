/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
define([
    "jquery",
    "text!controller/template/SeeSeries.html",
    "common/Ajax",
    "common/SeriesChart",
    "common/SimpleTable",
    "common/FactorsSelector"
],
        function ($, template, ajaxModule, seriesChart, table, factorsSelector) {

            var createChart = function (quotes, activeWindow) {
                var scalableData = [];
                $.each(quotes, function (idx, quote) {
                   // if (dhtmlx.Date.str_to_date("%d/%m/%Y")(quote.date).getMonth() % 4 === 0) {
                        scalableData.push(quote.value);
                    //}
                });

                var values = [{text: "quotes", color: "#0000FF"}];
                var yAxisData = {title: "Serie", data: scalableData};

                seriesChart.createChart(
                       activeWindow,
                        quotes, "#value#", "#0000FF", values, yAxisData);
                seriesChart.render();
            };

            var createTable = function (quotes, activeWindow) {
                var tableData = {rows: []};
                $.each(quotes, function (i, item) {
                    tableData.rows.push({
                        id: i,
                        data: Object.values(item)
                    });
                });
                var quotesTable = table.createTable($('#gridbox', activeWindow)[0], $('#pagingArea', activeWindow)[0], "date, value");
                quotesTable.setColTypes("ro,ron");
                quotesTable.setNumberFormat("0,000.00", 1, ".", ",");
                table.init(quotesTable);
                table.addData(quotesTable, tableData);
            };

            var controller = {};

            controller.makeController = function (window, mfId, windowManager) {
				$('.re-loading-spinner-background').show();
				window.setDimension(650, 400);
                    ajaxModule.GET(
                            "loadQuotes?mfId=" + encodeURIComponent(mfId),
                            function (data) {
                                if (data.quotes.length > 0) {
                                    dhtmlx.message({type: "successMsg", text: " Series loaded successfuly", expire: 5000});
                                    createChart(data.quotes, window);
                                } else {
                                    dhtmlx.message({
                                        type: "errorMsg",
                                        text: "Not found series!",
                                        expire: 5000
                                    });
                                }
								$('.re-loading-spinner-background').hide();
                            },
                            function (message) {
                                dhtmlx.message({
                                    type: "errorMsg",
                                    text: message.statusText,
                                    expire: 5000
                                });
								$('.re-loading-spinner-background').hide();
                            });

                windowManager.addCloseListener(window.getId(), function () {
                    return;
                });
                return window.getId();
            };
            return controller;
        });
