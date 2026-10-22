/*
 * CRUD integration tests running the real browser API client (js/api.js)
 * against the real server in docker. fetch is wrapped to resolve the
 * client's relative URLs and to carry the identity Caddy sets behind the
 * shared login (Remote-User / Remote-Groups), exactly like a signed-in
 * administrator's browser would.
 */
import { beforeAll, afterAll, describe, expect, it } from "vitest";
import { existsSync, readFileSync } from "node:fs";
import path from "node:path";
import * as api from "../src/dist/web/js/api.js";
import { ADMIN_KEY, CONTAINERS, USERS_URL, containerLogs } from "./globalSetup.js";

const BASE = process.env.SONGBOOK_URL ?? `http://localhost:${CONTAINERS.main.port}`;
const realFetch = globalThis.fetch;
const ADMIN = { "Remote-User": "lolo", "Remote-Groups": "admin" };
let identity = ADMIN;

beforeAll(() => {
    globalThis.fetch = (url, options = {}) =>
        realFetch(new URL(url, BASE), {
            ...options,
            headers: { ...options.headers, ...identity },
        });
});

afterAll(() => {
    globalThis.fetch = realFetch;
});

/** A request with no client wrapper: headers, cookies and redirects as given. */
function raw(container, target, options = {}) {
    return realFetch(new URL(target, `http://localhost:${CONTAINERS[container].port}`),
        { redirect: "manual", ...options });
}

function setCookies(response) {
    return response.headers.getSetCookie();
}

// Accented title + multi-word artist: the generated id contains '+' for
// spaces and %-escapes, which once broke update (double URL-encoding).
const SONG = `L'autre île de test
artist: Les Innocents Test
key: G

G          C
Première ligne de test
`;

const UPDATED_SONG = SONG.replace("Première ligne de test", "Ligne modifiée");

describe("song CRUD", () => {
    let id;

    it("creates a song and returns its id", async () => {
        id = await api.createSong(SONG);
        expect(id).toContain("+"); // spaces in title/artist end up as '+'
    });

    it("reads the song back unchanged", async () => {
        expect(await api.getSong(id)).toBe(SONG);
    });

    it("updates a song whose id contains '+' (double-encoding regression)", async () => {
        expect(await api.updateSong(id, UPDATED_SONG)).toBe(id);
        expect(await api.getSong(id)).toBe(UPDATED_SONG);
    });

    it("finds the song in search results", async () => {
        expect(await api.searchSongs("modifiée")).toContain(id);
    });

    it("deletes the song", async () => {
        await api.deleteSong(id);
        const err = await api.getSong(id).catch((e) => e);
        expect(err).toBeInstanceOf(api.ApiError);
        expect(err.status).toBe(404);
    });

    it("returns plain-text errors to API clients, not HTML pages", async () => {
        const err = await api.updateSong("does+not+exist", SONG).catch((e) => e);
        expect(err).toBeInstanceOf(api.ApiError);
        expect(err.status).toBe(404);
        expect(err.body).not.toContain("<html");
    });
});

describe("HTML rendering", () => {
    // Song content must never be able to inject markup into the rendered page
    const XSS_SONG = `Rock & <Roll>
artist: <b>DROP</b>

C
Lyric with <script>alert(1)</script> inside
`;
    let id;

    it("stores the raw song untouched", async () => {
        id = await api.createSong(XSS_SONG);
        expect(await api.getSong(id)).toBe(XSS_SONG); // text/song is verbatim
    });

    it("escapes special characters in the HTML view", async () => {
        const html = await api.getSong(id, "text/html");
        expect(html).not.toContain("<script>alert(1)</script>");
        expect(html).toContain("&lt;script&gt;");
        expect(html).toContain("Rock &amp; &lt;Roll&gt;");
    });

    afterAll(async () => {
        if (id) await api.deleteSong(id).catch(() => {});
    });
});

describe("access behind the shared login", () => {
    // Caddy removes Remote-User / Remote-Groups from client requests and sets
    // them from users on the admin routes: songbook trusts them (USERS_URL)
    async function write(headers) {
        const res = await raw("main", "/songs/", { method: "POST", headers, body: SONG });
        if (res.ok) await raw("main", "/songs/" + await res.text(), { method: "DELETE", headers });
        return res.status;
    }

    it("lets an administrator write", async () => {
        expect(await write(ADMIN)).toBe(200);
        expect(await write({ "Remote-User": "lolo", "Remote-Groups": "editor,admin" })).toBe(200);
    });

    it("refuses a write without identity (401)", async () => {
        expect(await write({})).toBe(401);
        expect(await write({ "Remote-Groups": "admin" })).toBe(401);
    });

    it("refuses a signed-in user who is not an administrator (403)", async () => {
        expect(await write({ "Remote-User": "bob", "Remote-Groups": "user" })).toBe(403);
        expect(await write({ "Remote-User": "bob", "Remote-Groups": "" })).toBe(403);
    });

    it("keeps reading public", async () => {
        const res = await raw("main", "/", { headers: { Accept: "text/html" } });
        expect(res.status).toBe(200);
    });

    it("shows the edit buttons after a visit as administrator, without granting anything", async () => {
        const admin = await raw("main", "/admin", { headers: ADMIN });
        expect(admin.status).toBe(200);
        const display = setCookies(admin).find((c) => c.startsWith("songbook-admin=1"));
        expect(display).toMatch(/HttpOnly/i);
        expect(display).toMatch(/Secure/i);
        expect(display).toMatch(/SameSite=Lax/i);

        const page = await (await raw("main", "/", { headers: { Accept: "text/html", Cookie: "songbook-admin=1" } })).text();
        expect(page).toContain('class="admin-admin"');
        const anonymous = await (await raw("main", "/", { headers: { Accept: "text/html" } })).text();
        expect(anonymous).toContain('class="user-admin"');

        const res = await raw("main", "/songs/", { method: "POST", headers: { Cookie: "songbook-admin=1" }, body: SONG });
        expect(res.status).toBe(401);
    });

    it("loads the users session keeper on the edit pages only", async () => {
        const edit = await (await raw("main", "/new", { headers: ADMIN })).text();
        expect(edit).toContain(`<script defer data-no-banner src="${USERS_URL}/session-keeper.js"></script>`);
        const home = await (await raw("main", "/", { headers: { ...ADMIN, Accept: "text/html" } })).text();
        expect(home).not.toContain("session-keeper.js");
    });

    it("signs out at users", async () => {
        const res = await raw("main", "/signout", { method: "POST" });
        expect(res.status).toBe(303);
        expect(res.headers.get("location")).toBe(`${USERS_URL}/api/logout`);
        expect(setCookies(res).some((c) => c.startsWith("songbook-admin=") && /Max-Age=0/i.test(c))).toBe(true);
    });

    it("reindexes on POST only", async () => {
        expect((await raw("main", "/admin/index/reset", { headers: ADMIN })).status).toBe(405);
        expect((await raw("main", "/admin/index/reset", { method: "POST", headers: ADMIN })).status).toBe(200);
        expect((await raw("main", "/admin/index/reset", { method: "POST" })).status).toBe(401);
    });
});

describe("administrator key login (fallback)", () => {
    function signIn(key) {
        return raw("main", "/signin", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ key }),
        });
    }

    it("refuses a wrong key", async () => {
        const res = await signIn("wrong-key");
        expect(res.status).toBe(401);
        expect(setCookies(res)).toEqual([]);
    });

    it("opens a session whose cookie is not the key", async () => {
        const res = await signIn(ADMIN_KEY);
        expect(res.status).toBe(303);
        const session = setCookies(res).find((c) => c.startsWith("songbook-session="));
        expect(session).toMatch(/HttpOnly/i);
        expect(session).toMatch(/Secure/i);
        expect(session).toMatch(/SameSite=Strict/i);
        expect(session).not.toContain(ADMIN_KEY);

        const cookie = session.split(";")[0];
        const created = await raw("main", "/songs/", { method: "POST", headers: { Cookie: cookie }, body: SONG });
        expect(created.status).toBe(200);
        await raw("main", "/songs/" + await created.text(), { method: "DELETE", headers: { Cookie: cookie } });

        // A browser can keep a stale duplicate next to the current cookie
        const stale = await raw("main", "/admin", { headers: { Cookie: `songbook-session=1.stale; ${cookie}` } });
        expect(stale.status).toBe(200);

        const forged = await raw("main", "/admin", { headers: { Cookie: cookie.slice(0, -2) + "xx" } });
        expect(forged.status).toBe(401);
    });

    it("never reads the key from an address or from the old cookie", async () => {
        const res = await raw("main", `/?key=${ADMIN_KEY}`, { headers: { Accept: "text/html", Cookie: `SessionKey=${ADMIN_KEY}` } });
        expect(res.status).toBe(200);
        expect(setCookies(res).some((c) => c.startsWith("songbook-session="))).toBe(false);
        // the old cookie, which held the key itself, is removed
        expect(setCookies(res).some((c) => c.startsWith("SessionKey=") && /Max-Age=0/i.test(c))).toBe(true);
        expect(await res.text()).toContain('class="user-admin"');

        const write = await raw("main", `/songs/?key=${ADMIN_KEY}`, { method: "POST", headers: { Cookie: `SessionKey=${ADMIN_KEY}` }, body: SONG });
        expect(write.status).toBe(401);
        const get = await raw("main", `/signin?key=${ADMIN_KEY}`);
        expect(setCookies(get).some((c) => c.startsWith("songbook-session="))).toBe(false);
    });

    it("creates a key when there is none, without printing it", async () => {
        const keyFile = path.join(CONTAINERS.fresh.data, "administrator.key");
        expect(existsSync(keyFile)).toBe(true);
        const key = readFileSync(keyFile, "utf8").trim();
        expect(key).toMatch(/^[0-9a-f]{32}$/);
        expect(containerLogs(CONTAINERS.fresh.name)).not.toContain(key);
        const page = await (await raw("fresh", "/", { headers: { Accept: "text/html" } })).text();
        expect(page).not.toContain(key);
    });

    it("never prints the key, even after sign-ins and old links", () => {
        expect(containerLogs(CONTAINERS.main.name)).not.toContain(ADMIN_KEY);
    });
});

describe("defaults (no shared login, no key)", () => {
    it("ignores the identity headers", async () => {
        const res = await raw("plain", "/songs/", { method: "POST", headers: ADMIN, body: SONG });
        expect(res.status).toBe(401);
    });

    it("has no key login and creates no key", async () => {
        const get = await raw("plain", "/signin");
        expect(get.status).toBe(303);
        expect(get.headers.get("location")).toBe("/admin");
        const post = await raw("plain", "/signin", { method: "POST", body: "key=anything" });
        expect(post.status).toBe(404);
        expect(existsSync(path.join(CONTAINERS.plain.data, "administrator.key"))).toBe(false);
    });

    it("keeps reading public", async () => {
        const res = await raw("plain", "/", { headers: { Accept: "text/html" } });
        expect(res.status).toBe(200);
    });
});

describe("health", () => {
    // Public route, queried by the Docker HEALTHCHECK: no session cookie.
    it("reports ok without a session, with every check", async () => {
        const res = await realFetch(new URL("/api/health", BASE));
        expect(res.status).toBe(200);
        expect(res.headers.get("content-type")).toContain("application/json");
        const health = await res.json();
        expect(health.status).toBe("ok");
        expect(health.version).toBeTruthy();
        expect(health.checks.map((c) => [c.name, c.ok, c.critical])).toEqual([
            ["songs", true, true],
            ["index", true, true],
            ["data-writable", true, false],
        ]);
    });
});
