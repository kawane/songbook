define(["require", "exports"], function(require, exports) {
    function createListItem(inner) {
        var li = document.createElement("li");
        if (inner != null)
            li.appendChild(inner);
        return li;
    }

    function createButton(glyph, target, action) {
        var button = document.createElement("a");
        var buttonGlyph = document.createElement("span");
        buttonGlyph.classList.add("glyphicon");
        buttonGlyph.classList.add("glyphicon-" + glyph);
        button.appendChild(buttonGlyph);
        button.addEventListener("click", function () {
            action(target, button);
        });
        return button;
    }

    function updateGlyph(button, glyph) {
        var buttonGlyph = button.querySelector("span.glyphicon");
        if (buttonGlyph != null) {
            buttonGlyph.className = "glyphicon glyphicon-" + glyph;
        }
    }

    function postSong(result) {
        // retrieves id from location
        var pathname = document.location.pathname;
        var id = pathname.substring(pathname.lastIndexOf('/') + 1);

        // retrieves key from location
        // TODO handle undefined key
        var search = document.location.search;
        var keyStart = search.indexOf("key=");
        var keyEnd = search.indexOf("&");
        var key = search.substring(keyStart + 4, keyEnd >= 0 ? keyEnd : search.length);

        var request = new XMLHttpRequest();
        request.open("put", "/songs/" + id + "?key=" + key, true);

        request.onreadystatechange = result;

        var song = document.querySelector(".song");
        request.send("<div class=\"song\">" + song.innerHTML + "</div>");
    }

    function createEditButton(song) {
        return createButton("pencil", song, function (target, button) {
            var edited = target.attributes["edited"];

            if (edited === undefined || edited === true) {
                target.attributes["edited"] = false;
                target.classList.add("edited");
                updateGlyph(button, "send");

                var title = target.querySelector(".song-title");
                title.contentEditable = "true";

                var authors = target.querySelectorAll(".song-author");
                for (var index in authors) {
                    var author = authors[index];
                    author.contentEditable = "true";
                }

                var verse = target.querySelector(".song-verse");
                verse.contentEditable = "true";
            } else {
                target.attributes["edited"] = true;
                target.classList.remove("edited");
                updateGlyph(button, "refresh");

                var title = target.querySelector(".song-title");
                title.contentEditable = "false";

                var authors = target.querySelectorAll(".song-author");
                for (var index in authors) {
                    var author = authors[index];
                    author.contentEditable = "false";
                }

                var verse = target.querySelector(".song-verse");
                verse.contentEditable = "false";

                postSong(function (event) {
                    var request = event.currentTarget;
                    if (request.readyState == 4) {
                        if (request.status == 200) {
                            updateGlyph(button, "pencil");
                        } else {
                            updateGlyph(button, "warning_sign");
                        }
                    }
                });
            }
        });
    }

    function createAddButton() {
        return createButton("plus", null, function (target, button) {
            // TODO
        });
    }

    function createRemoveButton() {
        return createButton("minus", null, function (target, button) {
            // TODO
        });
    }

    function installEditionModeActivation() {
        var tools = document.getElementById("tools");

        tools.appendChild(createListItem(createAddButton()));

        var song = document.querySelector(".song");
        if (song != null) {
            tools.appendChild(createListItem(createRemoveButton()));
            tools.appendChild(createListItem(createEditButton(song)));
        }
    }
    exports.installEditionModeActivation = installEditionModeActivation;

    function search(key, searchQuery) {
        var queryUrl = "";
        if (key !== undefined) {
            queryUrl = "?key=" + key;
        }
        window.location.assign("/search/" + encodeURIComponent(searchQuery) + queryUrl);
        return false;
    }
    exports.search = search;
});
//# sourceMappingURL=songbook.js.map
