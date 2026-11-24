import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import {
  assertFails,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { getBytes, ref, uploadString } from "firebase/storage";
import { afterAll, beforeAll, describe, test } from "vitest";
import { PROJECT_ID } from "./support/emulators";

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    storage: {
      rules: readFileSync(resolve(__dirname, "../../storage.rules"), "utf8"),
      host: "127.0.0.1",
      port: 9199,
    },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

describe("storage", () => {
  test("refuses every read and every write, signed in or not", async () => {
    const alice = testEnv.authenticatedContext("alice").storage();
    const anonymous = testEnv.unauthenticatedContext().storage();
    await assertFails(uploadString(ref(alice, "games/any/hints/image.png"), "data"));
    await assertFails(getBytes(ref(alice, "users/alice/avatar.png")));
    await assertFails(uploadString(ref(anonymous, "any.txt"), "data"));
  });
});
