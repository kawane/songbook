/*
 * Songbook REST API client. The server negotiates content with the Accept
 * header (text/song = raw songmark, text/plain = ids, text/html = rendered),
 * so every call sets it explicitly — fetch would send * / * otherwise and the
 * server would pick text/song for pages expecting html.
 * The SessionKey cookie rides along automatically (same origin).
 */

async function request(url, { method = "GET", accept = "text/plain", body } = {}) {
    const response = await fetch(url, {
        method,
        headers: { "Accept": accept },
        body,
    });
    const text = await response.text();
    if (!response.ok) {
        throw new Error(text || `${response.status} ${response.statusText}`);
    }
    return text;
}

export function searchSongs(query, accept) {
    const path = query ? `/search/${encodeURIComponent(query)}` : "/search/";
    return request(path, { accept });
}

export function getSong(id, accept = "text/song") {
    return request(`/songs/${encodeURIComponent(id)}`, { accept });
}

export function createSong(song) {
    return request("/songs/", { method: "POST", body: song });
}

export function updateSong(id, song) {
    return request(`/songs/${encodeURIComponent(id)}`, { method: "PUT", body: song });
}

export function deleteSong(id) {
    return request(`/songs/${encodeURIComponent(id)}`, { method: "DELETE" });
}
