
module songbook {

    function createListItem(inner:Node):HTMLElement {
        var li = document.createElement("li");
        if (inner != null) li.appendChild(inner);
        return li;
    }

    function createButton(glyph: String, target:HTMLElement, action: (target:HTMLElement, button: HTMLElement) => any) {
        var button = document.createElement("a");
        var buttonGlyph = document.createElement("span");
        buttonGlyph.classList.add("glyphicon");
        buttonGlyph.classList.add("glyphicon-"+glyph);
        button.appendChild(buttonGlyph);
        button.addEventListener("click", ()=>{action(target, button)});
        return button;
    }

    function updateGlyph(button: HTMLElement, glyph: string) {
        var buttonGlyph = <HTMLElement>button.querySelector("span.glyphicon");
        if (buttonGlyph!=null) {
            buttonGlyph.className = "glyphicon glyphicon-"+glyph;
        }
    }

    function postSong(result:(event: Event) => any) {
        // retrieves id from location
        var pathname = document.location.pathname;
        var id = pathname.substring(pathname.lastIndexOf('/')+1);

        // retrieves key from location
        // TODO handle undefined key
        var search = document.location.search;
        var keyStart = search.indexOf("key=");
        var keyEnd = search.indexOf("&");
        var key = search.substring(keyStart+4, keyEnd >= 0 ? keyEnd : search.length);

        var request = new XMLHttpRequest();
        request.open("put", "/songs/"+ id +"?key="+ key, true);

        request.onreadystatechange = result;

        var song = <HTMLElement>document.querySelector(".song");
        request.send("<div class=\"song\">" + song.innerHTML + "</div>");
    }

    function createEditButton(song: HTMLElement): Node {
        return createButton("pencil", song, (target, button) => {
            if (target.contentEditable !== "true") {
                target.contentEditable = "true";
                target.classList.add("edited");
                updateGlyph(button, "send");

            } else {
                target.contentEditable = "false";
                target.classList.remove("edited");
                updateGlyph(button, "refresh");
                postSong(event => {
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
        });
    }

    function createAddButton(): Node {
        return createButton("plus", null, (target, button) => {
            // TODO
        });
    }

    export function installEditionModeActivation() {
        var tools = <HTMLElement><any>document.getElementById("tools");

        tools.appendChild(createListItem(createAddButton()));

        var song = <HTMLElement><any>document.querySelector(".song");
        if (song!=null) tools.appendChild(createListItem(createEditButton(song)));
    }

    export function search(key: string, query: string) {
        window.location.assign("/search/" + query + "?key=" + key);
        return false;
    }
}
