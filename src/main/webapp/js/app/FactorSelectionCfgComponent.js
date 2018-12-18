define([
    "jquery",
    "common/Ajax",
    "text!app/templates/FactorsSelectionCfgTemplate.html",
    "common/FactorsSelector",
    "common/FrequencySelector",
    "app/TenorComponent",
    "app/WindowManager",
    "app/MfSelector"],
        function ($, ajaxModule, template, factorsSelector, frequencySelector, tenorComponent, windowManager, mfSelector) {
            var component = {};

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


            //default configuration
            var frequencyMap = {
                "DAILY": {type: "day", number: 1},
                "WEEKLY": {type: "week", number: 1},
                "TWO_WEEKS": {type: "week", number: 2},
                "MONTHLY": {type: "month", number: 1},
                "TWO_MONTHS": {type: "month", number: 2},
                "QUARTERLY": {type: "month", number: 3},
                "FOUR_MONTHS": {type: "month", number: 4},
                "SEMI_ANNUALLY": {type: "month", number: 6},
                "ANNUAL": {type: "year", number: 1},
                "BIANNUAL": {type: "year", number: 2}
            };

            var suggestionMethodData = [
                ["MIN_CORRELATED", "Min correlated"],
                ["MAX_CORRELATED", "Max correlated", null, null, true],
                ["CLUSTERIZATION", "Clusterization"]
            ];

            var titleText = "Drag-n-Drop Excel file here or <br> click to select Excel file for upload";

            var init = function (wrapperDom, factorSelectionCfg, series, uploadListener) {
                // var mfInput = mfSelector.makeController($(".mfSelector", wrapperDom)[0]);
                var frequency = frequencySelector.createCombo($("#frequency", wrapperDom)[0]);
                var historicalInterval = tenorComponent.makeController($("#historicalInterval", wrapperDom)[0]);
                var suggestionMethod = new dhtmlXCombo($("#suggestionMethod", wrapperDom)[0], "suggestionMethod", null, "image");
                suggestionMethod.addOption(suggestionMethodData);

                var fileNameInput = $("#fileNameInput", wrapperDom)[0];
                var chartIcon = $(".chart-icon", wrapperDom)[0];
                $(chartIcon).click(function () {
                    requirejs(['controller/SeeSeriesController'], function (seeSeriesController) {
                        var win = windowManager.createWindow("Series ");
                        var winId = seeSeriesController.makeController(win, factorSelectionCfg.seriesName, windowManager);
                        windowManager.showWindow(winId);
                    });
                });
                var uploadForm = new dhtmlXForm($("#uploadForm", wrapperDom)[0],
                        [{type: "fieldset", label: "Uploader", width: 300, list: [
                                    {
                                        type: "upload",
                                        titleText: titleText,
                                        name: "quotesUpload",
                                        inputWidth: 320,
                                        inputHeight: 50,
                                        url: "quotesUploader",
                                        swfLogs: 'enabled',
                                        autoStart: true,
                                        autoRemove: true
                                    }
                                ]}]);
                uploadForm.attachEvent("onBeforeFileAdd", function (realName) {
                    $('.re-loading-spinner-background').show();
                    return true;
                });
                uploadForm.attachEvent("onUploadFile", function (file, file2, extra) {
                    dhtmlx.message({type: "successMsg", text: "Successfully uploaded serie!", expire: 5000});
                    deleteSeries(factorSelectionCfg.seriesId);
                    console.log("Name:  " + extra.mfName);
                    console.log("ID:  " + extra.mfId);
                    factorSelectionCfg.seriesId = extra.mfId;
                    factorSelectionCfg.seriesName = extra.mfName;
                    $(fileNameInput).val(file);
                    $(chartIcon).removeClass("hidden");
                    if (uploadListener)
                        uploadListener();
                    $('.re-loading-spinner-background').hide();
                });

                uploadForm.attachEvent("onUploadFail", function (file, extra) {
                    $('.re-loading-spinner-background').hide();
                    dhtmlx.message({type: "errorMsg", text: extra, expire: 5000});
                    factorSelectionCfg.seriesId = null;
                });

                historicalInterval.setValue(factorSelectionCfg.historicalInterval);
                $('#maximalNumberInput', wrapperDom).val(factorSelectionCfg.maxSuggestions);
                $('#minimalQualityInput', wrapperDom).val(factorSelectionCfg.minimalQuality);
                frequency.selectOption(frequencySelector.getFrequencyId(factorSelectionCfg.frequency));
                /*
                 mfInput.addOnChangeListener(function (id, seriesName) {
                 factorSelectionCfg.seriesName = seriesName;
                 series.id = id;
                 });
                 */

                frequency.attachEvent("onChange", function (value, text) {
                    if (value === "" || value === null || value === undefined)
                        value = "TWO_MONTHS";
                    factorSelectionCfg.historicalPriceFrequency = value;
                });


                suggestionMethod.attachEvent("onChange", function (value, text) {
                    if (value === null || value === undefined)
                        value = "";
                    factorSelectionCfg.suggestionMethod = value;
                });

                $('#maximalNumberInput', wrapperDom).on('change', function () {
                    if (this.value === "" || this.value === null || this.value === undefined)
                        this.value = "0";
                    factorSelectionCfg.maxSuggestions = this.value;
                });

                $('#minimalQualityInput', wrapperDom).on('change', function () {
                    if (this.value === "" || this.value === null || this.value === undefined)
                        this.value = "0";
                    factorSelectionCfg.minimalQuality = this.value;
                });

                historicalInterval.onChange(function (tenor) {
                    factorSelectionCfg.historicalInterval = tenor;
                });
            };

            component.getFrequencyUnit = function () {
                return frequencyMap[factorSelectionCfg.frequency];
            };


            component.makeController = function (parentDom, uploadListener) {
                $(parentDom).append(template);
                var controller = {};
                var series = {};

                var factorSelectionCfg = {
                    "seriesName": "",
                    "historicalInterval": {"years": 2, "months": 0, "days": 0},
                    "frequency": "TWO_MONTHS",
                    "suggestionMethod": "MAX_CORRELATED",
                    "maxSuggestions": 10,
                    "minimalQuality": 0.5
                };

                init($("#factorSelectionCfg", parentDom)[0], factorSelectionCfg, series, uploadListener);

                controller.getValue = function () {
                    return factorSelectionCfg;
                };

                controller.getSeriesId = function () {
                    return factorSelectionCfg.seriesId;
                };

                return controller;
            };
            return component;
        });

