/// <amd-dependency path="ace" />
/// 
define(["require", "exports", "./SongApi", "ace"], function (require, exports, SongApi) {
    var updateSongEditor = ace.edit("update-songdata");
    updateSongEditor.getSession().setMode("ace/mode/song");
    updateSongEditor.renderer.setShowGutter(false);
    var createSongEditor = ace.edit("create-songdata");
    createSongEditor.getSession().setMode("ace/mode/song");
    createSongEditor.renderer.setShowGutter(false);
    var resultEditor = ace.edit("result");
    resultEditor.getSession().setMode("ace/mode/song");
    resultEditor.renderer.setShowGutter(false);
    document.querySelector("#server-url").value = window.location.origin + "/songs/";
    function getAction() {
        if (window.location.hash) {
            return window.location.hash.substring(1);
        }
        return "searchApi";
    }
    var forEach = Array.prototype.forEach;
    function highlightMenu(serviceName) {
        var activeElements = document.querySelectorAll("#services>li.active");
        forEach.call(activeElements, function (element) {
            element.classList.remove("active");
        });
        var element = document.querySelector("#services>li>a[href='#" + serviceName + "']");
        if (element) {
            element.parentElement.classList.add("active");
        }
    }
    function showForm(serviceName) {
        var forms = document.querySelectorAll(".action-form");
        forEach.call(forms, function (form) {
            form.classList.add("hidden");
        });
        var element = document.querySelector("#" + serviceName);
        if (element) {
            element.classList.remove("hidden");
        }
        createSongEditor.resize();
        updateSongEditor.resize();
        resultEditor.resize();
    }
    function setResult(content, mode) {
        resultEditor.setValue(content, -1);
        resultEditor.getSession().setMode(mode);
    }
    var action = getAction();
    highlightMenu(action);
    showForm(action);
    window.addEventListener("hashchange", function (e) {
        var action = getAction();
        highlightMenu(action);
        showForm(action);
    });
    function errorCallBack(error) {
        setResult(error, "ace/mode/html");
    }
    function getServerUrl() {
        return document.querySelector("#server-url").value;
    }
    function executeAction(action, form) {
        var serverUrl = getServerUrl();
        var api = new SongApi(serverUrl);
        switch (action) {
            case "get":
                var mode = "ace/mode/html";
                if (form.contentType.value === "application/json") {
                    mode = "ace/mode/json";
                }
                else if (form.contentType.value === "text/plain") {
                    mode = "ace/mode/song";
                }
                api.get(form.id.value, form.contentType.value, function (song) {
                    setResult(song, mode);
                }, errorCallBack);
                break;
            case "searchApi":
                var mode = "ace/mode/html";
                if (form.contentType.value === "application/json") {
                    mode = "ace/mode/json";
                }
                else if (form.contentType.value === "text/plain") {
                    mode = "ace/mode/song";
                }
                api.search(form.search.value, form.contentType.value, function (song) {
                    setResult(song, mode);
                }, errorCallBack);
                break;
            case "create":
                api.create(createSongEditor.getValue(), function (id) {
                    setResult(id, "ace/mode/song");
                }, errorCallBack);
                break;
            case "update":
                api.update(form.id.value, updateSongEditor.getValue(), function (result) {
                    setResult(result, "ace/mode/song");
                }, errorCallBack);
                break;
            case "delete":
                api.remove(form.id.value, function (result) {
                    setResult(result, "ace/mode/song");
                }, errorCallBack);
                break;
        }
    }
    var forms = document.querySelectorAll("form");
    forEach.call(forms, function (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();
            var form = e.target;
            var action = getAction();
            executeAction(action, form);
        });
    });
});
//# sourceMappingURL=consoleApi.js.map