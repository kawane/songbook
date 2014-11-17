
var forEach = function (list: NodeList, callback: (node: Node)=>void, context?: any){
    return Array.prototype.forEach.call(list, callback, context);
};

function keyboardListener(e: KeyboardEvent) {
    if (e.keyCode === 13) {
        var selection = window.getSelection();

        if (selection.anchorNode.parentNode.nodeType == Node.ELEMENT_NODE) {
            var parentElement = <HTMLElement>selection.anchorNode.parentNode;
            if(parentElement.classList.contains("song-line")) {
                var songChords = document.createElement("div");
                songChords.className = "song-chords";
                songChords.innerHTML = " ";

                var songLine = document.createElement("div");
                songLine.className = "song-line";
                songLine.innerHTML = " ";
                parentElement.parentElement.insertBefore(songLine, parentElement.nextSibling);
                parentElement.parentElement.insertBefore(songChords, parentElement.nextSibling);
                e.preventDefault();
            } else if (parentElement.classList.contains("song-line")) {
                var songChords = document.createElement("div");
                songChords.className = "song-chords";
                songChords.innerHTML = " ";

                parentElement.parentElement.insertBefore(songChords, parentElement.nextSibling);
                e.preventDefault();
            }

        }
    }
}

export function startEdition(song: HTMLElement) {
    var title = <HTMLElement>song.querySelector(".song-title");
    title.contentEditable = "true";

    song.removeEventListener("keypress", keyboardListener);
    song.addEventListener("keypress", keyboardListener);

    forEach(song.querySelectorAll(".song-author"), (author: HTMLElement) => {
        author.contentEditable = "true";
    });

    forEach(song.querySelectorAll(".song-verse"), (author: HTMLElement) => {
        author.contentEditable = "true";
    });

}


export function endEdition(song: HTMLElement) {
    song.removeEventListener("keypress", keyboardListener);
    forEach(song.querySelectorAll("*[contenteditable='true']"), (elt: HTMLElement) => {
        elt.contentEditable = "false";
    });
}