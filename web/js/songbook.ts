
function createListItem(inner:Node):HTMLElement {
    var li = document.createElement("li");
    if (inner != null) li.appendChild(inner);
    return li;
}


function createButton(glyph: string, target:HTMLElement, action: (target:HTMLElement, button: HTMLElement) => any) {
    var button = document.createElement("a");
    var buttonGlyph = document.createElement("span");
    buttonGlyph.classList.add("glyphicon");
    buttonGlyph.classList.add("glyphicon-"+glyph);
    button.appendChild(buttonGlyph);
    button.addEventListener("click", ()=>{action(target, button)});
    button.id = glyph;
    return button;
}

function updateGlyph(button: HTMLElement, glyph: string) {
    var buttonGlyph = <HTMLElement>button.querySelector("span.glyphicon");
    if (buttonGlyph!=null) {
        buttonGlyph.className = "glyphicon glyphicon-"+glyph;
    }
}


/**
 * Creates a alert.
 * @param message message to show
 * @param type alert type: success, info, warning, danger.
 */
export function createAlert(message: any, type: string, dismissible: boolean = true) {
    var alertDiv = document.createElement("div");
    alertDiv.classList.add("alert");
    alertDiv.classList.add("alert-" + type);
    if (dismissible) alertDiv.classList.add("alert-dismissible");
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
    } else {
        // message is a string
        var text = document.createElement("span");
        text.innerText = message;
        alertDiv.appendChild(text);
    }

    var content = <HTMLElement>document.querySelector("#content");
    content.parentElement.insertBefore(alertDiv, content);
}

/**
 * Put current song to the server.
 * @param result handler when put is done.
 */
function putSong(result:(event: Event) => any) {
    var song = <HTMLElement>document.querySelector(".song");
    var title = <HTMLElement>song.querySelector(".song-title");
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
function deleteSong(result:(event: Event) => any) {
    var request = new XMLHttpRequest();
    request.open("delete", window.location.search, true);
    request.onreadystatechange = result;
    request.send();
}

function createEditButton(song: HTMLElement): HTMLElement {
    return createButton("pencil", song, switchEdition);
}

function createAddButton(): Node {
    return createButton("plus", null, (target, button) => {
        window.location.pathname = "/new";
    });
}

function createRemoveButton(): Node {
    return createButton("minus", null, (target, button) => {
        deleteSong(event => {
            window.location.reload();
        });
    });
}

function switchEdition(target: HTMLElement, button: HTMLElement) {
    var edited = target.attributes["edited"];
    if (edited === undefined || edited === true) {
        target.attributes["edited"] = false;
        target.classList.add("edited");

        updateGlyph(button, "send");

        var title = <HTMLElement>target.querySelector(".song-title");
        title.contentEditable = "true";

        var authors = target.querySelectorAll(".song-author");
        for (var index in authors) {
            var author = <HTMLElement>authors[index];
            author.contentEditable = "true";
        }

        var verse = <HTMLElement>target.querySelector(".song-verse");
        verse.contentEditable = "true";

    } else {
        target.attributes["edited"] = true;
        target.classList.remove("edited");

        updateGlyph(button, "refresh");

        var title = <HTMLElement>target.querySelector(".song-title");
        title.contentEditable = "false";

        var authors = target.querySelectorAll(".song-author");
        for (var index in authors) {
            var author = <HTMLElement>authors[index];
            author.contentEditable = "false";
        }

        var verse = <HTMLElement>target.querySelector(".song-verse");
        verse.contentEditable = "false";

        putSong(event => {
            var request = <XMLHttpRequest>event.currentTarget;
            if (request.readyState == 4) {
                if (request.status == 200) {
                    var oldPathname = decodeURI(window.location.pathname)
                    var pathname = "/songs/" + request.response;
                    if (oldPathname !== pathname) {
                        window.location.pathname = pathname;
                    }
                    updateGlyph(button, "pencil");
                } else {
                    updateGlyph(button, "warning_sign");
                }
            }
        })
    }
}

export function installEditionMode(activate: boolean) {
    var tools = <HTMLElement><any>document.getElementById("tools");

    tools.appendChild(createListItem(createAddButton()));

    var song = <HTMLElement><any>document.querySelector(".song");
    if (song!=null) {
        tools.appendChild(createListItem(createRemoveButton()));
        var editButton = createEditButton(song);
        tools.appendChild(createListItem(editButton));

        if (activate) switchEdition(song, editButton);
    }
}

export function search(query: string) {
    window.location.pathname = "/search/" + encodeURIComponent(query);
    return false;
}

