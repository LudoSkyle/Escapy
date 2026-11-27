import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
  RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  addDoc,
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  query,
  serverTimestamp,
  setDoc,
  Timestamp,
  updateDoc,
  where,
} from "firebase/firestore";
import { afterAll, beforeAll, beforeEach, describe, test } from "vitest";
import { FIRESTORE_PORT, PROJECT_ID } from "./support/emulators";

let testEnv: RulesTestEnvironment;

beforeAll(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: readFileSync(resolve(__dirname, "../../firestore.rules"), "utf8"),
      host: "127.0.0.1",
      port: FIRESTORE_PORT,
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
    await setDoc(doc(firestore, "users/bob"), { username: "Bob", avatarId: 2 });
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

describe("chat", () => {
  /** The game, running for an hour more, or as [change] makes it. */
  async function setGame(change: Record<string, unknown> = {}) {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      await setDoc(doc(context.firestore(), "games/manor"), {
        title: "Le manoir",
        masterId: "alice",
        memberIds: ["alice", "bob"],
        status: "RUNNING",
        endsAt: Timestamp.fromMillis(Date.now() + 60 * 60 * 1000),
        ...change,
      });
    });
  }

  function message(uid: string, change: Record<string, unknown> = {}) {
    const author = uid === "alice" ? { username: "Alice", avatarId: 1 } : { username: "Bob", avatarId: 2 };
    return { authorId: uid, author, text: "On a trouvé la clé !", sentAt: serverTimestamp(), ...change };
  }

  function messages(uid: string) {
    return collection(testEnv.authenticatedContext(uid).firestore(), "games/manor/messages");
  }

  test("a player and the game master send a message in their own name and read the chat", async () => {
    await setGame();
    await assertSucceeds(addDoc(messages("bob"), message("bob")));
    await assertSucceeds(addDoc(messages("alice"), message("alice")));
    await assertSucceeds(getDocs(messages("bob")));
  });

  test("a paused game keeps its chat open", async () => {
    await setGame({ status: "PAUSED", endsAt: null, remainingMs: 1000 });
    await assertSucceeds(addDoc(messages("bob"), message("bob")));
  });

  test("a user who is not a member, or signed out, neither reads nor writes", async () => {
    await setGame();
    await assertFails(addDoc(messages("carol"), message("carol", { author: { username: "Carol", avatarId: 3 } })));
    await assertFails(getDocs(messages("carol")));
    const anonymous = collection(testEnv.unauthenticatedContext().firestore(), "games/manor/messages");
    await assertFails(getDocs(anonymous));
    await assertFails(addDoc(anonymous, message("bob")));
  });

  test("an excluded player no longer reads nor writes", async () => {
    await setGame({ memberIds: ["alice"] });
    await assertFails(addDoc(messages("bob"), message("bob")));
    await assertFails(getDocs(messages("bob")));
  });

  test("a message in the name of another user, or with another name or avatar, is refused", async () => {
    await setGame();
    await assertFails(addDoc(messages("bob"), message("bob", { authorId: "alice" })));
    await assertFails(addDoc(messages("bob"), message("bob", { author: { username: "Alice", avatarId: 2 } })));
    await assertFails(addDoc(messages("bob"), message("bob", { author: { username: "Bob", avatarId: 9 } })));
    await assertFails(addDoc(messages("bob"), message("bob", { author: { username: "Bob", avatarId: 2, admin: true } })));
  });

  test("a message with a field too many, or the time of the device, is refused", async () => {
    await setGame();
    await assertFails(addDoc(messages("bob"), message("bob", { isHint: true })));
    await assertFails(addDoc(messages("bob"), message("bob", { sentAt: Timestamp.now() })));
    const { sentAt: _sentAt, ...withoutTime } = message("bob");
    await assertFails(addDoc(messages("bob"), withoutTime));
  });

  test("a text of 500 UTF-16 units is the longest: an accent counts one, an emoji two", async () => {
    // As String.length counts in Kotlin, which the app checks the same way
    await setGame();
    await assertSucceeds(addDoc(messages("bob"), message("bob", { text: "é".repeat(500) })));
    await assertFails(addDoc(messages("bob"), message("bob", { text: "é".repeat(501) })));
    await assertSucceeds(addDoc(messages("bob"), message("bob", { text: "🔑".repeat(250) })));
    await assertFails(addDoc(messages("bob"), message("bob", { text: "🔑".repeat(251) })));
  });

  test("an empty text, or one of spaces only, is refused", async () => {
    await setGame();
    await assertFails(addDoc(messages("bob"), message("bob", { text: "" })));
    await assertFails(addDoc(messages("bob"), message("bob", { text: "   " })));
    await assertFails(addDoc(messages("bob"), message("bob", { text: 42 })));
  });

  test("the chat is closed before the game starts, once it is over, and once its time is up", async () => {
    await setGame({ status: "WAITING", endsAt: null });
    await assertFails(addDoc(messages("bob"), message("bob")));
    await setGame({ status: "FINISHED", endsAt: null });
    await assertFails(addDoc(messages("bob"), message("bob")));
    await setGame({ endsAt: Timestamp.fromMillis(Date.now() - 1000) });
    await assertFails(addDoc(messages("bob"), message("bob")));
    // The chat of a game over is still read
    await assertSucceeds(getDocs(messages("bob")));
  });

  test("nobody changes or deletes a message, not even its author", async () => {
    await setGame();
    let id = "";
    await testEnv.withSecurityRulesDisabled(async (context) => {
      id = (await addDoc(collection(context.firestore(), "games/manor/messages"), message("bob"))).id;
    });
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertFails(updateDoc(doc(bob, `games/manor/messages/${id}`), { text: "Rien" }));
    await assertFails(deleteDoc(doc(bob, `games/manor/messages/${id}`)));
  });
});

describe("missions", () => {
  beforeEach(async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const firestore = context.firestore();
      await setDoc(doc(firestore, "games/manor/masterMissions/safe"), { title: "Le coffre", answers: ["0427"], state: "ACTIVE" });
      await setDoc(doc(firestore, "games/manor/missions/safe"), { title: "Le coffre", state: "ACTIVE" });
    });
  });

  test("the members read the missions they see, and nobody else does", async () => {
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertSucceeds(getDocs(collection(bob, "games/manor/missions")));
    await assertSucceeds(getDoc(doc(bob, "games/manor/missions/safe")));
    const carol = testEnv.authenticatedContext("carol").firestore();
    await assertFails(getDocs(collection(carol, "games/manor/missions")));
    await assertFails(getDocs(collection(testEnv.unauthenticatedContext().firestore(), "games/manor/missions")));
  });

  test("only the game master reads the missions with their answers", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertSucceeds(getDocs(collection(alice, "games/manor/masterMissions")));
    await assertSucceeds(getDoc(doc(alice, "games/manor/masterMissions/safe")));
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertFails(getDocs(collection(bob, "games/manor/masterMissions")));
    await assertFails(getDoc(doc(bob, "games/manor/masterMissions/safe")));
    const carol = testEnv.authenticatedContext("carol").firestore();
    await assertFails(getDoc(doc(carol, "games/manor/masterMissions/safe")));
    await assertFails(getDocs(collection(testEnv.unauthenticatedContext().firestore(), "games/manor/masterMissions")));
  });

  test("nobody writes a mission, not even the game master", async () => {
    const alice = testEnv.authenticatedContext("alice").firestore();
    await assertFails(setDoc(doc(alice, "games/manor/missions/safe"), { title: "Hack", state: "SOLVED" }));
    await assertFails(updateDoc(doc(alice, "games/manor/masterMissions/safe"), { state: "SOLVED" }));
    const bob = testEnv.authenticatedContext("bob").firestore();
    await assertFails(updateDoc(doc(bob, "games/manor/missions/safe"), { state: "SOLVED" }));
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
