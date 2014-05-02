
var songbook = (function() {
	var exports = {};

	exports.convert = function(source, callback, errorCallback) {
		var req = new XMLHttpRequest();
		req.open('POST', '/service/convert', true);
		req.setRequestHeader("Content-Type", "text/chordpro");
		req.onreadystatechange = function() {
			if (req.readyState == 4) {
				if (req.status == 200) {
					callback(req.responseText);
				} else {
					errorCallback(req);
				}
			}
		};
		req.send(source);
	};

	exports.createSong = function(source, callback, errorCallback) {
		var req = new XMLHttpRequest();
		req.open('POST', '/rest/song/', true);
		req.setRequestHeader("Content-Type", "text/chordpro");
		req.onreadystatechange = function() {
			if (req.readyState == 4) {
				if (req.status == 200) {
					callback(req.responseText);
				} else {
					errorCallback(req);
				}
			}
		};
		req.send(source);
	};

	exports.modifySong = function(songId, source, callback, errorCallback) {
		var req = new XMLHttpRequest();
		req.open('PUT', '/rest/song/'+songId, true);
		req.setRequestHeader("Content-Type", "text/chordpro");
		req.onreadystatechange = function() {
			if (req.readyState == 4) {
				if (req.status == 200) {
					callback(req.responseText);
				} else {
					errorCallback(req);
				}
			}
		};
		req.send(source);
	};

	return exports;
}());
