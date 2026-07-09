import { createSong, updateSong } from "./api.js";

const host = document.getElementById("song-edit");
const songId = host.dataset.songid;
// Raw songmark text is inlined in the template — read it as text, never as HTML
const text = host.textContent;
host.textContent = "";

const deleteButton = document.getElementById("deleteButton");
if (songId && deleteButton) {
    deleteButton.hidden = false;
}

// Monaco is unusable with touch keyboards and weighs ~4 MB: phones and
// tablets get a plain textarea instead (dynamic import keeps them apart).
const editorModule = matchMedia("(pointer: coarse)").matches
    ? import("./editor/textarea.js")
    : import("./editor/monaco.js");

const editorPromise = editorModule.then(({ createEditor }) => createEditor(host, text));
editorPromise.then((editor) => editor.focus(), (err) => showError(err.message));

document.getElementById("saveButton").addEventListener("click", async (e) => {
    e.preventDefault();
    try {
        const editor = await editorPromise;
        const song = editor.getValue();
        const id = songId ? await updateSong(songId, song) : await createSong(song);
        window.location.pathname = "/songs/" + id;
    } catch (err) {
        showError(friendlyMessage(err));
    }
});

function friendlyMessage(err) {
    switch (err.status) {
        case 401:
            return "You are not signed in as administrator. Open your admin key link, then try again.";
        case 404:
            return "This song no longer exists on the server — it may have been renamed or deleted. Copy your text, then recreate it from “New Song”.";
        case 400:
            return "The server rejected the song: " + plainBody(err);
        default:
            if (err.status) {
                return `Saving failed (error ${err.status}). ${plainBody(err)} Your text is still in the editor — copy it somewhere safe before leaving the page.`;
            }
            return "Saving failed: the server can't be reached. Check your connection and try again — your text is still in the editor.";
    }
}

/* Error bodies can be HTML pages; only surface short plain-text ones. */
function plainBody(err) {
    const body = (err.body || "").trim();
    return !body.startsWith("<") && body.length <= 200 ? body : "";
}

function showError(message) {
    let box = document.getElementById("edit-error");
    if (!box) {
        box = document.createElement("div");
        box.id = "edit-error";
        box.className = "message message-danger";
        box.role = "alert";
        box.style.position = "fixed";
        box.style.top = "var(--space-2)";
        box.style.left = "var(--space-2)";
        box.style.zIndex = "var(--z-modal, 1000)";
        document.body.appendChild(box);
    }
    box.textContent = "Can't save song: " + message;
}
