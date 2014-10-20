define(["require", "exports"], function (require, exports) {
    function getQueryParam() {
        var queryMap = {};
        if (location.search.length > 0) {
            location.search.substring(1).split("&").map((function (arg) { return arg.split("="); })).forEach(function (arg) { return queryMap[arg[0]] = arg[1]; });
        }
        return queryMap;
    }
    exports.getQueryParam = getQueryParam;
    function request(requestData) {
        var req = new XMLHttpRequest();
        req.open(requestData.method, requestData.url, true);
        req.onreadystatechange = function () {
            if (req.readyState == 4) {
                if (req.status < 300) {
                    if (requestData.onSuccess) {
                        requestData.onSuccess(req.response, req);
                    }
                }
                else {
                    if (requestData.onError) {
                        requestData.onError(req.response, req);
                    }
                }
            }
        };
        req.send(requestData.data);
    }
    exports.request = request;
});
//# sourceMappingURL=utils.js.map