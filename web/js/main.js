define(["require", "exports", "./songbook", "./utils"], function (require, exports, songbook, utils) {
    var queryParam = utils.getQueryParam();
    var key = queryParam["key"];
    if (key !== undefined) {
        var path = window.location.pathname;
        var editionMode = path.substring(path.length - 4, path.length) === "/new";
        songbook.installEditionMode(editionMode);
    }
    var searchForm = document.querySelector("#search");
    var queryInputSearch = document.querySelector("#querySearch");
    if (searchForm) {
        searchForm.addEventListener("submit", function (e) {
            e.preventDefault();
            songbook.search(queryInputSearch.value);
        });
    }
    var searchPath = "/search/";
    if (window.location.pathname.indexOf(searchPath) == 0) {
        var searchQuery = decodeURIComponent(window.location.pathname.substring(searchPath.length));
        if (queryInputSearch) {
            queryInputSearch.value = searchQuery;
        }
    }
    var message = queryParam["message"];
    if (message) {
        songbook.createAlert(message, "info");
    }
});
//# sourceMappingURL=main.js.map