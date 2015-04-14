define(["require", "exports", "./SongApi"], function (require, exports, SongApi) {
    var songTextEdit = ace.edit("song");
    songTextEdit.getSession().setMode("ace/mode/song");
    songTextEdit.renderer.setShowGutter(false);
    songTextEdit.setOptions({
        maxLines: Infinity
    });
    songTextEdit.resize();
    var api = new SongApi("/songs/");
    var saveButton = document.getElementById("saveButton");
    saveButton.addEventListener("click", function (e) {
        e.preventDefault();
        var id = songTextEdit.container.dataset["songid"];
        if (id) {
            api.update(id, songTextEdit.getValue(), function (id) {
                location.pathname = "/songs/" + id;
            }, function (error) {
                console.log(error);
            });
        }
        else {
            api.create(songTextEdit.getValue(), function (id) {
                location.pathname = "/songs/" + id;
            }, function (error) {
                console.log(error);
            });
        }
    });
});
//# sourceMappingURL=edit.js.map