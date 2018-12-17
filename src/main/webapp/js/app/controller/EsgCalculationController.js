/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
define([
    "jquery",
    "text!controller/template/ESGCalculationTemplate.html", "common/SimpleTable"], function ($, template, table) {

    var createTable = function (terms, activeWindow) {
        var tableData = {rows: []};
        $.each(terms, function (i, item) {
            var data = Object.values(item);
            data[2] = data[2].toFixed(4);
            tableData.rows.push({
                id: i,
                data: data
            });
        });

        var header = "ESG index, Description, Weight, Env. Score, Social Score, Gov. Score";
        var colTypes = "rotxt,rotxt,ron,ron,ron,ron";
        var widths = "210,310,90,50,50,50";
        var alignments = "left,left,center,center,center,center";
        var quotesTable = table.createTable(
                activeWindow,
                activeWindow,
                header
                );
        quotesTable.setHeader(header, null,
                ["text-align:center", "text-align:center", "text-align:center",
                    "text-align:center", "text-align:center", "text-align:center"]);
        quotesTable.setColTypes(colTypes);
        quotesTable.setColAlign(alignments);
        quotesTable.setInitWidths(widths);
        quotesTable.enableMultiline(true);
        table.init(quotesTable);
        table.addData(quotesTable, tableData);
        return quotesTable;
    };

    var controller = {};
    controller.makeController = function (ESG_Calculation_Window, result, seriesName) {
        var ESG_Layout = ESG_Calculation_Window.attachLayout("3J");
        ESG_Layout.cells("a").setText("ESG Total");
        ESG_Layout.cells("b").setText("ESG Table");
        ESG_Layout.cells("c").setText("ESG Pie Chart");
        ESG_Layout.cells("a").attachHTMLString(template);
        ESG_Layout.cells("a").setWidth(520);
        ESG_Layout.cells("a").setHeight(200);
        ESG_Layout.cells("a").setMinWidth(510);
        ESG_Layout.cells("a").setMinHeight(200);
        ESG_Layout.cells("b").setHeight(542);
        ESG_Layout.cells("b").setMinWidth(400);
        ESG_Layout.cells("b").setMinHeight(542);
        ESG_Layout.cells("c").setHeight(352);
        ESG_Layout.cells("c").setMinHeight(352);
        var activeWindow = $(".dhxwin_active")[0];
        $("#esgRating", activeWindow).html(result.esgRating.toFixed(2));
        $("#controversy", activeWindow).html(result.controversy.toFixed(2));
        $("#e", activeWindow).html(result.e.toFixed(2));
        $("#s", activeWindow).html(result.s.toFixed(2));
        $("#g", activeWindow).html(result.g.toFixed(2));
        var data = [
            {"result": result.e, "resultName": "Environment", color: "#FF0000"},
            {"result": result.s, "resultName": "Social", color: "#FFFF00"},
            {"result": result.g, "resultName": "Governance", color: "#00FFFF"}
        ];
        var myPieChart;
        var chartbox = $("#chartbox", activeWindow)[0];
        myPieChart = ESG_Layout.cells("c").attachChart({
            view: "pie3D",
            container: chartbox,
            value: "#result#",
            color: "#color#",
            label: "<b>#result#</b>",
            tooltip: "#result#",
            legend: {
                width: 100,
                align: "right",
                valign: "middle",
                template: "#resultName#"
            }
        });
        myPieChart.parse(data, "json");
        createTable(result.factors, ESG_Layout.cells("b"));//
        $('#esgResult', activeWindow).attr("value", JSON.stringify(result));
        $('#mfSeriesName', activeWindow).attr("value", seriesName);
        $("#exportESGBtn", activeWindow).click(function () {
            $('#results', activeWindow).submit();
        });
        return ESG_Calculation_Window.getId();
    };
    return controller;
});
