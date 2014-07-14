
module songbook {

    function createButton(glyph: String, target:HTMLElement, action: (target:HTMLElement, button: HTMLElement) => any) {
        var button = document.createElement("button");
        button.className = "btn btn-default";
        button.type = "button";
        var buttonGlyph = document.createElement("span");
        buttonGlyph.className = "glyphicon glyphicon-"+glyph;
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

    function createAdministrationTools(song: HTMLElement): Node {
        var toolbar = document.createElement("div");
        toolbar.className = "";
        var editButton = createButton("pencil", song, (target, button) => {
            // TODO change pencil and save button states
            if (target.contentEditable !== "true") {
                target.contentEditable = "true";
                updateGlyph(button, "send");
            } else {
                target.contentEditable = "false";
                updateGlyph(button, "pencil");
            }
        });
        toolbar.appendChild(editButton);

        return toolbar;
    }

    export function installEditionModeActivation() {
        var allSongs = <HTMLElement[]><any>document.querySelectorAll(".song");
        for (var song in  allSongs) {
            var songNode = allSongs[song];
            var parentNode = songNode.parentElement;
            parentNode.insertBefore(createAdministrationTools(songNode), songNode);
        }
    }

    export function search(key: string, query: string) {
        window.location.assign("/search/" + query + "?key=" + key);
        return false;
    }
}
