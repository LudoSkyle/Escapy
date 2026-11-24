import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { deleteDoc, doc, getDoc, setDoc, updateDoc } from "firebase/firestore";
import { afterAll, beforeAll, beforeEach, describe, test } from "vitest";
import { PROJECT_ID } from "./support/emulators";

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: readFileSync(resolve(__dirname, "../../firestore.rules"), "utf8"),
      host: "127.0.0.1",
      port: 8080,
    },
  });
});

afterAll(async () => {
  await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "users/alice"), { username: "Alice", avatarId: 1 });
  });
});

describe("users", () => {
  test("a user reads their own profile", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertSucceeds(getDoc(doc(alice, "users/alice")));
  });

  test("a user does not read the profile of another user", async () => {
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertFails(getDoc(doc(bob, "users/alice")));
  });

  test("a signed-out client reads no profile", async () => {
    const anonymous = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(anonymous, "users/alice")));
  });

  test("a user does not create, change or delete even their own profile", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertFails(setDoc(doc(alice, "users/alice"), { username: "Alice", avatarId: 2 }));
    await assertFails(updateDoc(doc(alice, "users/alice"), { avatarId: 2 }));
    await assertFails(deleteDoc(doc(alice, "users/alice")));
    await assertFails(setDoc(doc(alice, "users/carol"), { username: "Carol", avatarId: 3 }));
  });
});

describe("every other collection", () => {
  test("is neither readable nor writable", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertFails(getDoc(doc(alice, "games/any")));
    await assertFails(setDoc(doc(alice, "games/any"), { title: "Hack" }));
    await assertFails(getDoc(doc(alice, "users/alice/private/any")));
  });
});
