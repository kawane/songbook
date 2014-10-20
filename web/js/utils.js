define(["require", "exports"], function (require, exports) {
    function getQueryParam() {
        var queryMap = {};
        if (location.search.length > 0) {
            location.search.substring(1).split("&").map((function (arg) { return arg.split("="); })).forEach(function (arg) { return queryMap[arg[0]] = arg[1]; });
        }
        return queryMap;
    }
    exports.getQueryParam = getQueryParam;
});
//# sourceMappingURL=utils.js.map