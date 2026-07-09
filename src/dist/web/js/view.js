const song = document.getElementById("song-view");

// --- Auto-hiding toolbar (video-player pattern): shown on load so it stays
// discoverable, fades out after a few seconds, and a tap on the song brings
// it back. While playing, the lyrics get the whole screen. ---

const toolbar = document.getElementById("toolbar");
const HIDE_DELAY = 5000;
let hideTimer;

function showToolbar() {
    toolbar.classList.remove("song-toolbar-hidden");
    armHideTimer();
}

function hideToolbar() {
    clearTimeout(hideTimer);
    toolbar.classList.add("song-toolbar-hidden");
}

function armHideTimer() {
    clearTimeout(hideTimer);
    hideTimer = setTimeout(hideToolbar, HIDE_DELAY);
}

song.addEventListener("click", (e) => {
    if (e.target.closest("a, button")) return;
    if (toolbar.classList.contains("song-toolbar-hidden")) {
        showToolbar();
    } else {
        hideToolbar();
    }
});
// Keep the toolbar up while it is being used (e.g. repeated transpositions)
toolbar.addEventListener("pointerdown", armHideTimer);
// Explicit hide button: on desktop, clicking the song is a less natural reflex
document.getElementById("hideToolbarButton").addEventListener("click", hideToolbar);
showToolbar();

// --- Font size ---

let fontSize = 100;
document.getElementById("biggerButton").addEventListener("click", () => {
    fontSize += 10;
    song.style.fontSize = fontSize + "%";
});
document.getElementById("smallerButton").addEventListener("click", () => {
    fontSize -= 10;
    song.style.fontSize = fontSize + "%";
});

// --- Full screen ---

const fullScreenButton = document.getElementById("fullScreenButton");
const fullscreenEnabled = document.fullscreenEnabled || document.webkitFullscreenEnabled;
if (!fullscreenEnabled) {
    // iPhone Safari has no fullscreen API
    fullScreenButton.hidden = true;
} else {
    fullScreenButton.addEventListener("click", () => {
        if (document.fullscreenElement || document.webkitFullscreenElement) {
            (document.exitFullscreen || document.webkitExitFullscreen).call(document);
        } else {
            const el = document.body;
            (el.requestFullscreen || el.webkitRequestFullscreen).call(el);
        }
    });
    const fullscreenChange = () => {
        const active = Boolean(document.fullscreenElement || document.webkitFullscreenElement);
        fullScreenButton.classList.toggle("active", active);
    };
    document.addEventListener("fullscreenchange", fullscreenChange);
    document.addEventListener("webkitfullscreenchange", fullscreenChange);
}

// --- Chords visibility (screen display and printing without chords) ---

const chordsButton = document.getElementById("chordsButton");
chordsButton.addEventListener("click", () => {
    const hidden = song.classList.toggle("song-hide-chords");
    chordsButton.classList.toggle("active", !hidden);
});

// --- Two-column layout on wide screens ---

const songWidth = song.clientWidth;
const songHeight = song.clientHeight;
const updateColumn = () => {
    const needColumn = songWidth < window.innerWidth / 2 && songHeight > window.innerHeight;
    song.querySelector(".song-content").classList.toggle("song-column", needColumn);
};
window.addEventListener("resize", updateColumn);
updateColumn();

// --- Transposition ---

const notesIndexes = {
    "C": 0, "C#": 1, "Db": 1, "D": 2, "D#": 3, "Eb": 3, "E": 4, "F": 5,
    "F#": 6, "Gb": 6, "G": 7, "G#": 8, "Ab": 8, "A": 9, "A#": 10, "Bb": 10, "B": 11,
};
const notes = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];

function getNoteIndex(i) {
    return ((i % 12) + 12) % 12;
}

function transpose(chord, demiToneCount) {
    let note = chord[0];
    if (chord.length > 1 && (chord[1] === "b" || chord[1] === "#")) {
        note += chord[1];
    }
    const newNote = notes[getNoteIndex(notesIndexes[note] + demiToneCount)];
    const indexOfBass = chord.indexOf("/");
    if (indexOfBass !== -1 && indexOfBass < chord.length - 1) {
        chord = chord.substring(0, indexOfBass + 1) + transpose(chord.substring(indexOfBass + 1), demiToneCount);
    }
    return newNote + chord.substring(note.length);
}

let transposeCount = 0;
const transposeDisplay = document.getElementById("transposeDisplay");
const musicalKeyElt = song.querySelector(".song-metadata-value[itemprop=musicalKey]");
const musicalKey = musicalKeyElt ? musicalKeyElt.textContent : null;
transposeDisplay.textContent = musicalKey ?? "0";

function transposeAll(count) {
    transposeCount += count;
    for (const chordElt of song.querySelectorAll(".song-chord")) {
        chordElt.textContent = transpose(chordElt.textContent, count);
    }
    transposeDisplay.textContent = musicalKey
        ? transpose(musicalKey, transposeCount)
        : String(transposeCount);
}

document.getElementById("transposeLessButton").addEventListener("click", () => transposeAll(-1));
document.getElementById("transposeMoreButton").addEventListener("click", () => transposeAll(1));
