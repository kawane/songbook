function search(query) {
    window.location.pathname = "/search/" + query;
    return false;
}
var searchForm = document.querySelector("#search");
var queryInputSearch = document.querySelector("#querySearch");
if (searchForm) {
    searchForm.addEventListener("submit", function (e) {
        e.preventDefault();
        search(queryInputSearch.value);
    });
}
var searchPath = "/search/";
if (window.location.pathname.indexOf(searchPath) == 0) {
    var searchQuery = decodeURIComponent(window.location.pathname.substring(searchPath.length));
    if (queryInputSearch) {
        queryInputSearch.value = searchQuery;
    }
}
//# sourceMappingURL=search.js.map