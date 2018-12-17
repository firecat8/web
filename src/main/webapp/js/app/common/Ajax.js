define(["jquery"], function ($) {
    var ajaxModule = {};
//    http://localhost:8080/inea/
//    var baseUrl = "";

    var ajax = function (servletUrl, method, data, success, error, processData, contentType, dataType, enctype) {

        var cfg = {
            url: servletUrl,
            method: method,
            enctype: enctype,
            data: data,
            processData: processData,
            dataType: dataType,
            contentType: contentType,
            success: success,
            error: error
        };

        if (method === "POST" && data !== undefined && enctype === undefined) {
            cfg.data = JSON.stringify(data);
        }

        $.ajax(cfg);
    };

    ajaxModule.POST = function (servletUrl, data, success, error) {
        ajax(servletUrl, "POST", data, success, error, undefined, 'application-json', 'json', undefined);
    };

    ajaxModule.GET = function (servletUrl, success, error) {
        ajax(servletUrl, "GET", undefined, success, error, undefined, 'application-json', 'json', undefined);
    };
    ajaxModule.postFile = function (servletUrl, data, success, error) {
        ajax(servletUrl, "POST", data, success, error, false, false, undefined, 'multipart/form-data');
    };

    return ajaxModule;
});


