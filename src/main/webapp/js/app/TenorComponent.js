/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */

define([
	"jquery",
	"text!app/templates/TenorTemplate.html"],
		function ($, template) {
			var component = {};

			var attachOnChangeListeners = function (parentDom, tenor, onChangeListeners) {
				$.each(Object.keys(tenor), function (idx, elementClass) {
					$("." + elementClass, parentDom).on('change', function () {
						if (this.value === null || this.value === undefined || this.value === "")
							this.value = 0;
						tenor[elementClass] = this.value;
						for (var i = 0; i < onChangeListeners.length; i++) {
							onChangeListeners[i].apply(this, [tenor]);
						}
					});
				});
			};

			var visualize = function (parentDom, tenor) {
				$.each(Object.keys(tenor), function (idx, elementClass) {
					$("." + elementClass, parentDom).val(tenor[elementClass]);
				});
			};
			component.makeController = function (parentDom) {
				var controller = {};
				var onChangeListeners = [];

				$(parentDom).append(template);
				var tenor = {"years": 0, "months": 0, "days": 0};

				attachOnChangeListeners(parentDom, tenor, onChangeListeners);
				visualize(parentDom, tenor);

				controller.setValue = function (t) {
					tenor.years = t.years;
					tenor.months = t.months;
					tenor.days = t.days;
					visualize(parentDom, t);
				};
				controller.getValue = function () {
					return tenor;
				};
				controller.onChange = function (listener) {
					onChangeListeners.push(listener);
				};

				return controller;
			};
			return component;
		});

