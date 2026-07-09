import { songmarkLanguage, songmarkThemes } from "./songmark.js";

const VS = "/js/vendor/monaco/vs";
let monacoPromise;

/*
 * Monaco ships as an AMD bundle: inject its loader on demand and resolve the
 * global `monaco` once editor.main is loaded. Nothing else on the site is AMD,
 * so the loader's global `require`/`define` are harmless.
 */
function loadMonaco() {
    return monacoPromise ??= new Promise((resolve, reject) => {
        const script = document.createElement("script");
        script.src = `${VS}/loader.js`;
        script.onload = () => {
            window.MonacoEnvironment = {
                getWorkerUrl: () => `${VS}/base/worker/workerMain.js`,
            };
            window.require.config({ paths: { vs: VS } });
            window.require(["vs/editor/editor.main"], () => resolve(window.monaco), reject);
        };
        script.onerror = () => reject(new Error("Failed to load Monaco loader"));
        document.head.appendChild(script);
    });
}

function currentTheme() {
    return document.documentElement.dataset.theme === "dark" ? "songmark-dark" : "songmark-light";
}

export async function createEditor(host, text) {
    const monaco = await loadMonaco();

    if (!monaco.languages.getLanguages().some((l) => l.id === "songmark")) {
        monaco.languages.register({ id: "songmark" });
        monaco.languages.setMonarchTokensProvider("songmark", songmarkLanguage);
        for (const [name, theme] of Object.entries(songmarkThemes)) {
            monaco.editor.defineTheme(name, theme);
        }
    }

    const editor = monaco.editor.create(host, {
        value: text,
        language: "songmark",
        theme: currentTheme(),
        fontFamily: "ui-monospace, Consolas, Menlo, monospace",
        fontSize: 14,
        minimap: { enabled: false },
        wordWrap: "off",
        lineNumbers: "off",
        automaticLayout: true,
        scrollBeyondLastLine: false,
    });

    // Follow the site theme toggle (data-theme attribute on <html>)
    new MutationObserver(() => monaco.editor.setTheme(currentTheme()))
        .observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme"] });

    return {
        getValue: () => editor.getValue(),
        focus: () => editor.focus(),
    };
}
