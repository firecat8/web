define([], function () {

    var windows = new dhtmlXWindows();
    windows.setSkin("dhx_skyblue");
    windows.attachViewportTo("winVP");
    var window = {};
    var listeners = [];
    var winPos = 0;
    window.createWindow = function (windowName) {
        if (winPos === 10)
            winPos = 0;
        var win = windows.createWindow(windowName, winPos * 10, winPos * 10, 1300, 600);
        win.setText(windowName);
        winPos++;
        return win;
    };

    window.addCloseListener = function (winId, listener) {
        if (listeners[winId] === undefined) {
            listeners[winId] = [];
        }
        listeners[winId].push(listener);
    };
    window.showWindow = function (windowId) {
        windows.window(windowId).setModal(true);
        windows.window(windowId).show();
        windows.window(windowId).setModal(false);
    };
    window.showWindowModal = function (windowId) {
        windows.window(windowId).setModal(true);
        windows.window(windowId).show();
    };
    window.attachOnCloseWindowEvent = function (openWindowsComponent) {
        windows.attachEvent("onClose", function (win) {
            var id = win.getId();
            if (listeners[id]) {
                var allListeners = listeners[id];
               // console.log(id);
                for (var i = 0; i < allListeners.length; i++) {
                    allListeners[i]();
                }
                listeners[id] = undefined;
                openWindowsComponent.removeWindowFromList(id);
                return true;
            }

            return false;
        });
    };
    return window;
});

