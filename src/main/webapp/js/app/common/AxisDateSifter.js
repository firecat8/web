/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
define([], function () {

    var sifter = {};
    var tenor = {};
    var conditions = [
        tenor => {
            return tenor.years >= 5;
        },
        tenor => {
            return tenor.years > 3;
        },
        tenor => {
            return tenor.months > 5 || tenor.years > 0;
        },
        tenor => {
            return tenor.months > 2;
        },
        tenor => {
            return tenor.months > 1;
        },
        tenor => {
            return tenor.months === 1;// && tenor.days <= 10;
        },
        tenor => {
            return true;
        }
    ];

    var functions = [
        date => {
            var x = parseInt(tenor.years / 10) + 1;
            return date.getFullYear() % x === 0 && date.getMonth() === 0 && date.getDate() === 1;
        },
        date => {
            return  date.getMonth() % 6 === 0 && date.getDate() === 1;
        },
        date => {
            var x = parseInt((tenor.months + tenor.years * 12) / 10) + 1;
            return  date.getMonth() % x === 0 && date.getDate() === 1;
        },
        date => {
            return  date.getDate() % 16 === 0;
        },
        date => {
            return  date.getDate() % 8 === 0;
        },
        date => {
            if (tenor.days % 5 === 0)
                return date.getDate() % 5 === 0;
            if (tenor.days % 3 === 0)
                return date.getDate() % 3 === 0;
            if (tenor.days % 2 === 0)
                return date.getDate() % 2 === 0;
            return  date.getDate() % 5 === 0;
        },
        date => {
            var x = parseInt(tenor.days / 10) + 1;
            return  date.getDate() % x === 0;
        }
    ];
    var conditionIndex;

    sifter.setRange = function (startDate, endDate) {

        tenor.years = endDate.getFullYear() - startDate.getFullYear();
        tenor.months = endDate.getMonth() - startDate.getMonth();
        tenor.days = endDate.getDate() - startDate.getDate();
        for (var i = 0; i < conditions.length; i++) {
            if (conditions[i](tenor)) {
                conditionIndex = i;
                break;
            }
        }
    };

    sifter.siftDates = function (date) {
        return functions[conditionIndex](date) ? dhtmlx.Date.date_to_str("%d/%m/%y")(date) : "";
    };

    return sifter;
});

