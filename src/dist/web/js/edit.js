import { createSong, updateSong, deleteSong } from "./api.js";
import { hideErrorMessage, showErrorMessage } from "./dialog.js";
import { confirmDialog } from "./vendor/mesnos-dialog.js";

const host = document.getElementById("song-edit");
const songId = host.dataset.songid;
// Raw songmark text is inlined in the template — read it as text, never as HTML
const text = host.textContent;
host.textContent = "";

const deleteButton = document.getElementById("deleteButton");
if (songId && deleteButton) {
    deleteButton.hidden = false;
    deleteButton.addEventListener("click", async (e) => {
        e.preventDefault();
        const confirmed = await confirmDialog({
            title: "Delete song?",
            message: "This song will be deleted. This cannot be undone.",
            confirmLabel: "Delete",
            danger: true,
        });
        if (!confirmed) return;
        try {
            await withSession(() => deleteSong(songId));
            window.location.href = "/";
        } catch (err) {
            showError(err);
        }
    });
}

// Monaco is unusable with touch keyboards and weighs ~4 MB: phones and
// tablets get a plain textarea instead (dynamic import keeps them apart).
const editorModule = matchMedia("(pointer: coarse)").matches
    ? import("./editor/textarea.js")
    : import("./editor/monaco.js");

const editorPromise = editorModule.then(({ createEditor }) => createEditor(host, text));
editorPromise.then((editor) => editor.focus(), (err) => showErrorMessage(err.message));

document.getElementById("saveButton").addEventListener("click", async (e) => {
    e.preventDefault();
    try {
        const editor = await editorPromise;
        const song = editor.getValue();
        const id = await withSession(() => songId ? updateSong(songId, song) : createSong(song));
        window.location.pathname = "/songs/" + id;
    } catch (err) {
        showError(err);
    }
});

// Behind the shared login, the users session keeper (loaded by the page,
// banner off) says when the session is lost and when it comes back: warn
// before a save fails, and clear the warning once signed in again.
window.addEventListener("mesnos-session:expired", () => showError({ status: 401, expired: true }));
window.addEventListener("mesnos-session:restored", hideErrorMessage);

// The access token of the shared login lasts 15 minutes. When a write is
// refused, renew the session once through the users session keeper (loaded
// on this page when songbook runs behind the shared login), then try again.
// The refusal comes from Caddy before songbook: nothing was written.
async function withSession(write) {
    try {
        return await write();
    } catch (err) {
        if ((err.status === 401 || err.status === 403) && await window.mesnosSession?.refresh()) {
            return write();
        }
        throw err;
    }
}

function friendlyMessage(err) {
    if (err.expired) {
        return "Your session has expired. Sign in again in a new tab: this page picks the session up by itself, and your text stays in the editor.";
    }
    switch (err.status) {
        case 401:
        case 403:
            return "You are not signed in as administrator anymore (the session has expired, or this account is not an administrator). Sign in again in a new tab, then try again: your text is still in the editor.";
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

function showError(err) {
    const signIn = err.status === 401 || err.status === 403
        ? { href: "/signin", label: "Sign in (new tab)" }
        : undefined;
    showErrorMessage(friendlyMessage(err), signIn);
}
