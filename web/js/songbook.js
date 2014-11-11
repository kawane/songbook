define(["require", "exports"], function (require, exports) {
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
        button.id = glyph;
        return button;
    }
    function updateGlyph(button, glyph) {
        var buttonGlyph = button.querySelector("span.glyphicon");
        if (buttonGlyph != null) {
            buttonGlyph.className = "glyphicon glyphicon-" + glyph;
        }
    }
    /**
     * Creates a alert.
     * @param message message to show
     * @param type alert type: success, info, warning, danger.
     */
    function createAlert(message, type, dismissible) {
        if (dismissible === void 0) { dismissible = true; }
        var alertDiv = document.createElement("div");
        alertDiv.classList.add("alert");
        alertDiv.classList.add("alert-" + type);
        if (dismissible)
            alertDiv.classList.add("alert-dismissible");
        alertDiv.setAttribute("role", "alert");
        if (dismissible) {
            // adds button to close the alert
            var button = document.createElement("button");
            button.className = "close";
            button.setAttribute("data-dismiss", "alert");
            var span1 = document.createElement("span");
            span1.setAttribute("aria-hidden", "true");
            span1.innerHTML = "&times;";
            button.appendChild(span1);
            var span2 = document.createElement("span");
            span2.className = "sr-only";
            span2.innerText = "Close";
            button.appendChild(span2);
            alertDiv.appendChild(button);
        }
        if (message.querySelector) {
            // message is a HTMLElement
            alertDiv.appendChild(message);
        }
        else {
            // message is a string
            var text = document.createElement("span");
            text.innerText = message;
            alertDiv.appendChild(text);
        }
        var content = document.querySelector("#content");
        content.parentElement.insertBefore(alertDiv, content);
    }
    exports.createAlert = createAlert;
    /**
     * Put current song to the server.
     * @param result handler when put is done.
     */
    function putSong(result) {
        var song = document.querySelector(".song");
        var title = song.querySelector(".song-title");
        var id = encodeURIComponent(title.innerText);
        var request = new XMLHttpRequest();
        request.open("put", "/songs/" + id + window.location.search, true);
        request.onreadystatechange = result;
        request.send("<div class=\"song\">" + song.innerHTML + "</div>");
    }
    /**
     * Delete current song on the server.
     * @param result handler when delete is done.
     */
    function deleteSong(result) {
        var request = new XMLHttpRequest();
        request.open("delete", window.location.search, true);
        request.onreadystatechange = result;
        request.send();
    }
    function createEditButton(song) {
        return createButton("pencil", song, switchEdition);
    }
    function createAddButton() {
        return createButton("plus", null, function (target, button) {
            window.location.pathname = "/new";
        });
    }
    function createRemoveButton() {
        return createButton("minus", null, function (target, button) {
            deleteSong(function (event) {
                window.location.reload();
            });
        });
    }
    function switchEdition(target, button) {
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
        }
        else {
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
            putSong(function (event) {
                var request = event.currentTarget;
                if (request.readyState == 4) {
                    if (request.status == 200) {
                        var oldPathname = decodeURI(window.location.pathname);
                        var pathname = "/songs/" + request.response;
                        if (oldPathname !== pathname) {
                            window.location.pathname = pathname;
                        }
                        updateGlyph(button, "pencil");
                    }
                    else {
                        updateGlyph(button, "warning_sign");
                    }
                }
            });
        }
    }
    function installEditionMode(activate) {
        var tools = document.getElementById("tools");
        tools.appendChild(createListItem(createAddButton()));
        var song = document.querySelector(".song");
        if (song != null) {
            tools.appendChild(createListItem(createRemoveButton()));
            var editButton = createEditButton(song);
            tools.appendChild(createListItem(editButton));
            if (activate)
                switchEdition(song, editButton);
        }
    }
    exports.installEditionMode = installEditionMode;
    function search(query) {
        window.location.pathname = "/search/" + encodeURIComponent(query);
        return false;
    }
    exports.search = search;
});
//# sourceMappingURL=songbook.js.map