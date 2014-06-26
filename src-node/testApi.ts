///<reference path='typings/node/node.d.ts'/>
/**
 * Created by laurent on 14/05/2014.
 */


import http = require("http");
import fs = require("fs");



function get(options, callbackdata) {
    http.get(options, function(resp) {
        var data = "";
        resp.on("data", function(chunk) {
            data += chunk;
        });
        resp.on("end", function() {
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
        headers: {"Accept": "text/html"}
    };
    get(options, (data) => {
        var songs = JSON.parse(data);
        songs.forEach((song) => {
            options.path = "/rest/song/" + song.Id + "?noheader=true";
            get(options, (songData) => {
                fs.writeFile("../data/songs/" + song.Title + ".html", songData, {encoding: "utf-8"});
            });
        });
    })
}


function createNewSong(requestOptions: {host: string; port: number}, songdata, done: (id: string) => void) {
    var options = {
        host: requestOptions.host,
        port: requestOptions.port,
        method: "POST",
        path: "/songs"
    };

    var req = http.request(options, (resp) => {
        var id = "";
        resp.on("data", (chunk) => {
            id += chunk;
        });
        resp.on("end", () => {
            done(id);
        });
    });
    req.write(songdata);
    req.end();
}


var requestOptions = {host: "localhost", port: 8080};

createNewSong(
    requestOptions,
    '<div class="song"><div class="song-title">My song</div></div>',
    (id) => {
        console.log("Created song: " + id);
    });



