# Updating vendored frontend assets

The frontend is zero-build: static ES modules and CSS served as-is by Undertow.
Two third-party assets are vendored (pinned copies committed in the repo) so the
site stays fully self-hosted and works offline:

## Monaco editor (`src/dist/web/js/vendor/monaco/`)

A minimal subset of the AMD `min/vs` build (~4 MB): loader, editor core, CSS,
generic worker and codicon font. Basic language packs are not vendored — the
songmark language is registered at runtime from `js/editor/songmark.js`.

To upgrade:

1. Bump `VERSION` in `tools/update-monaco.sh`.
2. Run `./tools/update-monaco.sh` from the repo root (curl + tar, no npm needed).
   The script fails loudly if the tarball layout changed — check the file list
   in the script against the new `min/vs` tree in that case.
3. Test `/edit/{id}` and `/new` (songmark colors, save) and check the browser
   console for loader/worker errors.
4. Commit the result.

## mesnos design system (`src/dist/web/css/vendor/mesnos.style.css`, `src/dist/web/js/vendor/mesnos-dialog.js`)

From https://components.mesnos.ovh, both in the version written in
`css/vendor/MESNOS_VERSION`:

- the shared stylesheet (tokens, buttons, messages, forms, dialogs, dark theme
  via `data-theme`). Songbook's own layers sit on top: `css/app.css` (navbar,
  item list, button groups, print — gaps not yet provided by the library) and
  `css/song.css` (song rendering);
- the shared dialog, `mesnos-dialog.js`: a standalone ES module (no import, no
  build step), whose `confirmDialog()` asks before deleting a song (`view.js`,
  `edit.js`). Its labels default to English (the pages are `lang="en"`).

To upgrade:

1. Bump `VERSION` in `tools/update-mesnos.sh` and run it.
2. Check the site rendering in light and dark themes, and print preview
   (must stay black on white); delete a song from its page and from the
   editor (Cancel, Escape, then Delete).
3. Commit the result.
