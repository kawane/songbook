/*
 * Songbook REST API client. The server negotiates content with the Accept
 * header (text/song = raw songmark, text/plain = ids, text/html = rendered),
 * so every call sets it explicitly — fetch would send * / * otherwise and the
 * server would pick text/song for pages expecting html.
 * The SessionKey cookie rides along automatically (same origin).
 *
 * Song ids come URL-encoded from the server ('+' stands for a space, as in
 * "les+innocents-l+autre+finistere") and must be used verbatim in paths:
 * re-encoding them turns '+' into '%2B' and the server finds nothing.
 */

export class ApiError extends Error {
    constructor(status, statusText, body) {
        super(body || statusText);
        this.status = status;
        this.body = body;
    }
}

async function request(url, { method = "GET", accept = "text/plain", body } = {}) {
    const response = await fetch(url, {
        method,
        headers: { "Accept": accept },
        body,
    });
    const text = await response.text();
    if (!response.ok) {
        throw new ApiError(response.status, response.statusText, text);
    }
    return text;
}

export function searchSongs(query, accept) {
    const path = query ? `/search/${encodeURIComponent(query)}` : "/search/";
    return request(path, { accept });
}

export function getSong(id, accept = "text/song") {
    return request(`/songs/${id}`, { accept });
}

export function createSong(song) {
    return request("/songs/", { method: "POST", body: song });
}

export function updateSong(id, song) {
    return request(`/songs/${id}`, { method: "PUT", body: song });
}

export function deleteSong(id) {
    return request(`/songs/${id}`, { method: "DELETE" });
}
