define([
    "jquery",
    "text!app/templates/IntervalCalendarComponentTemplate.html",
    "app/WindowManager"],
        function ($, template, windowManager) {
            var component = {};

            var setError = function (msg) {
                dhtmlx.message({
                    type: "errorMsg",
                    text: msg,
                    expire: 5000
                });
            };
            var IntervalCalendarController = function () {

                var legendName;
                var intervalLabel;
                var date_from;
                var date_to;
                var intervalButton;
                var intervalCalendar;
                var start_date;
                var end_date;
                
                var checkDates = function () {
                    var from = $(date_from).val();
                    var to = $(date_to).val();
                    if (from === to) {
                        setError("The dates must be different!");
                        return false;
                    }
                    var from_ms = new Date(intervalCalendar._strToDate(from)).getTime();
                    var to_ms = new Date(intervalCalendar._strToDate(to)).getTime();
                    if (from_ms > to_ms) {
                        setError("The dates interval [From, To] is not correct!");
                        return false;
                    }
                    return true;
                };

                this.init = function (domTree, config) {
                    legendName = $("#legendName", domTree)[0];
                    $(legendName).text(config.legend);
                    intervalLabel = $("#intervalLabel", domTree)[0];
                    date_from = $("#date_from", domTree)[0];
                    date_to = $("#date_to", domTree)[0];
                    intervalCalendar = new dhtmlXCalendarObject([date_from, date_to]);
                    intervalCalendar.setDateFormat(config.format);
                    intervalCalendar.hideTime();
                    intervalCalendar.attachEvent("onClick", function (date) {
                        checkDates(date);
                    });
                    intervalButton = $("#intervalButton", domTree)[0];
                    $(intervalButton).val(config.btnValue);
                };

                this.setInterval = function (startDate, endDate) {
                    start_date = startDate;
                    end_date = endDate;
                    $(date_from).val(startDate);
                    $(date_to).val(endDate);
                    $(intervalLabel).text("Start date: " + startDate + "       End date: " + endDate);
                    intervalCalendar.setSensitiveRange(startDate, endDate);
                };

                this.onButtonClick = function (onClickEvent) {
                    $(intervalButton).on("click", function () {
                        if (!checkDates())
                            return;
                        onClickEvent(
                                $(date_from).val(),
                                $(date_to).val()
                                );
                    });
                };

                this.getISODate = function (strDate) {
                    var date = intervalCalendar._strToDate(strDate);
                    return intervalCalendar._dateToStr(date, "%Y-%m-%d");
                };

                return this;
            };


            component.makeController = function (activeWindow, config) {
                $(activeWindow).append(template);
                var controller = new IntervalCalendarController();
                controller.init($("#intervalConfig", activeWindow), config);
                return controller;
            };
            return component;
        });