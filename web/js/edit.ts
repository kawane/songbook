
import SongApi = require("./SongApi");

// JavaScript imports
declare var ace;

var songTextEdit = ace.edit("song");
songTextEdit.getSession().setMode("ace/mode/song");
songTextEdit.renderer.setShowGutter(false);
songTextEdit.setOptions({
    maxLines: Infinity
});
songTextEdit.resize();

var api = new SongApi("/songs/");
var saveButton = document.getElementById("saveButton");
saveButton.addEventListener("click", (e) => {
    e.preventDefault();
    var id = songTextEdit.container.dataset["songid"];
    if (id) {
        api.update(id, songTextEdit.getValue(), (id: string) => {
            location.pathname = "/songs/" + id;
        }, (error) => {
            console.log(error);
        });
    } else {
        api.create(songTextEdit.getValue(), (id: string) => {
            location.pathname = "/songs/" + id;
        }, (error) => {
            console.log(error);
        })
    }
});


