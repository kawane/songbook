
import utils = require("./utils");

var songUrl = "/songs/";

/**
 * Return the url for this song id.
 *
 * @param id
 * @returns {string}
 */
export function url(id: string): string {
    return songUrl + id;
}

/**
 * Get a song
 *
 * @param id
 * @param onSuccess
 * @param onError
 */
export function get(id: string, onSuccess: (song: string) => void, onError?: (error: string) => void) {
    utils.request({
        method: "GET",
        url:  url(id),
        onSuccess: onSuccess,
        onError: onError
    });
}

/**
 * Create a song
 *
 * @param {string} song
 * @param {string} key
 * @param onSuccess
 * @param onError
 */
export function create(song: string, key: string, onSuccess: (id: string) => void, onError?: (error: string) => void) {
    utils.request({
        method: "POST",
        url:  songUrl + "?key=" + key,
        data: song,
        onSuccess: onSuccess,
        onError: onError
    });
}

/**
 *  Modify a Song
 *
 * @param {string} id
 * @param {string} song
 * @param {string} key
 * @param onSuccess
 * @param onError
 */
export function modify(id: string, song: string, key: string, onSuccess?: () => void, onError?: (error: string) => void) {
    utils.request( {
        method: "PUT",
        url: url(id) + "?key=" + key,
        data: song,
        onSuccess: onSuccess,
        onError: onError
    });
}

/**
 * Remove a song
 *
 * @param {string} id
 * @param {string} key
 * @param onSuccess
 * @param onError
 */
export function remove(id: string, key: string, onSuccess?: () => void, onError?: (error: string) => void) {
    utils.request( {
        method: "DELETE",
        url: url(id) + "?key=" + key,
        onSuccess: onSuccess,
        onError: onError
    });
}
