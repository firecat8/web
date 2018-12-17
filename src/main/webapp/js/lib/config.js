(function () {
	requirejs.config({
		baseUrl: "js",
		paths: {
			app: "app",
			common: "app/common",
			controller: "app/controller",
			style: "css/",
			i18n: "lib/i18n",
			domReady: "lib/domReady",
			css: "lib/css",
			text: "lib/text",
			jquery: "lib/jquery-3.3.1.min",
			dhtmlx: "lib/dhtmlx",
			dhtmlx_component: "lib/dhtmlx_component/",
			dhtmlx_ext: "lib/ext/",
			dhtmlx_excells: "lib/excells/"
		}
	});
	requirejs(["jquery", "domReady", "app/main"]);
})();