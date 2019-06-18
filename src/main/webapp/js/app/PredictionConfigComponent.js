define([
    "jquery",
    "common/Ajax",
    "text!app/templates/PredictionConfigTemplate.html",
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
            var defaultConfig = {
                "mfId": "",
                "config": {
                    "historicalInterval": {"years": 2, "months": 0, "days": 0},
                    "backwardAnalyticalPeriod": {"years": 0, "months": 0, "days": 0},
                    "historicalPriceFrequency": "TWO_WEEKS",
                    "predictionHorizon": {"years": 2, "months": 0, "days": 0},
                    "historicalPredictionStep": 2}};
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

            var titleText = "Drag-n-Drop Excel file here or <br> click to select Excel file for upload";

            var copyDefaultConfig = function (defaultConfig) {
                var predictConfig = {};
                predictConfig.mfId = defaultConfig.mfId;
                predictConfig.config = {};
                predictConfig.config.historicalPriceFrequency = defaultConfig.config.historicalPriceFrequency;
                predictConfig.config.historicalInterval = defaultConfig.config.historicalInterval;
                predictConfig.config.predictionHorizon = defaultConfig.config.predictionHorizon;
                predictConfig.config.backwardAnalyticalPeriod = defaultConfig.config.backwardAnalyticalPeriod;
                predictConfig.config.historicalPredictionStep = defaultConfig.config.historicalPredictionStep;
                return predictConfig;
            };
            var PredictionConfigController = function () {

                var predictConfig = copyDefaultConfig(defaultConfig);
                var uploadForm;
                var frequency;
                var historicalInterval;
                var predictionHorizon;
                var backwardAnalyticalPeriod;

                var fileNameInput;
                var chartIcon;
                var fileName;
                var uploadListener;

                this.init = function (predictTree) {

                    frequency = frequencySelector.createCombo($("#historicalPriceFrequency", predictTree)[0]);
                    historicalInterval = tenorComponent.makeController($("#historicalInterval", predictTree)[0]);
                    predictionHorizon = tenorComponent.makeController($("#predictionHorizon", predictTree)[0]);
                    backwardAnalyticalPeriod = tenorComponent.makeController($("#backwardAnalyticalPeriod", predictTree)[0]);
                    fileNameInput = $("#fileNameInput", predictTree)[0];
                    chartIcon = $(".chart-icon", predictTree)[0];
                    $(chartIcon).click(function () {
                        requirejs(['controller/SeeSeriesController'], function (seeSeriesController) {
                            var win = windowManager.createWindow("Series " + fileName);
                            var winId = seeSeriesController.makeController(win, predictConfig.mfId, windowManager);
                            windowManager.showWindow(winId);
                        });
                    });
                    uploadForm = new dhtmlXForm($("#uploadForm", predictTree)[0],
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
                        deleteSeries(predictConfig.mfId);
                        $('.re-loading-spinner-background').hide();
                        console.log("Name:  " + extra.mfName);
                        console.log("ID:  " + extra.mfId);
                        predictConfig.mfId = extra.mfId;
                        fileName = file;
                        $(fileNameInput).val(file);
                        $(chartIcon).removeClass("hidden");
                        if (uploadListener)
                            uploadListener();
                    });

                    uploadForm.attachEvent("onUploadFail", function (file, extra) {
                        $('.re-loading-spinner-background').hide();
                        dhtmlx.message({type: "errorMsg", text: extra, expire: 5000});
                        uploadForm.setItemValue("quotesUpload");
                        $(fileNameInput).val("");
                        $(chartIcon).addClass("hidden");
                        predictConfig.mfId = null;
                    });

                    frequency.attachEvent("onChange", function (value, text) {
                        if (value === "" || value === null || value === undefined)
                            value = "TWO_WEEKS";
                        predictConfig.config.historicalPriceFrequency = value;
                    });

                    $('#predictStep', predictTree).on('change', function () {
                        if (this.value === "" || this.value === null || this.value === undefined)
                            this.value = "0";
                        predictConfig.config.historicalPredictionStep = this.value;
                    });

                    historicalInterval.setValue(predictConfig.config.historicalInterval);
                    predictionHorizon.setValue(predictConfig.config.predictionHorizon);
                    backwardAnalyticalPeriod.setValue(predictConfig.config.backwardAnalyticalPeriod);

                    historicalInterval.onChange(function (tenor) {
                        predictConfig.config.historicalInterval = tenor;
                    });
                    predictionHorizon.onChange(function (tenor) {
                        predictConfig.config.predictionHorizon = tenor;
                    });
                    backwardAnalyticalPeriod.onChange(function (tenor) {
                        predictConfig.config.backwardAnalyticalPeriod = tenor;
                    });

                };
                this.addOnUploadListener = function (listener) {
                    uploadListener = listener;
                };
                this.getPredictConfigInJsonFormat = function () {
                    return predictConfig;
                };
                this.getFrequencyUnit = function () {
                    return frequencyMap[predictConfig.config.historicalPriceFrequency];
                };
                return this;
            };

            component.makeController = function (activeWindow) {
                $(activeWindow).append(template);
                var controller = new PredictionConfigController();
                controller.init($("#predictConfig", activeWindow));
                return controller;
            };
            return component;
        });

