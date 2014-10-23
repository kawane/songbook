
import utils = require("./utils");

export var PathUrl = "/songs/";

export function create(songUrl: string): SongApi {
    return new SongApi(songUrl);
}

export class SongApi {

    songUrl: string;

    constructor(songUrl: string) {
        this.songUrl = songUrl;
    }

    /**
     * Return the url for this song id.
     *
     * @param id
     * @returns {string}
     */
    url(id: string): string {
        return this.songUrl + id;
    }

    /**
     * Search songs
     *
     * @param id
     * @param onSuccess
     * @param onError
     */
    search(query: string, contentType: string ,onSuccess: (song: string) => void, onError?: (error: string) => void) {
        utils.request({
            method: "GET",
            headers: {"Content-Type": contentType},
            url:  "/search/" + encodeURIComponent(query),
            onSuccess: onSuccess,
            onError: onError
        });
    }

    /**
     * Get a song
     *
     * @param id
     * @param onSuccess
     * @param onError
     */
    get(id: string, onSuccess: (song: string) => void, onError?: (error: string) => void) {
        utils.request({
            method: "GET",
            url:  this.url(id),
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
    create(song: string, key: string, onSuccess: (id: string) => void, onError?: (error: string) => void) {
        utils.request({
            method: "POST",
            url:  this.songUrl + "?key=" + key,
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
    update(id: string, song: string, key: string, onSuccess?: (result) => void, onError?: (error: string) => void) {
        utils.request({
            method: "PUT",
            url: this.url(id) + "?key=" + key,
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
    remove(id: string, key: string, onSuccess?: (result) => void, onError?: (error: string) => void) {
        utils.request( {
            method: "DELETE",
            url: this.url(id) + "?key=" + key,
            onSuccess: onSuccess,
            onError: onError
        });
    }
}

