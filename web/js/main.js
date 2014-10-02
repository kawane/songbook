define(["require", "exports", "./songbook", "./utils"], function(require, exports, songbook, utils) {
    var queryParam = utils.getQueryParam();

    var key = queryParam["key"];

    if (key !== undefined) {
        songbook.installEditionModeActivation();
    }

    var searchForm = document.querySelector("#search");
    var queryInputSearch = document.querySelector("#querySearch");
    if (searchForm) {
        searchForm.addEventListener("submit", function (e) {
            e.preventDefault();
            songbook.search(key, queryInputSearch.value);
        });
    }

    var searchPath = "/search/";
    if (window.location.pathname.indexOf(searchPath) == 0) {
        var searchQuery = decodeURIComponent(window.location.pathname.substring(searchPath.length));
        if (queryInputSearch) {
            queryInputSearch.value = searchQuery;
        }
    }
});
//# sourceMappingURL=main.js.map
