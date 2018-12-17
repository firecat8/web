/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
define([], function () {
    var frequencySelector = {};

    var data = [
        ["DAILY", "Daily"],
        ["WEEKLY", "Weekly"],
        ["TWO_WEEKS", "Two weeks", null, null, true],
        ["MONTHLY", "Monthly"],
        ["TWO_MONTHS", "Two months"],
        ["QUARTERLY", "Quaterly"],
        ["FOUR_MONTHS", "Four months"],
        ["SEMI_ANNUALLY", "Semi annualy"],
        ["ANNUAL", "Annual"],
        ["BIANNUAL", "Bianual"]
    ];

    frequencySelector.createCombo = function (parentDom) {
        var mfInput = new dhtmlXCombo(parentDom, "combo", null, "image");
        mfInput.addOption(data);
        return mfInput;
    };

    frequencySelector.getFrequencyId = function (NAME) {
        for (var i = 0; i < data.length; i++) {
            if(data[i][0]=== NAME){
                return i;
            }
        }
        return 2;
    };
    return frequencySelector;
});