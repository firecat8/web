define(["app/WindowManager", "app/OpenWindowsComponent"], function (window, openWindows) {

    var Menu = {};

    Menu.initMenus = function () {
        var mainMenu = new dhtmlXMenuObject({
            parent: "menu"}
        );
        mainMenu.addNewSibling(null, "ESG", "ESG", false);
        mainMenu.addNewSibling(null, "Prediction", "Prediction", false);
       // mainMenu.addNewSibling(null, "SeeSeries", "See series", false);
       // mainMenu.addNewSibling(null, "Upload", "Upload", false);

        var openWindowsController = openWindows.makeController(window);
        mainMenu.attachEvent("onClick", function (id, zoneId, cas) {
            requirejs(['controller/' + id + "Controller"], function (controller) {
                var win = window.createWindow(id);
                var winId = controller.makeController(win, window);
                window.showWindow(winId);
                openWindowsController.addWindowInList(id, winId);
            });
        });
        window.attachOnCloseWindowEvent(openWindowsController);
        var logoutMenu = new dhtmlXMenuObject({
            parent: "menu"}
        );
        logoutMenu.addNewSibling(null, "Logout", "Logout", false);
		var appHome = document.location.host;
        logoutMenu.attachEvent("onClick", function (id, zoneId, cas) {
            var logoutServletUrl = "http://"+appHome+"/inea/logout";
            location.href = logoutServletUrl;
        });
        logoutMenu.setAlign("right");

        logoutMenu.setSkin("dhx_web");
        mainMenu.setSkin("dhx_web");
    };
    return Menu;
});
