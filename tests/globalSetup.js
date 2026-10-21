/*
 * Builds the docker image and runs throwaway containers for the tests.
 * Requires docker; nothing else touches the developer's containers
 * (dedicated names + ports).
 *
 * - main: behind the shared login (USERS_URL: Remote-User / Remote-Groups
 *   are trusted) with the key login on and a known key seeded in /data;
 * - fresh: key login on, empty /data: the server creates the key;
 * - plain: the defaults, neither the headers nor a key.
 */
import { execFileSync, spawnSync } from "node:child_process";
import { mkdirSync, writeFileSync, rmSync } from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";

const ROOT = path.dirname(fileURLToPath(import.meta.url));
const IMAGE = "songbook:test";
export const ADMIN_KEY = "test-admin-key";
export const USERS_URL = "https://users.example.test";

export const CONTAINERS = {
    main: {
        name: "songbook-vitest", port: 18000, data: path.join(ROOT, ".data"),
        env: [`USERS_URL=${USERS_URL}`, "ADMIN_KEY_LOGIN=true"],
    },
    fresh: {
        name: "songbook-vitest-fresh", port: 18001, data: path.join(ROOT, ".data-fresh"),
        env: ["ADMIN_KEY_LOGIN=true"],
    },
    plain: {
        name: "songbook-vitest-plain", port: 18002, data: path.join(ROOT, ".data-plain"),
        env: [],
    },
};
export const PORT = CONTAINERS.main.port;

function docker(...args) {
    return execFileSync("docker", args, { stdio: ["ignore", "pipe", "inherit"] }).toString();
}

/** What the server printed (stdout and stderr), for the tests that check what it never prints. */
export function containerLogs(name) {
    const result = spawnSync("docker", ["logs", name], { encoding: "utf8" });
    return result.stdout + result.stderr;
}

async function waitFor(port) {
    const deadline = Date.now() + 30000;
    for (;;) {
        try {
            await fetch(`http://localhost:${port}/api/health`);
            return;
        } catch {
            if (Date.now() > deadline) throw new Error("songbook container did not come up on :" + port);
            await new Promise((r) => setTimeout(r, 500));
        }
    }
}

export async function setup() {
    docker("build", "-q", "-t", IMAGE, path.join(ROOT, ".."));

    for (const container of Object.values(CONTAINERS)) {
        rmSync(container.data, { recursive: true, force: true });
        mkdirSync(container.data, { recursive: true });
        try {
            execFileSync("docker", ["rm", "-f", container.name], { stdio: "ignore" });
        } catch { /* not running */ }
    }
    writeFileSync(path.join(CONTAINERS.main.data, "administrator.key"), ADMIN_KEY + "\n");

    for (const container of Object.values(CONTAINERS)) {
        docker("run", "-d", "--rm", "--name", container.name,
            ...container.env.flatMap((e) => ["-e", e]),
            "-p", `${container.port}:8000`, "-v", `${container.data}:/data`, IMAGE);
    }
    await Promise.all(Object.values(CONTAINERS).map((c) => waitFor(c.port)));
}

export async function teardown() {
    for (const container of Object.values(CONTAINERS)) {
        try { docker("rm", "-f", container.name); } catch { /* already gone */ }
        // The server runs as root in the container, so on a Linux host the files
        // it wrote to the bind mount belong to root and cannot be removed by the
        // test runner. Empty the directory from inside a container first (Docker
        // Desktop hides this ownership difference).
        try {
            docker("run", "--rm", "-v", `${container.data}:/data`, "--entrypoint", "sh", IMAGE,
                "-c", "rm -rf /data/* /data/.[!.]*");
        } catch { /* nothing to clean */ }
        rmSync(container.data, { recursive: true, force: true });
    }
}
