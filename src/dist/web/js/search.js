const form = document.getElementById("search");
const input = document.getElementById("querySearch");

if (form && input) {
    form.addEventListener("submit", (e) => {
        e.preventDefault();
        window.location.pathname = "/search/" + encodeURIComponent(input.value);
    });

    // Prefill the search box when landing on a /search/{query} URL
    const prefix = "/search/";
    if (window.location.pathname.startsWith(prefix)) {
        input.value = decodeURIComponent(window.location.pathname.substring(prefix.length));
    }
}
