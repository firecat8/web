/* 
 * 
 *  EuroRisk Systems (c) Ltd. All rights reserved.
 * 
 */
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
                if (mfId && mfId !== '')
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
            };
            var titleText = "Drag-n-Drop Excel file here or <br> click to select Excel file for upload";
            var UploadFileController = function () {

                var uploadForm;
                var fileNameInput;
                var chartIcon;
                var fileName;
                this.init = function (domTree) {
                    fileNameInput = $("#fileNameInput", domTree)[0];
                    chartIcon = $(".chart-icon", domTree)[0];
                    $(chartIcon).click(function () {
                        requirejs(['controller/SeeSeriesController'], function (seeSeriesController) {
                            var win = windowManager.createWindow("Series " + fileName);
                            var winId = seeSeriesController.makeController(win, predictConfig.mfId, windowManager);
                            windowManager.showWindow(winId);
                        });
                    });
                    uploadForm = new dhtmlXForm($("#uploadForm", domTree)[0],
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
                    uploadForm.attachEvent("onUploadFile", function (file, file2, extra) {
                        dhtmlx.message({type: "successMsg", text: "Successfully uploaded serie!", expire: 5000});
                        var prevMfId = predictConfig.mfId;
                        console.log("Name:  " + extra.mfName);
                        console.log("ID:  " + extra.mfId);
                        predictConfig.mfId = extra.mfId;
                        fileName = file;
                        $(fileNameInput).val(file);
                        $(chartIcon).removeClass("hidden");
                        deleteSeries(prevMfId);
                    });
                    uploadForm.attachEvent("onUploadFail", function (file, extra) {
                        dhtmlx.message({type: "errorMsg", text: extra, expire: 5000});
                        predictConfig.mfId = null;
                    });
                };
            };
            component.makeController = function (activeWindow) {
                $(activeWindow).append(template);
                var controller = new UploadFileController();
                controller.init($("#uploadFileForm", activeWindow));
                return controller;
            };
            return component;
        });