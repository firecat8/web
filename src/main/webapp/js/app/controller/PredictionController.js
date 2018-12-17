define([
    "jquery",
    "text!controller/template/PredictionTemplate.html",
    "common/SeriesChart",
    "common/Ajax",
    "app/PredictionConfigComponent",
    "app/IntervalCalendarComponent"], function ($, template, seriesChart, ajaxModule, predictConfComponent, intervalCalendarComp) {
    var controller = {};

    var prevEvalId = {};

    var resultsHolder = {};

    var checkDate = function (res, idx, date, dateIndex, calendar) {
        if (res[idx].date === date) {
            return idx;
        }
        if (idx > 0) {
            var currentDate_ms = new Date(calendar.getISODate(res[idx].date)).getTime();
            var date_ms = new Date(calendar.getISODate(date)).getTime();
            if (date_ms < currentDate_ms) {
                var prevDate_ms = new Date(calendar.getISODate(res[idx - 1].date)).getTime();
                if (date_ms - prevDate_ms < currentDate_ms - date_ms)
                    return idx - 1;
                return idx;
            }
        }
        return dateIndex;
    };

    var getRangedResults = function (startDate, endDate, intervalCalendar) {
        var startDateIndex = -1;
        var endDateIndex = -1;
        var res = resultsHolder.results;
        $.each(res, function (idx, result) {
            if (startDateIndex === -1) {
                startDateIndex = checkDate(res, idx, startDate, startDateIndex, intervalCalendar);
            }
            if (endDateIndex === -1) {
                endDateIndex = checkDate(res, idx, endDate, endDateIndex, intervalCalendar);
            }
            if (startDateIndex !== -1 && endDateIndex !== -1)
                return;
        });
        if (startDateIndex === endDateIndex)
            startDateIndex--;
        if (endDateIndex === res.length - 1)
            endDateIndex++;
        return res.slice(startDateIndex, endDateIndex);
    };

    var setNewInterval = function (intervalCalendar, results) {
        intervalCalendar.setInterval(results[0].date, results[results.length - 1].date);
    };

    var makeIntervalCalendarConfig = function () {
        return {
            legend: "Chart Interval",
            format: "%j/%n/%Y",
            btnValue: "Redraw chart"
        };
    };

    var deletePreviousSeries = function () {
        if (prevEvalId && prevEvalId !== '') {
            ajaxModule.POST(
                    "deletePredictionResults",
                    prevEvalId,
                    function () {
                        console.log("Deleted previous prediction results.");
                    },
                    function () {
                        console.log("Error with results deletion.");
                    }
            );
        }
    };
    var deleteSeries = function (mfId) {
        if (mfId && mfId !== '')
            ajaxModule.POST(
                    "deleteSeries",
                    mfId,
                    function (msg) {
                        console.log(msg);
                    },
                    function (error) {
                        console.log("Error with series deletion.");
                    }
            );
    };

    var createSerieChart = function (chartDom, valueName, valueColor, result, values, yAxis) {

        seriesChart.createChart(
                chartDom,
                result.results,
                valueName,
                valueColor,
                values,
                yAxis);

    };
    var createSeriesChart = function (result, chartDom) {
        var values = [{text: "historicalPrices", color: "#0000FF"},
            {text: "predictions", color: "#FFD700"},
            {text: "volaMinus", color: "#c9180c"},
            {text: "volaPlus", color: "#ADFF2F"}];
        var scalableData = [];
        $.each(result.results, function (idx, results) {
            if (results.historicalPrices) {
                scalableData.push(results.historicalPrices);
            }
            if (results.predictions) {
                scalableData.push(results.predictions);
            }
            if (results.volaMinus) {
                scalableData.push(results.volaMinus);
            }
            if (results.volaPlus) {
                scalableData.push(results.volaPlus);
            }
        });
        var yAxis = {title: " ", data: scalableData};
        createSerieChart(chartDom, "#historicalPrices#", "#0000FF", result, values, yAxis);
        seriesChart.addSeries([
            {
                value: "#predictions#",
                tooltip: "#predictions#",
                line: {
                    color: "#FFD700",
                    width: 3
                },
                item: {
                    radius: 0
                }
            },
            {
                value: "#volaMinus#",
                tooltip: "#volaMinus#",
                line: {
                    color: "#c9180c",
                    width: 3
                },
                item: {
                    radius: 0
                }
            },
            {
                value: "#volaPlus#",
                tooltip: "#volaPlus#",
                line: {
                    color: "#ADFF2F",
                    width: 3
                },
                item: {
                    radius: 0
                }
            }
        ]);
        seriesChart.render();

    };
    controller.makeController = function (predictWindow, windowManager) {
        var myLayout = predictWindow.attachLayout("2U");
        myLayout.cells("a").setText("Prediction configuration");
        myLayout.cells("b").setText("Prediction result");
        myLayout.cells("a").attachHTMLString(template);
        myLayout.cells("a").setWidth(475);
        myLayout.cells("a").setMinHeight(300);
        myLayout.cells("a").setMinWidth(475);
        myLayout.cells("b").setMinHeight(400);
        myLayout.cells("b").setMinWidth(585);
        predictWindow.maximize();
        var activeWindow = $(".dhxwin_active")[0];
        prevEvalId = $('#evalID', activeWindow).val();
        $("#exportButton", activeWindow).hide();
        var chartDom = $('#seriesChart', activeWindow)[0];
        var predictConfigController = predictConfComponent.makeController($("#predictConfigDiv", activeWindow)[0]);
        var datesInterval = $('#datesInterval', activeWindow)[0];
        $(datesInterval).hide();
        var config = makeIntervalCalendarConfig();
        var intervalCalendar = intervalCalendarComp.makeController(datesInterval, config);
        intervalCalendar.onButtonClick(
                function (startDate, endDate) {
                    var rangedResults = {};
                    rangedResults.results = getRangedResults(startDate, endDate, intervalCalendar);
                    createSeriesChart(rangedResults, myLayout.cells("b"));
                    dhtmlx.message({type: "successMsg", text: "The chart is redrawn", expire: 3000});
                }
        );
        predictConfigController.addOnUploadListener(
                function () {
                    $(chartDom).empty();
                    $(datesInterval).hide();
                    $("#exportButton", activeWindow).hide();
                }
        );
        $("#predictButton", activeWindow).click(function () {
            var data = predictConfigController.getPredictConfigInJsonFormat();
            $('.re-loading-spinner-background').show();
            ajaxModule.POST(
                    "predict",
                    data,
                    function (result) {
                        if (result.results.length === 0) {
                            dhtmlx.message({
                                type: "errorMsg",
                                text: "Not found results!",
                                expire: 5000
                            });
                            $(chartDom).empty();
                            $("#exportButton", activeWindow).hide();
                            $(datesInterval).hide();
                        } else {
                            prevEvalId = $('#evalID', activeWindow).val();
                            deletePreviousSeries();
                            $('#evalID', activeWindow).attr("value", result.evalID);
                            $('#seriesID', activeWindow).attr("value", result.seriesID);
                            resultsHolder = result;
                            dhtmlx.message({type: "successMsg", text: " Prediction is over", expire: 3000});
                            createSeriesChart(result, myLayout.cells("b"));
                            $("#exportButton", activeWindow).show();
                            setNewInterval(intervalCalendar, result.results);
                            $(datesInterval).show();
                        }
                        $('.re-loading-spinner-background').hide();
                    },
                    function (error) {
                        $('.re-loading-spinner-background').hide();
                        dhtmlx.message({
                            type: "errorMsg",
                            text: error.statusText,
                            expire: 5000
                        });
                        $("#exportButton", activeWindow).hide();
                        $(datesInterval).hide();
                    });
        });
        $("#exportButton", activeWindow).click(function () {
            $('#results', activeWindow).submit();
        });
        windowManager.addCloseListener(predictWindow.getId(), function () {
            deletePreviousSeries();
            deleteSeries(predictConfigController.getPredictConfigInJsonFormat().mfId);
        });
        return predictWindow.getId();
    };
    return controller;
});