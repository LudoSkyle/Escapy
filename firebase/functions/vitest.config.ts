import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["test/**/*.test.ts"],
    // One emulator suite for every file: the files run one after the other, each on clean data
    fileParallelism: false,
    testTimeout: 20_000,
    hookTimeout: 20_000,
  },
});
