/*
 * Songmark language for Monaco, ported from the vscode-songmark TextMate
 * grammar (https://github.com/kawane/vscode-songmark).
 */

export const songmarkLanguage = {
    defaultToken: "",
    tokenizer: {
        root: [
            [/\bhttps?:\/\/\S+/, "link"],
            // "key: value" metadata and section labels (Chorus:, Intro:, Bridge:)
            [/^[^:\n]+:/, "meta"],
            [/\b(?:C|D|E|F|G|A|B)(?:b|#)?(?:m|M|min|maj|dim|Δ|°|ø|Ø)?(?:(?:sus|add)?(?:b|#)?(?:2|4|5|6|7|9|10|11|13)?)*(?:\+|aug|alt)?(?:\/(?:C|D|E|F|G|A|B)(?:b|#)?)?\b/, "chord"],
        ],
    },
};

/* Token colors mirror the site stylesheet: chords blue, metadata green. */
export const songmarkThemes = {
    "songmark-light": {
        base: "vs",
        inherit: true,
        rules: [
            { token: "chord", foreground: "2180ff", fontStyle: "bold" },
            { token: "meta", foreground: "009500", fontStyle: "bold" },
            { token: "link", foreground: "2563eb", fontStyle: "underline" },
        ],
        colors: {},
    },
    "songmark-dark": {
        base: "vs-dark",
        inherit: true,
        rules: [
            { token: "chord", foreground: "6ea8fe", fontStyle: "bold" },
            { token: "meta", foreground: "4cc38a", fontStyle: "bold" },
            { token: "link", foreground: "8ab4f8", fontStyle: "underline" },
        ],
        colors: {},
    },
};
