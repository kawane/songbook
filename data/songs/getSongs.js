/**
* Created by laurent on 14/05/2014.
*/
var http = require("http");
var fs = require("fs");

var options = {
    host: "songbookchord.appspot.com",
    port: 80,
    path: "/rest/song/",
    headers: { "Accept": "text/html" }
};

function get(options, callbackdata) {
    http.get(options, function (resp) {
        var data = "";
        resp.on("data", function (chunk) {
            data += chunk;
        });
        resp.on("end", function () {
            callbackdata(data);
        });
    });
}

get(options, function (data) {
    var songs = JSON.parse(data);
    songs.forEach(function (song) {
        options.path = "/rest/song/" + song.Id + "?noheader=true";
        get(options, function (songData) {
            fs.writeFile(song.Title + ".html", songData, { encoding: "utf-8" });
        });
    });
});
//# sourceMappingURL=getSongs.js.map
