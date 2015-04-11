
import SongApi = require("./SongApi");

var songTextEdit = <HTMLTextAreaElement>document.getElementById("song");
if (songTextEdit) {
    var api = new SongApi("/songs/");
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
