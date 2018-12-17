define(
        [
            "jquery",
            "common/SeriesChart"
        ],
        function ($, seriesChart) {

            var controller = {};
            controller.makeController = function (window, windowManager) {
                var myLayout = window.attachLayout("2E");
                myLayout.cells("a").setText("Upload file");
                myLayout.cells("b").setText("Series chart");
                myLayout.cells("a").setHeight(150);
                myLayout.cells("b").collapse();
              
                var myForm = myLayout.cells("a").attachForm([{type: "fieldset", label: "Uploader", list: [
                            {type: "upload", name: "myFiles", inputWidth: 330, url: "uploader", swfLogs: 'enabled',
                                autoStart: true, autoRemove: true}
                        ]}]);
                myForm.attachEvent("onUploadFile", function (file, file2, extra) {
                    dhtmlx.message({type: "successMsg", text: "Successfully saved serie in database!", expire: 5000});
                    createChart(extra.quotes, myLayout.cells("b"), file.split('.').slice(0, -1).join('.'));
                    myLayout.cells("b").expand();
                });

                myForm.attachEvent("onUploadFail", function (file, extra) {
                    dhtmlx.message({type: "errorMsg", text: extra, expire: 5000});
                    myLayout.cells("b").collapse();
                });

                var createChart = function (quotes, activeWindow, serieName) {
                    var scalableData = [];
                    $.each(quotes, function (idx, quote) {
                        if (dhtmlx.Date.str_to_date("%d/%m/%Y")(quote.date).getMonth() % 4 === 0) {
                            scalableData.push(quote.value);
                        }
                    });
                    var values = [{text: serieName, color: "#0000FF"}];
                    var yAxisData = {title: serieName, data: scalableData};
                    seriesChart.createChart(
                            activeWindow,
                            quotes, "#value#", "#0000FF", values, yAxisData);
                    seriesChart.render();
                };

                windowManager.addCloseListener(window.getId(), function () {
                    return;
                });
                return window.getId();
            };

            return controller;
        });
