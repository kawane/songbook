# AI Development Guidelines — songbook

A minimal server to store, search and display songs in ChordPro format (lyrics
and chords). Public for reading on `songbook.llgmusic.net`; editing is
reserved to the administrator.

songbook is part of the mesnos ecosystem but deliberately independent: it calls
no other service at runtime. Ecosystem context (services, principles,
decisions): [mesnos/carnet.mesnos.ovh](https://git.mesnos.ovh/mesnos/carnet.mesnos.ovh).

## Quick Reference

- **Server**: Java 25, Undertow (HTTP), Lucene (search). Entry point
  `songbook.server.Server`. Built with Gradle inside Docker (no Gradle wrapper).
- **Frontend**: zero-build — native ES modules and plain CSS served as-is from
  `src/dist/web/` (`js/`, `css/`, `templates/`).
- **Vendored assets**: the mesnos design system — `mesnos.style.css` and the
  shared dialog `js/vendor/mesnos-dialog.js` (`confirmDialog()`), version in
  `src/dist/web/css/vendor/MESNOS_VERSION` — and Monaco (editor). Update them
  with `tools/update-mesnos.sh` / `tools/update-monaco.sh`, see
  `doc/Update_Vendored_Assets.md`. Never edit vendored files by hand.
- **Data**: one ChordPro file per song in `data/songs/`; the Lucene index and
  the admin key live in the data directory at runtime (git-ignored).

## Commands

```sh
docker build -t songbook .                                  # build
docker run --rm -p 8000:8000 -v "$PWD/data:/data" -e ADMIN_KEY_LOGIN=true songbook   # run on :8000 (key in data/administrator.key)
npm ci && npm test   # API tests (Node 24, Docker required: they build and run the image)
```

The tests also run on every push and pull request (`.github/workflows/tests.yml`).

## Things that other code relies on

- **Content negotiation**: `/songs/{id}` returns HTML, plain text or raw
  ChordPro (`text/song`) depending on the `Accept` header. This is the planned
  way for learnsong to read songs from songbook — keep it stable.
- **Admin access**: routes wrapped in `adminAccess(...)` (`/edit`, `/delete`,
  `/new`, `/admin`) and the write methods of `/songs/{id}`. Reading must stay
  public. The identity is read in one place, `Access.currentUser`:
  - behind the shared login (`USERS_URL`, `https://users.llgmusic.net` in
    production), admin = `Remote-User` set and `Remote-Groups` containing
    `admin`. These headers are trusted only because Caddy removes them from
    every client request and sets them from users (`forward_auth`) on the admin
    routes; the port must not be published on the host. Without `USERS_URL`
    they are ignored;
  - fallback `ADMIN_KEY_LOGIN=true`: `POST /signin` with the key in the body,
    cookie `songbook-session` = expiry + HMAC (never the key), `POST /signout`.
    `?key=` and the old `SessionKey` cookie are not read anymore;
  - the `songbook-admin` cookie only shows the edit buttons on the public
    pages; it grants nothing.
  - 401 = nobody signed in, 403 = signed in without the admin role; `edit.js`
    handles both, and the edit pages load the users session keeper
    (`data-no-banner`, songbook shows its own message).
  - State changes are POST only (`/admin/index/reset`, `/signout`).
  - Later (decision 0016): check the users JWT in `Access` instead of the
    headers.
- **Health**: `GET /api/health` is public (served before the session check)
  and answers 200 `ok`/`degraded` or 503 `down` with the list of checks
  (`Health.java`; vital: `songs`, `index`; not vital: `data-writable`). The
  Docker `HEALTHCHECK` calls it. Shared contract: `ecosysteme/sante.md` in
  the knowledge base. Never put paths or raw errors in it.
- **HTML escaping**: `SongUtils.writeHtml` escapes all song content and only
  emits links for http/https/mailto. Keep it that way (it closes a stored XSS).

## Documentation sync

| Change | Update |
|---|---|
| Route, `Accept` behaviour, admin access | this file and `ecosysteme/services.md` in the knowledge base |
| Vendored asset version | `doc/Update_Vendored_Assets.md` if the procedure changes |

## Don'ts

- ❌ Add a frontend build step (bundler, transpiler): the site is zero-build on purpose
- ❌ Make songbook call another service at runtime without a degraded mode
  (the session keeper is loaded by the browser on the edit pages only: reading
  never depends on users)
- ❌ Log or print the admin key, or accept it in an address
- ❌ Trust `Remote-User` / `Remote-Groups` anywhere but in `Access`, or without `USERS_URL`
- ❌ New dependencies without discussion
