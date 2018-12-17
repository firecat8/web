define([
    "jquery",
    "text!app/templates/FactorsSelectionCfgTemplate.html",
    "common/FactorsSelector",
    "common/FrequencySelector",
    "app/TenorComponent",
    "app/MfSelector"],
        function ($, template, factorsSelector, frequencySelector, tenorComponent, mfSelector) {
            var component = {};
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
            var init = function (wrapperDom, factorSelectionCfg, series) {

                var mfInput = mfSelector.makeController($(".mfSelector", wrapperDom)[0]);
                var frequency = frequencySelector.createCombo($("#frequency", wrapperDom)[0]);
                var historicalInterval = tenorComponent.makeController($("#historicalInterval", wrapperDom)[0]);
                var suggestionMethod = new dhtmlXCombo($("#suggestionMethod", wrapperDom)[0], "suggestionMethod", null, "image");
                suggestionMethod.addOption(suggestionMethodData);

                historicalInterval.setValue(factorSelectionCfg.historicalInterval);
                $('#maximalNumberInput', wrapperDom).val(factorSelectionCfg.maxSuggestions);
                $('#minimalQualityInput', wrapperDom).val(factorSelectionCfg.minimalQuality);
                frequency.selectOption(frequencySelector.getFrequencyId(factorSelectionCfg.frequency));

                mfInput.addOnChangeListener(function (id, seriesName) {
                    factorSelectionCfg.seriesName = seriesName;
                    series.id = id;
                });

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


            component.makeController = function (parentDom) {
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

                init($("#factorSelectionCfg", parentDom)[0], factorSelectionCfg, series);

                controller.getValue = function () {
                    return factorSelectionCfg;
                };

                controller.getSeriesId = function () {
                    return series.id;
                };

                return controller;
            };
            return component;
        });

