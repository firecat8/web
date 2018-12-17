/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
define(["jquery"], function ($) {
	var table = {};

	table.createTable = function (domElement, pagingDomElement, headers) {
		var myGrid = domElement.attachGrid();
		myGrid.setHeader(headers);
		myGrid.setImagePath("/inea/css/imgs/dhxgrid_terrace/");
		//myGrid.enablePaging(true, 20, 5, pagingDomElement, true);
	//	myGrid.setPagingSkin("toolbar", "dhx_terrace");
		return myGrid;
	};

	table.init = function (table2) {
		table2.init();
	}

	table.addData = function (table2, data) {
		table2.parse(data, "json");
	}
	return table;
});