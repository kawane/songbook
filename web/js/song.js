define(["require", "exports", "./utils"], function (require, exports, utils) {
    var songUrl = "/songs/";
    /**
     * Return the url for this song id.
     *
     * @param id
     * @returns {string}
     */
    function url(id) {
        return songUrl + id;
    }
    exports.url = url;
    /**
     * Get a song
     *
     * @param id
     * @param onSuccess
     * @param onError
     */
    function get(id, onSuccess, onError) {
        utils.request({
            method: "GET",
            url: url(id),
            onSuccess: onSuccess,
            onError: onError
        });
    }
    exports.get = get;
    /**
     * Create a song
     *
     * @param {string} song
     * @param {string} key
     * @param onSuccess
     * @param onError
     */
    function create(song, key, onSuccess, onError) {
        utils.request({
            method: "POST",
            url: songUrl + "?key=" + key,
            data: song,
            onSuccess: onSuccess,
            onError: onError
        });
    }
    exports.create = create;
    /**
     *  Modify a Song
     *
     * @param {string} id
     * @param {string} song
     * @param {string} key
     * @param onSuccess
     * @param onError
     */
    function modify(id, song, key, onSuccess, onError) {
        utils.request({
            method: "PUT",
            url: url(id) + "?key=" + key,
            data: song,
            onSuccess: onSuccess,
            onError: onError
        });
    }
    exports.modify = modify;
    /**
     * Remove a song
     *
     * @param {string} id
     * @param {string} key
     * @param onSuccess
     * @param onError
     */
    function remove(id, key, onSuccess, onError) {
        utils.request({
            method: "DELETE",
            url: url(id) + "?key=" + key,
            onSuccess: onSuccess,
            onError: onError
        });
    }
    exports.remove = remove;
});
//# sourceMappingURL=song.js.map