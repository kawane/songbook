define(["require", "exports"], function (require, exports) {
    var forEach = function (list, callback, context) {
        return Array.prototype.forEach.call(list, callback, context);
    };
    function keyboardListener(e) {
        if (e.keyCode === 13) {
            var selection = window.getSelection();
            if (selection.anchorNode.parentNode.nodeType == Node.ELEMENT_NODE) {
                var parentElement = selection.anchorNode.parentNode;
                if (parentElement.classList.contains("song-line")) {
                    var songChords = document.createElement("div");
                    songChords.className = "song-chords";
                    songChords.innerHTML = " ";
                    var songLine = document.createElement("div");
                    songLine.className = "song-line";
                    songLine.innerHTML = " ";
                    parentElement.parentElement.insertBefore(songLine, parentElement.nextSibling);
                    parentElement.parentElement.insertBefore(songChords, parentElement.nextSibling);
                    e.preventDefault();
                }
                else if (parentElement.classList.contains("song-line")) {
                    var songChords = document.createElement("div");
                    songChords.className = "song-chords";
                    songChords.innerHTML = " ";
                    parentElement.parentElement.insertBefore(songChords, parentElement.nextSibling);
                    e.preventDefault();
                }
            }
        }
    }
    function startEdition(song) {
        var title = song.querySelector(".song-title");
        title.contentEditable = "true";
        song.removeEventListener("keypress", keyboardListener);
        song.addEventListener("keypress", keyboardListener);
        forEach(song.querySelectorAll(".song-author"), function (author) {
            author.contentEditable = "true";
        });
        forEach(song.querySelectorAll(".song-verse"), function (author) {
            author.contentEditable = "true";
        });
    }
    exports.startEdition = startEdition;
    function endEdition(song) {
        song.removeEventListener("keypress", keyboardListener);
        forEach(song.querySelectorAll("*[contenteditable='true']"), function (elt) {
            elt.contentEditable = "false";
        });
    }
    exports.endEdition = endEdition;
});
//# sourceMappingURL=editSong.js.map