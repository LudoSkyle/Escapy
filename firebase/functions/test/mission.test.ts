import { signInAnonymously } from "firebase/auth";
import { Timestamp } from "firebase-admin/firestore";
import { afterEach, beforeEach, describe, expect, test } from "vitest";
import { ANSWERS_PER_MINUTE, isCorrectAnswer, MISSIONS_PER_GAME_LIMIT, normalizeText, validateMission } from "../src/game/mission";
import { adminFirestore, clearEmulators, failureOf, newClient, TestClient } from "./support/emulators";

interface Created {
  gameId: string;
  joinCode: string;
}

const NEW_GAME = { title: "Le manoir", durationMinutes: 60 };

function mission(missionId: string, change: Record<string, unknown> = {}) {
  return {
    missionId,
    title: `Mission ${missionId}`,
    statement: "Trouvez le code du coffre.",
    type: "CODE",
    answers: ["0427"],
    trigger: { type: "ON_START" },
    isFinal: false,
    ...change,
  };
}

describe("missions", () => {
  let clients: TestClient[] = [];

  async function user() {
    const client = newClient();
    const { user: signedIn } = await signInAnonymously(client.auth);
    await client.call("ensureProfile");
    clients.push(client);
    return Object.assign(client, { uid: signedIn.uid });
  }

  /** A game with one player, waiting: its game master writes missions before starting it. */
  async function waitingGame() {
    const master = await user();
    const player = await user();
    const created = await master.call<Created>("createGame", NEW_GAME);
    await player.call("joinGame", { code: created.joinCode });
    const save = (missionId: string, change: Record<string, unknown> = {}) =>
      master.call("saveMission", { gameId: created.gameId, ...mission(missionId, change) });
    return { master, player, gameId: created.gameId, save };
  }

  function game(gameId: string) {
    return adminFirestore().collection("games").doc(gameId);
  }

  async function masterMission(gameId: string, missionId: string) {
    return (await game(gameId).collection("masterMissions").doc(missionId).get()).data();
  }

  async function visibleMission(gameId: string, missionId: string) {
    return (await game(gameId).collection("missions").doc(missionId).get()).data();
  }

  beforeEach(async () => {
    await clearEmulators();
    clients = [];
  });

  afterEach(async () => {
    await Promise.all(clients.map((client) => client.close()));
  });

  describe("saveMission", () => {
    test("writes a locked mission the players do not see, and creates nothing twice when sent again", async () => {
      // Given
      const { gameId, save } = await waitingGame();

      // When
      await save("safe");
      await save("safe", { title: "Le coffre" });

      // Then
      const stored = await masterMission(gameId, "safe");
      expect(stored).toMatchObject({ title: "Le coffre", type: "CODE", answers: ["0427"], codeLength: 4, state: "LOCKED" });
      expect(await visibleMission(gameId, "safe")).toBeUndefined();
      expect((await game(gameId).collection("masterMissions").get()).size).toBe(1);
    });

    test("refuses a player, a mission out of bounds, and one mission too many", async () => {
      // Given
      const { player, gameId, save } = await waitingGame();
      const batch = adminFirestore().batch();
      for (let index = 0; index < MISSIONS_PER_GAME_LIMIT; index++) {
        batch.set(game(gameId).collection("masterMissions").doc(`m${index}`), {
          ...mission(`m${index}`),
          state: "LOCKED",
          createdAt: Timestamp.now(),
        });
      }

      // When
      const byPlayer = await failureOf(player.call("saveMission", { gameId, ...mission("safe") }));
      const tooShort = await failureOf(save("safe", { answers: ["12"] }));
      await batch.commit();
      const tooMany = await failureOf(save("one-more"));

      // Then
      expect(byPlayer).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
      expect(tooShort).toEqual({ code: "functions/invalid-argument", reason: "MISSION_INVALID" });
      expect(tooMany).toEqual({ code: "functions/resource-exhausted", reason: "TOO_MANY_MISSIONS" });
    });

    test("while the game runs, a mission due already goes on at once, and a change shows to the players", async () => {
      // Given
      const { master, gameId, save } = await waitingGame();
      await save("first");
      await master.call("startGame", { gameId });

      // When
      await save("improvised", { type: "TEXT", answers: ["Lumière"] });
      await save("first", { title: "Le coffre fort", answers: ["9999"] });

      // Then
      expect((await visibleMission(gameId, "improvised"))?.state).toBe("ACTIVE");
      expect((await visibleMission(gameId, "first"))?.title).toBe("Le coffre fort");
      expect((await masterMission(gameId, "first"))?.answers).toEqual(["9999"]);
    });
  });

  describe("startGame and the missions the players see", () => {
    test("the missions of the start unlock, with the words of the mission and never its answers", async () => {
      // Given
      const { master, gameId, save } = await waitingGame();
      await save("safe");
      await save("hidden", { trigger: { type: "MANUAL" } });

      // When
      await master.call("startGame", { gameId });

      // Then
      const visible = await visibleMission(gameId, "safe");
      expect(Object.keys(visible!).sort()).toEqual(
        ["codeLength", "createdAt", "isFinal", "solvedAt", "solvedBy", "state", "statement", "title", "type", "unlockedAt"],
      );
      expect(visible).toMatchObject({ state: "ACTIVE", codeLength: 4, type: "CODE", solvedBy: null });
      expect(await visibleMission(gameId, "hidden")).toBeUndefined();
      expect((await masterMission(gameId, "hidden"))?.state).toBe("LOCKED");
    });
  });

  describe("submitAnswer", () => {
    async function runningGame() {
      const context = await waitingGame();
      await context.save("safe");
      await context.save("word", { type: "TEXT", answers: ["L’Œuvre d'été"], trigger: { type: "AFTER_MISSION", missionId: "safe" } });
      await context.master.call("startGame", { gameId: context.gameId });
      return context;
    }

    test("a wrong answer is told wrong and counts, a right one solves and unlocks what follows", async () => {
      // Given
      const { player, gameId } = await runningGame();

      // When
      const wrong = await player.call<{ correct: boolean }>("submitAnswer", { gameId, missionId: "safe", answer: "427" });
      const right = await player.call<{ correct: boolean }>("submitAnswer", { gameId, missionId: "safe", answer: "0427" });

      // Then: a code is compared as a string, its leading zero counts
      expect(wrong).toEqual({ correct: false });
      expect(right).toEqual({ correct: true });
      const solved = await visibleMission(gameId, "safe");
      const profile = (await adminFirestore().collection("users").doc(player.uid).get()).data()!;
      expect(solved).toMatchObject({ state: "SOLVED", solvedBy: { userId: player.uid, username: profile.username } });
      expect((await visibleMission(gameId, "word"))?.state).toBe("ACTIVE");
      expect((await masterMission(gameId, "safe"))?.answerWindow.count).toBe(2);
    });

    test("a text is compared without case, accents, typographic apostrophes nor extra spaces", async () => {
      // Given
      const { player, gameId } = await runningGame();
      await player.call("submitAnswer", { gameId, missionId: "safe", answer: "0427" });

      // When
      const result = await player.call<{ correct: boolean }>("submitAnswer", { gameId, missionId: "word", answer: "  l'oeuvre   D'ETE " });

      // Then
      expect(result).toEqual({ correct: true });
    });

    test("a locked mission reads as no mission, and answers wait while the game is paused or not started", async () => {
      // Given
      const { master, player, gameId, save } = await waitingGame();
      await save("safe");
      const notStarted = await failureOf(player.call("submitAnswer", { gameId, missionId: "safe", answer: "0427" }));
      await save("hidden", { trigger: { type: "MANUAL" } });
      await master.call("startGame", { gameId });

      // When
      const locked = await failureOf(player.call("submitAnswer", { gameId, missionId: "hidden", answer: "0427" }));
      await master.call("pauseGame", { gameId });
      const paused = await failureOf(player.call("submitAnswer", { gameId, missionId: "safe", answer: "0427" }));

      // Then
      expect(notStarted).toEqual({ code: "functions/failed-precondition", reason: "GAME_NOT_STARTED" });
      expect(locked).toEqual({ code: "functions/not-found", reason: "MISSION_NOT_FOUND" });
      expect(paused).toEqual({ code: "functions/failed-precondition", reason: "GAME_PAUSED" });
    });

    test("answers are limited per minute for a mission, all the players together", async () => {
      // Given
      const { player, gameId } = await runningGame();
      await game(gameId).collection("masterMissions").doc("safe").update({
        answerWindow: { startedAt: Timestamp.now(), count: ANSWERS_PER_MINUTE },
      });

      // When
      const result = await failureOf(player.call("submitAnswer", { gameId, missionId: "safe", answer: "0427" }));

      // Then
      expect(result).toEqual({ code: "functions/resource-exhausted", reason: "TOO_MANY_ANSWERS" });
    });

    test("an answer once the time is up is refused, and the game is recorded over on time", async () => {
      // Given
      const { player, gameId } = await runningGame();
      await game(gameId).update({ endsAt: Timestamp.fromMillis(Date.now() - 1000) });

      // When
      const result = await failureOf(player.call("submitAnswer", { gameId, missionId: "safe", answer: "0427" }));

      // Then
      expect(result).toEqual({ code: "functions/failed-precondition", reason: "GAME_FINISHED" });
      expect((await game(gameId).get()).data()).toMatchObject({ status: "FINISHED", result: "TIME_UP" });
    });

    test("solving every final mission wins the game", async () => {
      // Given
      const { master, player, gameId, save } = await waitingGame();
      await save("first", { isFinal: true });
      await save("second", { isFinal: true, answers: ["1111"] });
      await master.call("startGame", { gameId });

      // When
      await player.call("submitAnswer", { gameId, missionId: "first", answer: "0427" });
      const afterOne = (await game(gameId).get()).data()!.status;
      await player.call("submitAnswer", { gameId, missionId: "second", answer: "1111" });

      // Then
      expect(afterOne).toBe("RUNNING");
      expect((await game(gameId).get()).data()).toMatchObject({ status: "FINISHED", result: "WON" });
    });
  });

  describe("unlockMission, validateMission and deleteMission", () => {
    test("the game master unlocks a mission by hand, and validates one in the place of the players", async () => {
      // Given
      const { master, gameId, save } = await waitingGame();
      await save("hidden", { trigger: { type: "MANUAL" } });
      await save("other", { trigger: { type: "MANUAL" } });
      await save("next", { trigger: { type: "AFTER_MISSION", missionId: "other" } });
      await master.call("startGame", { gameId });

      // When
      await master.call("unlockMission", { gameId, missionId: "hidden" });
      await master.call("validateMission", { gameId, missionId: "other" });

      // Then: a locked mission validated is solved, and what follows unlocks
      expect((await visibleMission(gameId, "hidden"))?.state).toBe("ACTIVE");
      expect((await visibleMission(gameId, "other"))).toMatchObject({ state: "SOLVED", solvedBy: { userId: master.uid } });
      expect((await visibleMission(gameId, "next"))?.state).toBe("ACTIVE");
    });

    test("a player neither unlocks nor validates, and a game not started unlocks nothing", async () => {
      // Given
      const { master, player, gameId, save } = await waitingGame();
      await save("hidden", { trigger: { type: "MANUAL" } });

      // When
      const notStarted = await failureOf(master.call("unlockMission", { gameId, missionId: "hidden" }));
      await master.call("startGame", { gameId });
      const unlockByPlayer = await failureOf(player.call("unlockMission", { gameId, missionId: "hidden" }));
      const validateByPlayer = await failureOf(player.call("validateMission", { gameId, missionId: "hidden" }));

      // Then
      expect(notStarted).toEqual({ code: "functions/failed-precondition", reason: "GAME_NOT_STARTED" });
      expect(unlockByPlayer).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
      expect(validateByPlayer).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
    });

    test("deleting a mission frees the ones after it to be unlocked by hand, and never wins the game", async () => {
      // Given
      const { master, player, gameId, save } = await waitingGame();
      await save("first", { isFinal: true });
      await save("second", { isFinal: true, answers: ["1111"], trigger: { type: "AFTER_MISSION", missionId: "first" } });
      await save("third", { trigger: { type: "AFTER_MISSION", missionId: "second" } });
      await master.call("startGame", { gameId });
      await player.call("submitAnswer", { gameId, missionId: "first", answer: "0427" });

      // When
      await master.call("deleteMission", { gameId, missionId: "second" });
      await master.call("deleteMission", { gameId, missionId: "second" });

      // Then
      expect(await masterMission(gameId, "second")).toBeUndefined();
      expect(await visibleMission(gameId, "second")).toBeUndefined();
      expect((await masterMission(gameId, "third"))?.trigger).toEqual({ type: "MANUAL" });
      expect((await game(gameId).get()).data()!.status).toBe("RUNNING");
    });
  });
});

describe("mission values", () => {
  test("a text is normalized: case, accents, ligatures, apostrophes, punctuation and spaces", () => {
    expect(normalizeText("  L’Œuvre  d'Été !")).toBe("l'oeuvre d'ete");
    expect(normalizeText("Ça, c'est « LÀ »")).toBe("ca c'est la");
  });

  test("a code is compared exactly, as a string", () => {
    expect(isCorrectAnswer({ type: "CODE", answers: ["0427"] }, "0427")).toBe(true);
    expect(isCorrectAnswer({ type: "CODE", answers: ["0427"] }, "427")).toBe(false);
    expect(isCorrectAnswer({ type: "TEXT", answers: ["Clé"] }, "   ")).toBe(false);
  });

  test("a mission has a title, codes of one length of 3 to 8 digits, and a trigger without a circle", () => {
    const triggers = new Map<string, { type: "MANUAL" } | { type: "AFTER_MISSION"; missionId: string }>([
      ["a", { type: "AFTER_MISSION", missionId: "b" }],
      ["b", { type: "MANUAL" }],
    ]);
    const base = { title: "Coffre", type: "CODE", answers: ["123"], trigger: { type: "MANUAL" }, isFinal: false };
    expect(validateMission(base, "c", triggers).codeLength).toBe(3);
    expect(() => validateMission({ ...base, title: " " }, "c", triggers)).toThrow();
    expect(() => validateMission({ ...base, answers: ["123", "1234"] }, "c", triggers)).toThrow();
    expect(() => validateMission({ ...base, answers: ["12a"] }, "c", triggers)).toThrow();
    expect(() => validateMission({ ...base, answers: ["123456789"] }, "c", triggers)).toThrow();
    expect(() => validateMission({ ...base, trigger: { type: "AFTER_MISSION", missionId: "zzz" } }, "c", triggers)).toThrow();
    // b after a, while a is after b: a circle nobody unlocks
    expect(() => validateMission({ ...base, trigger: { type: "AFTER_MISSION", missionId: "a" } }, "b", triggers)).toThrow();
  });
});
