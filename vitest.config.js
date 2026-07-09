import { defineConfig } from "vitest/config";

export default defineConfig({
    test: {
        globalSetup: "./tests/globalSetup.js",
        testTimeout: 15000,
        hookTimeout: 180000,
    },
});
