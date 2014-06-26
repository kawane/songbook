///<reference path='typings/node/node.d.ts'/>
/**
* Created by laurent on 14/05/2014.
*/
var http = require("http");
var fs = require("fs");

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

// retrieve song list then retrieve song by song and save it to songs folder
function saveAllSongsFromAppspot() {
    var options = {
        host: "songbookchord.appspot.com",
        port: 80,
        path: "/rest/song/",
        headers: { "Accept": "text/html" }
    };
    get(options, function (data) {
        var songs = JSON.parse(data);
        songs.forEach(function (song) {
            options.path = "/rest/song/" + song.Id + "?noheader=true";
            get(options, function (songData) {
                fs.writeFile("../data/songs/" + song.Title + ".html", songData, { encoding: "utf-8" });
            });
        });
    });
}

function createNewSong(requestOptions, songdata, done) {
    var options = {
        host: requestOptions.host,
        port: requestOptions.port,
        method: "POST",
        path: "/songs"
    };

    var req = http.request(options, function (resp) {
        var id = "";
        resp.on("data", function (chunk) {
            id += chunk;
        });
        resp.on("end", function () {
            done(id);
        });
    });
    req.write(songdata);
    req.end();
}

var requestOptions = { host: "localhost", port: 8080 };

createNewSong(requestOptions, '<div class="song"><div class="song-title">My song</div></div>', function (id) {
    console.log("Created song: " + id);
});
