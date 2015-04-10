define(["require", "exports", "./songbook", "./utils", "./songApi"], function (require, exports, songbook, utils, songApi) {
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
    var songTextEdit = document.getElementById("song");
    if (songTextEdit) {
        var api = songApi.create(songApi.PathUrl);
        var editButton = document.getElementById("editButton");
        editButton.addEventListener("click", function (e) {
            e.preventDefault();
            var id = songTextEdit.dataset["songid"];
            if (id) {
                api.update(id, songTextEdit.value, function (id) {
                    location.pathname = "/songs/" + id;
                }, function (error) {
                    console.log(error);
                });
            }
            else {
                api.create(songTextEdit.value, function (id) {
                    location.pathname = "/songs/" + id;
                }, function (error) {
                    console.log(error);
                });
            }
        });
    }
});
//# sourceMappingURL=main.js.map