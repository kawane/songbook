define(["require", "exports", "./SongApi"], function (require, exports, SongApi) {
    var songTextEdit = document.getElementById("song");
    if (songTextEdit) {
        var api = new SongApi("/songs/");
        var saveButton = document.getElementById("saveButton");
        saveButton.addEventListener("click", function (e) {
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
//# sourceMappingURL=edit.js.map