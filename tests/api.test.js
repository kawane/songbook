/*
 * CRUD integration tests running the real browser API client (js/api.js)
 * against the real server in docker. fetch is wrapped to resolve the
 * client's relative URLs and to carry the SessionKey cookie, exactly like
 * a signed-in browser would.
 */
import { beforeAll, afterAll, describe, expect, it } from "vitest";
import * as api from "../src/dist/web/js/api.js";
import { PORT, ADMIN_KEY } from "./globalSetup.js";

const BASE = process.env.SONGBOOK_URL ?? `http://localhost:${PORT}`;
const realFetch = globalThis.fetch;
let sessionKey = ADMIN_KEY;

beforeAll(() => {
    globalThis.fetch = (url, options = {}) =>
        realFetch(new URL(url, BASE), {
            ...options,
            headers: {
                ...options.headers,
                ...(sessionKey ? { Cookie: `SessionKey=${sessionKey}` } : {}),
            },
        });
});

afterAll(() => {
    globalThis.fetch = realFetch;
});

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

describe("authorization", () => {
    it("rejects writes without the admin key", async () => {
        sessionKey = null;
        try {
            const err = await api.createSong(SONG).catch((e) => e);
            expect(err).toBeInstanceOf(api.ApiError);
            expect(err.status).toBe(401);
        } finally {
            sessionKey = ADMIN_KEY;
        }
    });
});
