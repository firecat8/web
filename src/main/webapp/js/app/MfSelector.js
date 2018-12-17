define([
	"jquery",
	"text!app/templates/MfSelectorTemplate.html",
	"common/FactorsSelector",
	"app/WindowManager"],
		function ($, template, factorsSelector, windowManager) {
			var component = {};

			component.makeController = function (parentDom) {
				$(parentDom).append(template);
				var controller = {};
				var seriesName = "";
				var id = "";
				var onChangeListeners = [];

				var mfInput = factorsSelector.createCombo($("#mfName", parentDom)[0]);
				mfInput.attachEvent("onChange", function (value, text) {
					if (value === null || value === undefined)
						value = "";
					seriesName = text;
					id = value;
					$(".chart-icon", parentDom).removeClass("hidden");
					for(var i=0; i < onChangeListeners.length; i++){
						onChangeListeners[i](id, seriesName);
					}
				});

				$(".chart-icon", parentDom).click(function () {
					requirejs(['controller/SeeSeriesController'], function (esgController) {
						var win = windowManager.createWindow("Series " + seriesName);
						var winId = esgController.makeController(win, id, windowManager);
						windowManager.showWindow(winId);
					});
				});

				controller.getValue = function () {
					return id;
				};
				
				controller.addOnChangeListener = function (listener) {
					onChangeListeners.push(listener);
				};

				return controller;
			};
			return component;
		});

