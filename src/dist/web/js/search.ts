
function search(query: string) {
    window.location.pathname = "/search/" + query;
    return false;
}

var searchForm = document.querySelector("#search");
var queryInputSearch = <HTMLInputElement>document.querySelector("#querySearch");
if (searchForm) {
    searchForm.addEventListener("submit", (e) => {
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



