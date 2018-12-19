/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */

define([
    "jquery",
    "text!controller/template/ESGTemplate.html",
    "common/SeriesChart",
    "common/Ajax",
    "common/SimpleTable",
    "app/FactorSelectionCfgComponent",
    "app/WindowManager"
], function ($, template, seriesChart, ajaxModule, table, selectionConfComponent, windowManager) {
    var controller = {};

    var deleteSeries = function (mfId) {
        if (mfId && mfId !== '') {
            ajaxModule.POST(
                    "deleteSeries",
                    mfId,
                    function (msg) {
                        console.log(msg);
                    },
                    function (error) {
                        console.log("Error with series deletion." + mfId);
                    }
            );
        }
    };
    var colors = ["#FF0000", "#0FF000", "#FF00FF", "#FFFF00", "#DC7633"];

    var createChart = function (chartDom, targetSeries, scalableData, values) {
        var yAxisData = {title: "Serie", data: scalableData};
        seriesChart.createChart(
                chartDom,
                targetSeries, "#value#", "#0000FF", values, yAxisData);
    };

    var createFormulaChart = function (targetSeries, generatedSeries, activeWindow) {
        var chartDom = activeWindow;
        chartDom.detachObject();
        chartDom.expand();
        $.each(targetSeries, function (tidx, titem) {
            $.each(generatedSeries, function (gidx, gitem) {
                if (titem.date === gitem.date)
                    titem.generated = gitem.value;
            });
        });
        var scalableData = [];
        $.each(targetSeries, function (idx, quote) {
            scalableData.push(quote.value);
        });
        var values = [{text: "target", color: "#0000FF"}, {text: "generated", color: "#FF0000"}];
        createChart(chartDom, targetSeries, scalableData, values);
        seriesChart.addSeries([
            {
                value: "#generated#",
                tooltip: "#generated#",
                line: {
                    color: "#FF0000",
                    width: 3
                },
                item: {
                    radius: 0
                }
            }
        ]);
        seriesChart.render();
    };

    var createComparisonChart = function (series, activeWindow, names) {
        var chartDom = activeWindow;
        chartDom.detachObject();
        chartDom.expand();
        var scalableData = [];
        $.each(series, function (idx, quote) {
            if (dhtmlx.Date.str_to_date("%d/%m/%Y")(quote.date).getMonth() % 4 === 0) {
                scalableData.push(quote.value);
            }
        });
        var values = [{text: "target", color: "#0000FF"}];
        $.each(names, function (idx, name) {
            values.push({text: name, color: colors[idx]});
        });
        createChart(chartDom, series, scalableData, values);
        $.each(names, function (idx, key) {
            seriesChart.addSeries([
                {
                    value: "#" + key + "#",
                    tooltip: "#" + key + "#",
                    line: {
                        color: colors[idx],
                        width: 3
                    },
                    item: {
                        radius: 0
                    }
                }
            ]);
        });
        seriesChart.render();
    };

    var calcCoef = function (targetSeries, esgSeries) {
        var targetValues = targetSeries.map(s => s.value);
        var esgValues = esgSeries.map(s => s.value);
        var targetMin = Math.min.apply(null, targetValues);
        var targetMax = Math.max.apply(null, targetValues);
        var targetAvg = (targetMin + targetMax) / 2;
        var esgMin = Math.min.apply(null, esgValues);
        var esgMax = Math.max.apply(null, esgValues);
        var esgAvg = (esgMin + esgMax) / 2;
        return targetAvg / esgAvg;
    };

    var loadSeries = function (mfId, tableContent, activeWindow, startDate, serieName) {
        ajaxModule.GET(
                "loadSeries?mfId=" + encodeURIComponent(mfId) + "&start=" + encodeURIComponent(startDate),
                function (data) {
                    if (data.quotes.length > 0) {

                        if (!tableContent.allSeries) {
                            tableContent.allSeries = data.quotes.slice(0);
                            tableContent.targetSeries = data.quotes.slice(0);
                            tableContent.names = [];
                        } else {
                            tableContent.names.push(serieName);
                            $.each(tableContent.allSeries, function (tidx, titem) {
                                $.each(data.quotes, function (gidx, gitem) {
                                    if (titem.date === gitem.date) {
                                        titem[serieName] = gitem.value * calcCoef(tableContent.targetSeries, data.quotes);
                                    }
                                });
                            });
                        }
                        dhtmlx.message({type: "successMsg", text: " Series loaded successfuly", expire: 5000});
                        createComparisonChart(tableContent.allSeries, activeWindow, tableContent.names);
                    } else {
                        dhtmlx.message({
                            type: "errorMsg",
                            text: "Not found series!",
                            expire: 5000
                        });
                    }
                },
                function (message) {
                    dhtmlx.message({
                        type: "errorMsg",
                        text: message.statusText,
                        expire: 5000
                    });
                });
    };

    var createTable = function (terms, activeWindow, isComparable) {
        var tableData = {rows: []};
        $.each(terms, function (i, item) {
            var data = Object.values(item);
            data[data.length - 3] = data[data.length - 3].toFixed(2) * 100;//Series quality in %
            var campare = false;
            if (isComparable)
                data.unshift(campare);
            data.unshift(i + 1);
            tableData.rows.push({
                id: i,
                data: data
            });
        });

        var header = "selected, coeficient, function, argument, ESG index, description, Start date, End date, Series length, Series quality [%], correlation, distance";
        header = isComparable ? "row number,compare, " + header : "row number," + header;
        var colTypes = "ch,ron,rotxt,ron,rotxt,rotxt,ro,ro,ron,ron,ron,ron";
        colTypes = isComparable ? "ron,ch," + colTypes : "ron," + colTypes;
        var widths = "65,140,75,80,200,390,80,80,100,120,*,*";
        widths = isComparable ? "64,65," + widths : "64," + widths;
        var alignments = "center,center,center,center,left,left,center,center,center,center,center,center";
        alignments = isComparable ? "center,center," + alignments : "center," + alignments;
        var headerAlignments = [
            "text-align:center", "text-align:center", "text-align:center",
            "text-align:center", "text-align:center", "text-align:center",
            "text-align:center", "text-align:center", "text-align:center", "text-align:center",
            "text-align:center", "text-align:center", "text-align:center"];
        if (isComparable)
            headerAlignments.push("text-align:center");
        var quotesTable = table.createTable(
                activeWindow,
                activeWindow,
                header);
        quotesTable.setHeader(header, null, headerAlignments);
        quotesTable.setColTypes(colTypes);
        quotesTable.setColAlign(alignments);
        quotesTable.setInitWidths(widths);
        quotesTable.enableMultiline(true);
        table.init(quotesTable);
        table.addData(quotesTable, tableData);
        return quotesTable;
    };

    var checkCells = function (layout, minCellWidth, cellName) {
        var a = layout.cells("a");
        var b = layout.cells("b");
        var c = layout.cells("c");
        switch (cellName) {
            case "a":
                setWidth(a, 0, layout, [b, c], minCellWidth, 1, 2);
                break;
            case "b":
                setWidth(b, 1, layout, [a, c], minCellWidth, 0, 2);
                break;
            case "c":
                setWidth(c, 2, layout, [a, b], minCellWidth, 0, 1);
                break;
        }
    };

    var setWidth = function (selectedCell, selectedCellIndex, layout, cells, minCellWidth, index1, index2) {
        if (cells[0].isCollapsed() && cells[1].isCollapsed())
            return;
        var lWidth = layout.conf.b_size.w;
        var cellWidth = lWidth / 2;
        var w1 = layout.items[index1].getWidth();
        var w2 = layout.items[index2].getWidth();
        var min_w1 = minCellWidth[index1];
        var min_w2 = minCellWidth[index2];
        if (selectedCell.isCollapsed()) {
            if (w1 > min_w1 && w2 > min_w2 && layout.items[index1].name !== "b")
                return;
            if (min_w1 > min_w2) {
                layout.items[index1].setWidth(min_w1);//it's enought to change one cell's width
                return;
            }
            if (cellWidth > min_w1 && cellWidth > min_w2) {
                layout.items[index1].setWidth(cellWidth);//it's enought to change one cell's width
                return;
            }
            if (cellWidth < min_w1 && cellWidth < min_w2) {
                var i = min_w1 > min_w2 ? index1 : index2;
                layout.items[i].collapse();
            }
            return;
        }
        var w = layout.items[selectedCellIndex].getWidth();
        if (lWidth - w < min_w1) {
            layout.items[index1].collapse();
            return;
        }
        if (w2 < min_w2 && index2 !== 1 && selectedCellIndex !== 1) {
            layout.items[index2].collapse();
            return;
        }
        var min_selected_w = minCellWidth[selectedCellIndex];
        layout.items[index2].setWidth(min_w2);
        layout.items[selectedCellIndex].setWidth(min_selected_w);
        layout.items[index1].setWidth(min_w1);
    };

    var setWidthIfTrue = function (condition, i, myLayout, width) {
        if (condition)
            myLayout.items[i].setWidth(width);
    };

    controller.makeController = function (window, windowManager) {
        var myLayout = window.attachLayout("4U");
        window.setMinDimension(700, 400);
        myLayout.cells("a").setText("Configuration");
        myLayout.cells("b").setText("Series");
        myLayout.cells("c").setText("Compared series");
        myLayout.cells("d").setText("ESG indexes ( formula terms )");
        window.maximize();
        var layoutWidth = myLayout.conf.b_size.w;
        var chartsWidth = (layoutWidth - 700) / 2;
        var minCellWidth = [700];
        minCellWidth.push(chartsWidth);
        minCellWidth.push(chartsWidth);
        myLayout.cells("a").attachHTMLString(template);
        myLayout.cells("a").setWidth(700);
        myLayout.cells("a").setHeight(460);
        myLayout.cells("a").setMinHeight(460);
        // myLayout.cells("a").setMinWidth(700);
        myLayout.cells("b").setWidth(chartsWidth);
        // myLayout.cells("b").setMinWidth(chartsWidth);
        myLayout.cells("c").setWidth(chartsWidth);
        myLayout.attachEvent("onPanelResizeFinish", function (names) {
            $.each(names, function (idx, name) {
                switch (name) {
                    case "a":
                        var condition = myLayout.items[0].getWidth() < minCellWidth[0];
                        setWidthIfTrue(condition, 0, myLayout, minCellWidth[0]);
                        if (idx === 0 && !condition)
                            myLayout.items[2].collapse();
                        break;
                    case "b":
                        var condition = myLayout.items[1].getWidth() < minCellWidth[1];
                        setWidthIfTrue(condition, 1, myLayout, minCellWidth[1]);
                        if (idx === 1 && condition && myLayout.items[2].isCollapsed())
                            myLayout.items[0].setWidth(myLayout.conf.b_size.w - minCellWidth[1]);
                        break;
                    case "c":
                        setWidthIfTrue(myLayout.items[2].getWidth() < minCellWidth[2], 2, myLayout, minCellWidth[2]);
                        break;
                }
            });
        });
        //
        myLayout.attachEvent("onCollapse", function (item) {
            if (item !== "d") {
                console.log(item + " c");
                console.log(myLayout.conf.b_size.w + " " + myLayout._getAvailWidth());
                checkCells(myLayout, minCellWidth, item);
            }
        });
        myLayout.attachEvent("onExpand", function (item) {
            if (item !== "d") {
                console.log(item + " e");
                console.log(myLayout.conf.b_size.w + " " + myLayout._getAvailWidth());
                checkCells(myLayout, minCellWidth, item);
            }
        });
        //
        var activeWindow = $(".dhxwin_active")[0];
        var attachGetFactorsListener = function (buttonId, servletPath, enableId) {
            $(buttonId, activeWindow).click(function () {
                $('#compareSeriesButton', activeWindow).prop('disabled', true);
                $('#calculateButton', activeWindow).prop('disabled', true);
                var data = selectionConfig.getValue();
                data.evaluationId = evaluationId;
                $('.re-loading-spinner-background').show();
                myLayout.cells("b").detachObject();
                myLayout.cells("c").detachObject();

                ajaxModule.POST(
                        servletPath,
                        data,
                        function (result) {
                            dhtmlx.message({type: "successMsg", text: "Factors loaded", expire: 5000});
                            series = result.series;
                            $.each(series, function (i, item) {
                                item.correlation = item.correlation.toFixed(4);
                                item.distance = item.distance.toFixed(4);
                            });
                            createTable(series, myLayout.cells("d"));
                            $(enableId, activeWindow).removeAttr('disabled');
                            $(buttonId, activeWindow).prop('disabled', true);
                            $('.re-loading-spinner-background').hide();
                        },
                        function (error) {
                            $('.re-loading-spinner-background').hide();
                            dhtmlx.message({
                                type: "errorMsg",
                                text: error.statusText,
                                expire: 5000
                            });
                        });
            });
        };
        var evaluationId = {};
        $('.re-loading-spinner-background').show();
        ajaxModule.GET("esg/createEvaluation",
                function (evalId) {
                    evaluationId = evalId.evalId;
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
        var disableButtons = function (btnIds) {
            for (var i = 0; i < btnIds.length; i++) {
                var isExist = $(btnIds[i], activeWindow).attr('disabled');
                if (typeof isExist === typeof undefined || isExist === false)
                    $(btnIds[i], activeWindow).prop('disabled', true);
            }
        };
        var detachObject = function (cellNames) {
            for (var i = 0; i < cellNames.length; i++) {
                myLayout.cells(cellNames[i]).detachObject();
            }
        };
        var onUploadFileListener = function () {
            var isExist = $('#loadFactorsButton', activeWindow).attr('disabled');
            if (typeof isExist !== typeof undefined && isExist !== false)
                $('#loadFactorsButton', activeWindow).removeAttr('disabled');
            disableButtons([
                "#suggestFactorsButton", '#clearUnselectedButton',
                '#findFormulaButton', '#compareSeriesButton', '#calculateButton'
            ]);
            detachObject(['b', 'c', 'd']);
        };
        
        var selectionConfig = selectionConfComponent.makeController($("#factorSelectionCfg", activeWindow)[0], onUploadFileListener);
        var series = [];

        attachGetFactorsListener("#loadFactorsButton", "esg/loadFactors", "#suggestFactorsButton");
        attachGetFactorsListener("#suggestFactorsButton", "esg/suggestFactors", '#clearUnselectedButton');
        $('#clearUnselectedButton', activeWindow).click(function () {
            series = $.grep(series, function (item, idx) {
                return item.selected;
            });
            createTable(series, myLayout.cells("d"));
            $('#findFormulaButton', activeWindow).removeAttr('disabled');
            $('#clearUnselectedButton', activeWindow).prop('disabled', true);
        });
        $('#findFormulaButton', activeWindow).click(function () {
            var request = {
                evalId: evaluationId,
                mfIds: series.map(serie => serie.marketElement)
            };
            $('.re-loading-spinner-background').show();
            ajaxModule.POST(
                    "esg/findFormula",
                    request,
                    function (result) {
                        $.each(result.terms, function (i, item) {
                            var term = series.find(function (element) {
                                return element.marketElement === item.marketElement;
                            });
                            term.termCoeficient = item.termCoeficient.toFixed(4);
                            term.function = "power";
                            term.argument = item.argument;
                        });
                        createTable(series, myLayout.cells("d"));
                        createFormulaChart(result.targetPrices, result.generatedPrices, myLayout.cells("b"));
                        $('#compareSeriesButton', activeWindow).removeAttr('disabled');
                        $('#calculateButton', activeWindow).removeAttr('disabled');
                        $('#findFormulaButton', activeWindow).prop('disabled', true);
                        $('.re-loading-spinner-background').hide();
                    },
                    function (error) {
                        $('.re-loading-spinner-background').hide();
                        dhtmlx.message({
                            type: "errorMsg",
                            text: error.statusText,
                            expire: 5000
                        });
                    });
        });

        $('#compareSeriesButton', activeWindow).click(function () {
            myLayout.cells("d").detachObject();
            var table = createTable(series, myLayout.cells("d"), true);
            var mfId = selectionConfig.getSeriesId();
            var tableContent = {};
            loadSeries(mfId, tableContent, myLayout.cells("c"), series[0].startDate, "value");

            table.attachEvent("onCheck", function (rId, cInd, state) {
                if (cInd === 1 && state) {
                    if (tableContent.names.length > 4) {
                        dhtmlx.message({
                            type: "errorMsg",
                            text: "Max count of compared series is six!",
                            expire: 5000
                        });
                        table.cells(rId, cInd).setValue("false");
                        ;
                        return;
                    }
                    var selectedMfId = series[rId].marketElement;
                    loadSeries(selectedMfId, tableContent, myLayout.cells("c"), series[0].startDate, selectedMfId);
                    return;
                }
                if (cInd === 1 && !state) {
                    selectedMfId = series[rId].marketElement;
                    tableContent.names.splice(tableContent.names.indexOf(selectedMfId), 1);
                    $.each(tableContent.allSeries, function (tidx, titem) {
                        delete titem[selectedMfId];
                    });
                    createComparisonChart(tableContent.allSeries, myLayout.cells("c"), tableContent.names);
                }
            });
            $('#loadFactorsButton', activeWindow).prop('disabled', false);
        });
        $('#calculateButton', activeWindow).click(function () {

            $('.re-loading-spinner-background').show();
            ajaxModule.POST(
                    "esg/calculate",
                    {
                        series: series
                    },
                    function (result) {
                        requirejs(['controller/EsgCalculationController'], function (esgController) {
                            var win = windowManager.createWindow("EsgCalculation");
                            var winId = esgController.makeController(win, result, selectionConfig.getValue().seriesName);
                            windowManager.showWindow(winId);
                            windowManager.addCloseListener(winId, function () {
                                return true;
                            });
                        });
                        $('.re-loading-spinner-background').hide();
                    },
                    function (error) {
                        $('.re-loading-spinner-background').hide();
                        dhtmlx.message({
                            type: "errorMsg",
                            text: error.statusText,
                            expire: 5000
                        });
                    });
        });
        /*
         * Event executed when closing the window. 
         */
        windowManager.addCloseListener(window.getId(), function () {
            $('.re-loading-spinner-background').show();
            ajaxModule.GET(
                    "esg/deleteEval?evalId=" + encodeURIComponent(evaluationId),
                    function (success) {
                        console.log(success);
                        $('.re-loading-spinner-background').hide();
                    },
                    function (error) {
                        console.log(error);
                        $('.re-loading-spinner-background').hide();
                    });

            deleteSeries(selectionConfig.getSeriesId());
        });
        return window.getId();
    };
    return controller;
});