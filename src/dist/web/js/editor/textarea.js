/* Plain textarea editor: mobile fallback where Monaco is unusable. */
export function createEditor(host, text) {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.wrap = "off";
    textarea.autocapitalize = "off";
    textarea.autocomplete = "off";
    textarea.spellcheck = false;
    host.appendChild(textarea);

    return {
        getValue: () => textarea.value,
        focus: () => textarea.focus(),
    };
}
