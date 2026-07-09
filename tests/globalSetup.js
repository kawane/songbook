/*
 * Builds the docker image and runs a throwaway container for the tests, with
 * a known admin key seeded in /data. Requires docker; nothing else touches
 * the developer's containers (dedicated name + port).
 */
import { execFileSync } from "node:child_process";
import { mkdirSync, writeFileSync, rmSync } from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";

const ROOT = path.dirname(fileURLToPath(import.meta.url));
const DATA_DIR = path.join(ROOT, ".data");
const CONTAINER = "songbook-vitest";
const IMAGE = "songbook:test";
export const PORT = 18000;
export const ADMIN_KEY = "test-admin-key";

function docker(...args) {
    return execFileSync("docker", args, { stdio: ["ignore", "pipe", "inherit"] }).toString();
}

export async function setup() {
    docker("build", "-q", "-t", IMAGE, path.join(ROOT, ".."));

    rmSync(DATA_DIR, { recursive: true, force: true });
    mkdirSync(DATA_DIR, { recursive: true });
    writeFileSync(path.join(DATA_DIR, "administrator.key"), ADMIN_KEY + "\n");
    writeFileSync(path.join(DATA_DIR, "administrator.activated"), "");

    try {
        execFileSync("docker", ["rm", "-f", CONTAINER], { stdio: "ignore" });
    } catch { /* not running */ }
    docker("run", "-d", "--rm", "--name", CONTAINER,
        "-p", `${PORT}:8000`, "-v", `${DATA_DIR}:/data`, IMAGE);

    // Wait for the server to accept requests
    const deadline = Date.now() + 30000;
    for (;;) {
        try {
            await fetch(`http://localhost:${PORT}/`);
            break;
        } catch {
            if (Date.now() > deadline) throw new Error("songbook container did not come up on :" + PORT);
            await new Promise((r) => setTimeout(r, 500));
        }
    }
}

export async function teardown() {
    try { docker("rm", "-f", CONTAINER); } catch { /* already gone */ }
    rmSync(DATA_DIR, { recursive: true, force: true });
}
