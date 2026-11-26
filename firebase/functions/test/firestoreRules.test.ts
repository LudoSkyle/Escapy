import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { collection, deleteDoc, doc, getDoc, getDocs, query, setDoc, updateDoc, where } from "firebase/firestore";
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
    const firestore = context.firestore();
    await setDoc(doc(firestore, "users/alice"), { username: "Alice", avatarId: 1 });
    await setDoc(doc(firestore, "users/alice/games/manor"), { title: "Le manoir", isMaster: true });
    // Alice runs the game, Bob plays it, Carol is in none
    await setDoc(doc(firestore, "games/manor"), { title: "Le manoir", masterId: "alice", memberIds: ["alice", "bob"] });
    await setDoc(doc(firestore, "games/manor/players/bob"), { username: "Bob", avatarId: 2 });
    await setDoc(doc(firestore, "games/manor/private/access"), { excludedIds: ["dave"] });
    await setDoc(doc(firestore, "joinCodes/ABCDEF"), { gameId: "manor" });
    await setDoc(doc(firestore, "joinAttempts/carol"), { failures: 1 });
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

  test("a user reads the list of their games, and nobody else does", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertSucceeds(getDocs(collection(alice, "users/alice/games")));
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertFails(getDocs(collection(bob, "users/alice/games")));
    await assertFails(getDoc(doc(bob, "users/alice/games/manor")));
    await assertFails(setDoc(doc(alice, "users/alice/games/other"), { title: "Hack", isMaster: true }));
  });

  test("a user does not create, change or delete even their own profile", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertFails(setDoc(doc(alice, "users/alice"), { username: "Alice", avatarId: 2 }));
    await assertFails(updateDoc(doc(alice, "users/alice"), { avatarId: 2 }));
    await assertFails(deleteDoc(doc(alice, "users/alice")));
    await assertFails(setDoc(doc(alice, "users/carol"), { username: "Carol", avatarId: 3 }));
  });
});

describe("games", () => {
  test("the game master and the players read the game and its players", async () => {
    for (const member of ["alice", "bob"]) {
      const firestore = testEnv.authenticatedContext(member).firestore();
      await assertSucceeds(getDoc(doc(firestore, "games/manor")));
      await assertSucceeds(getDocs(collection(firestore, "games/manor/players")));
    }
  });

  test("nobody lists the games, not even those they are a member of", async () => {
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertFails(getDocs(collection(bob, "games")));
    await assertFails(getDocs(query(collection(bob, "games"), where("memberIds", "array-contains", "bob"))));
  });

  test("a user who is not a member reads neither the game nor its players", async () => {
    const carol = testEnv.authenticatedContext("carol").firestore();
    await assertFails(getDoc(doc(carol, "games/manor")));
    await assertFails(getDocs(collection(carol, "games/manor/players")));
    await assertFails(getDoc(doc(carol, "games/manor/players/bob")));
  });

  test("a signed-out client reads no game", async () => {
    const anonymous = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(anonymous, "games/manor")));
    await assertFails(getDoc(doc(anonymous, "games/missing")));
  });

  test("a deleted game reads as missing, not as refused", async () => {
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertSucceeds(getDoc(doc(bob, "games/missing")));
  });

  test("no member reads the excluded players, not even the game master", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertFails(getDoc(doc(alice, "games/manor/private/access")));
  });

  test("nobody writes a game or its players, not even the game master", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertFails(updateDoc(doc(alice, "games/manor"), { title: "Hack" }));
    await assertFails(setDoc(doc(alice, "games/other"), { masterId: "alice", memberIds: ["alice"] }));
    await assertFails(deleteDoc(doc(alice, "games/manor")));
    await assertFails(setDoc(doc(alice, "games/manor/players/carol"), { username: "Carol", avatarId: 3 }));
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertFails(deleteDoc(doc(bob, "games/manor/players/bob")));
  });

  test("nobody reads or writes the codes and the attempts", async () => {
    const carol = testEnv.authenticatedContext("carol").firestore();
    await assertFails(getDoc(doc(carol, "joinCodes/ABCDEF")));
    await assertFails(getDocs(collection(carol, "joinCodes")));
    await assertFails(setDoc(doc(carol, "joinCodes/GHJKMN"), { gameId: "manor" }));
    await assertFails(getDoc(doc(carol, "joinAttempts/carol")));
    await assertFails(deleteDoc(doc(carol, "joinAttempts/carol")));
  });
});

describe("every other collection", () => {
  test("is neither readable nor writable", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertFails(getDoc(doc(alice, "scenarios/any")));
    await assertFails(setDoc(doc(alice, "scenarios/any"), { title: "Hack" }));
    await assertFails(getDoc(doc(alice, "users/alice/private/any")));
  });
});
