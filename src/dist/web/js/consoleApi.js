import { searchSongs, getSong, createSong, updateSong, deleteSong } from "./api.js";

const result = document.getElementById("result");
const forms = document.querySelectorAll(".action-form");

// Hash-based tabs: show the .action-form matching location.hash
function showTab() {
    const active = (window.location.hash || "#searchApi").substring(1);
    for (const form of forms) {
        form.hidden = form.id !== active;
    }
}
window.addEventListener("hashchange", showTab);
showTab();

function value(id) {
    return document.getElementById(id).value;
}

function display(promise) {
    promise.then(
        (text) => { result.textContent = text; },
        (err) => { result.textContent = "Error: " + err.message; },
    );
}

const actions = {
    searchApi: () => display(searchSongs(value("search-query"), value("search-content-type"))),
    get: () => display(getSong(value("get-id"), value("get-content-type"))),
    create: () => display(createSong(value("create-songdata"))),
    update: () => display(updateSong(value("update-id"), value("update-songdata"))),
    delete: () => display(deleteSong(value("delete-id"))),
};

for (const form of forms) {
    form.querySelector("form").addEventListener("submit", (e) => {
        e.preventDefault();
        actions[form.id]();
    });
}
