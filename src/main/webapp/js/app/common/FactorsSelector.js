/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
define(["jquery",
	"common/Ajax"], function ($, ajaxModule) {

	var factorsSelector = {};

	factorsSelector.createCombo = function (parentDom) {
		var mfInput = new dhtmlXCombo(parentDom, "combo", null, "image");
		$('.re-loading-spinner-background').show();
		ajaxModule.GET("getFactors", function (data) {
			$('.re-loading-spinner-background').hide();
			mfInput.addOption(data);
		},
				function (data) {
					$('.re-loading-spinner-background').hide();
					dhtmlx.message({
						type: "errorMsg",
						text: data.statusText,
						expire: 5000
					});
				});
		mfInput.enableFilteringMode(true);
		mfInput.enableAutocomplete(true);

		return mfInput;
	};

	return factorsSelector;
});
