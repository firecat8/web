/* 
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
define(["jquery"], function ($) {

    var component = {};
    component.makeController = function (WindowManager) {
        var controller = {};
        var openWindowsCombo = new dhtmlXCombo("openWindowsDiv", "comboWindows", 160, "image");
        openWindowsCombo.setComboText("Open windows");
        openWindowsCombo.readonly(true);
        openWindowsCombo.attachEvent("onSelectionChange", function () {
            var selectedIndex = openWindowsCombo.getSelectedIndex();
            if (selectedIndex !== -1) {
                var option = openWindowsCombo.getOptionByIndex(selectedIndex);
                WindowManager.showWindow(option.value);
            }
            openWindowsCombo.setComboText("Open windows");
        });

        controller.addWindowInList = function (winName, winId) {
            openWindowsCombo.addOption([{value: winId.toString(), text: winName, selected: true}]);
        };
        controller.removeWindowFromList = function (winId) {
            openWindowsCombo.deleteOption(winId.toString());
        };
        return controller;
    };
    return component;
});