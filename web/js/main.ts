/// <reference path ='./SongApi'/>
import songbook = require("./songbook");
import utils = require("./utils");

var queryParam = utils.getQueryParam();

var key = queryParam["key"];

if (key !== undefined) {
    var path = window.location.pathname;
    var editionMode = path.substring(path.length-4, path.length) === "/new";
    songbook.installEditionMode(editionMode);
}

var searchForm = document.querySelector("#search");
var queryInputSearch = <HTMLInputElement>document.querySelector("#querySearch");
if (searchForm) {
    searchForm.addEventListener("submit", (e) => {
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

var songTextEdit = <HTMLTextAreaElement>document.getElementById("song");
import songApi = require("./songApi");
if (songTextEdit) {
    var api = songApi.create(songApi.PathUrl);
    var editButton = document.getElementById("editButton");
    editButton.addEventListener("click", (e) => {
        e.preventDefault();
        var id = songTextEdit.dataset["songid"];
        if (id) {
            api.update(id, songTextEdit.value, (id: string) => {
                location.pathname = "/songs/" + id;
            }, (error) => {
                console.log(error);
            });
        } else {
            api.create(songTextEdit.value, (id: string) => {
                location.pathname = "/songs/" + id;
            }, (error) => {
                console.log(error);
            })
        }
    });

}



