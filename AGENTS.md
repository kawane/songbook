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
- **Vendored assets**: `mesnos.style.css` (shared design system, version in
  `src/dist/web/css/vendor/MESNOS_VERSION`) and Monaco (editor). Update them
  with `tools/update-mesnos.sh` / `tools/update-monaco.sh`, see
  `doc/Update_Vendored_Assets.md`. Never edit vendored files by hand.
- **Data**: one ChordPro file per song in `data/songs/`; the Lucene index and
  the admin key live in the data directory at runtime (git-ignored).

## Commands

```sh
docker build -t songbook .                                  # build
docker run --rm -p 8000:8000 -v "$PWD/data:/data" songbook   # run on :8000
npm ci && npm test   # API tests (Node 24, Docker required: they build and run the image)
```

The tests also run on every push and pull request (`.github/workflows/tests.yml`).

## Things that other code relies on

- **Content negotiation**: `/songs/{id}` returns HTML, plain text or raw
  ChordPro (`text/song`) depending on the `Accept` header. This is the planned
  way for learnsong to read songs from songbook — keep it stable.
- **Admin access**: routes wrapped in `adminAccess(...)` (`/edit`, `/delete`,
  `/new`, `/admin`) and the write methods of `/songs/{id}`. Reading must stay
  public.
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
- ❌ Log or print the admin key
- ❌ New dependencies without discussion
