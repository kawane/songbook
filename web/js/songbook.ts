
module songbook {

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

    function putSong(result:(event: Event) => any) {
        // retrieves id from song title
        var song = <HTMLElement>document.querySelector(".song");
        var title = <HTMLElement>song.querySelector(".song-title");
        var id = title.innerText;

        var request = new XMLHttpRequest();
        request.open("put", "/songs/"+ id +"?key="+ getKey(), true);

        request.onreadystatechange = result;

        request.send("<div class=\"song\">" + song.innerHTML + "</div>");
    }

    function getKey() {
        // retrieves key from location
        // TODO handle undefined key
        var search = document.location.search;
        var keyStart = search.indexOf("key=");
        var keyEnd = search.indexOf("&");
        var key = search.substring(keyStart + 4, keyEnd >= 0 ? keyEnd : search.length);
        return key;
    }

    function createEditButton(song: HTMLElement): HTMLElement {
        return createButton("pencil", song, switchEdition);
    }

    function switchEdition(target:HTMLElement, button: HTMLElement) {
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
                        updateGlyph(button, "pencil");
                    } else {
                        updateGlyph(button, "warning_sign");
                    }
                }
            })
        }
    }

    function createAddButton(): Node {
        return createButton("plus", null, (target, button) => {
            window.location.assign("/new?key=" + getKey());
        });
    }

    function createRemoveButton(): Node {
        return createButton("minus", null, (target, button) => {
            // TODO
        });
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

    export function search(key: string, query: string) {
        window.location.assign("/search/" + query + "?key=" + key);
        return false;
    }
}
