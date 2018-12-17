<html lang="en">
    <head>
        <title>Welcome</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="stylesheet" type="text/css" href="css/skins/skyblue/dhtmlx.css"/>
        <link rel="stylesheet" type="text/css" href="css/skins/terrace/dhtmlx.css"/>
        <link rel="stylesheet" type="text/css" href="css/skins/web/dhtmlx.css"/>
        <link rel="stylesheet" type="text/css" href="css/common.css"/>
        <link rel="stylesheet" type="text/css" href="css/config.css"/>


        <script src="js/lib/dhtmlx.js"></script>
        <script src="js/lib/ext/swfobject.js"></script>

        <script src="js/lib/require-2.3.5.js"></script>
        <script type="text/javascript" >
            require(["js/lib/config.js"]);
        </script>

    </head>

    <body>

        <div class="header">

            <img id="header-logo" alt="relogo" src="css/img/reLogo.png"/>

        </div>
        <div class="clear">
            <div class="menu" id="menu">
                <div id="openWindowsDiv"></div>
            </div>
        </div>
        <div class="clear">
            <div class="re-loading-spinner-background">
                <img 
                    src="css/skins/web/imgs/dhxcarousel_web/dhxcarousel_cell_progress.gif"
                    data-tooltip-key="LoadingSpinnerComponent_loading" 
                    data-tooltip="Loading, please wait"
                    alt="Loading, please wait..." />
            </div>
        </div>
        <div id="winVP" style="width:100%;heigth:100%;oveflow:auto"></div>

    </body>
</html>