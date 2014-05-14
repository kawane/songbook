/**
 * Created by laurent on 14/05/2014.
 */

import http = require("http");
import fs = require("fs");

var options = {
    host: "songbookchord.appspot.com",
    port: 80,
    path: "/rest/song/",
    headers: {"Accept": "text/plain"}
};

function get(options, callbackdata) {
    http.get(options, function(resp) {
        var data = "";
        resp.on("data", function(chunk) {
            data += chunk;
        })
        resp.on("end", function() {
            callbackdata(data);

        })
    });

}


get(options, (data) => {
    var songs = JSON.parse(data);
    songs.forEach((song) => {
        options.path = "/rest/song/" + song.Id
        get(options, (songData) => {
            fs.writeFile(song.Title + ".cho", songData, {encoding: "utf-8"});
        });
    });
})



