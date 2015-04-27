var song = document.getElementById("song");
var fontSize = 100;
var biggerButton = document.getElementById("biggerButton");
biggerButton.addEventListener("click", function (e) {
    fontSize += 10;
    song.style.fontSize = fontSize + "%";
});
var smallerButton = document.getElementById("smallerButton");
smallerButton.addEventListener("click", function (e) {
    fontSize -= 10;
    song.style.fontSize = fontSize + "%";
});
// FullScreen
var fullScreenButton = document.getElementById("fullScreenButton");
fullScreenButton.addEventListener("click", function (e) {
    if (isFullScreen()) {
        exitFullScreen();
    }
    else {
        requestFullScreen(document.body);
    }
});
var fullscreenChange = function () {
    if (isFullScreen()) {
        fullScreenButton.firstElementChild.classList.add("active");
    }
    else {
        fullScreenButton.firstElementChild.classList.remove("active");
    }
};
document.addEventListener("fullscreenchange ", fullscreenChange);
document.addEventListener("webkitfullscreenchange", fullscreenChange);
document.addEventListener("mozfullscreenchange", fullscreenChange);
document.addEventListener("MSFullscreenChange", fullscreenChange);
function isFullScreen() {
    if (document["isFullScreen"]) {
        return document["isFullScreen"];
    }
    else if (document["webkitIsFullScreen"]) {
        return document["webkitIsFullScreen"];
    }
    else if (document["mozFullScreen"]) {
        return document["mozFullScreen"];
    }
}
function exitFullScreen() {
    if (document["exitFullscreen"]) {
        return document["exitFullscreen"]();
    }
    else if (document["webkitExitFullscreen"]) {
        return document["webkitExitFullscreen"]();
    }
    else if (document["mozCancelFullScreen"]) {
        return document["mozCancelFullScreen"]();
    }
}
function requestFullScreen(element) {
    if (element["requestFullScreen"]) {
        element["requestFullScreen"]();
    }
    else if (element["webkitRequestFullScreen"]) {
        element["webkitRequestFullScreen"]();
    }
    else if (element["mozRequestFullScreen"]) {
        element["mozRequestFullScreen"]();
    }
}
// Restore two column current user pref
var songWidth = song.clientWidth;
var songHeight = song.clientHeight;
var updateColumn = function () {
    var needColumn = songWidth < window.innerWidth / 2;
    needColumn = needColumn && songHeight > window.innerHeight;
    if (needColumn) {
        song.classList.add("song-column");
    }
    else {
        song.classList.remove("song-column");
    }
};
window.addEventListener("resize", updateColumn);
updateColumn();
// Transposition
var transposeCount = 0;
var musicalKey = null;
var transposeDisplay = document.getElementById("transposeDisplay");
var musicalKeyElt = song.querySelector(".song-metadata-value[itemprop=musicalKey]");
if (musicalKeyElt) {
    musicalKey = musicalKeyElt.textContent;
    transposeDisplay.innerHTML = musicalKey;
}
else {
    transposeDisplay.innerHTML = "0";
}
var forEachNode = function (list, callback, context) {
    return Array.prototype.forEach.call(list, callback, context);
};
var transposeLessButton = document.getElementById("transposeLessButton");
transposeLessButton.addEventListener("click", function (e) {
    transposeAll(-1);
});
var transposeMoreButton = document.getElementById("transposeMoreButton");
transposeMoreButton.addEventListener("click", function (e) {
    transposeAll(1);
});
function transposeAll(count) {
    transposeCount += count;
    forEachNode(song.querySelectorAll(".song-chord"), function (chordElt) {
        chordElt.textContent = transpose(chordElt.textContent, count);
    });
    if (musicalKey) {
        transposeDisplay.innerHTML = transpose(musicalKey, transposeCount);
    }
    else {
        transposeDisplay.innerHTML = transposeCount + "";
    }
}
var notesIndexes = {
    "C": 0,
    "C#": 1,
    "Db": 1,
    "D": 2,
    "D#": 3,
    "Eb": 3,
    "E": 4,
    "F": 5,
    "F#": 6,
    "Gb": 6,
    "G": 7,
    "G#": 8,
    "Ab": 8,
    "A": 9,
    "A#": 10,
    "Bb": 10,
    "B": 11
};
var notes = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
function transpose(chord, demiToneCount) {
    var note = chord[0];
    if (chord.length > 1 && (chord[1] === "b" || chord[1] === "#")) {
        note += chord[1];
    }
    var newNote = notes[getNoteIndex(notesIndexes[note] + demiToneCount)];
    var indexOfBass = chord.indexOf("/");
    if (indexOfBass !== -1 && indexOfBass < chord.length - 1) {
        chord = chord.substring(0, indexOfBass + 1) + transpose(chord.substring(indexOfBass + 1), demiToneCount);
    }
    return newNote + chord.substring(note.length);
}
function getNoteIndex(i) {
    if (i < 0) {
        return getNoteIndex(12 + i);
    }
    if (i >= 12) {
        return getNoteIndex(i - 12);
    }
    return i;
}
//# sourceMappingURL=view.js.map