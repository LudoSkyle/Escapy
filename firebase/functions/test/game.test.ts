import { signInAnonymously } from "firebase/auth";
import { Timestamp } from "firebase-admin/firestore";
import { afterEach, beforeEach, describe, expect, test } from "vitest";
import {
  JOIN_CODE_ALPHABET,
  JOIN_FAILURES_LIMIT,
  randomJoinCode,
  validateAddedMinutes,
  validateDurationMinutes,
  validateGameTitle,
  validateJoinCode,
  validateMaxPlayers,
} from "../src/game/game";
import { isTimeUp, playedMsOf, remainingMsOf } from "../src/game/timer";
import { adminFirestore, clearEmulators, failureOf, newClient, TestClient } from "./support/emulators";

interface Created {
  gameId: string;
  joinCode: string;
}

const NEW_GAME = { title: "Le manoir", durationMinutes: 60 };

/** A signed-in user with a profile, as the app has one before it reaches the home screen. */
async function newUser(): Promise<TestClient & { uid: string }> {
  const client = newClient();
  const { user } = await signInAnonymously(client.auth);
  await client.call("ensureProfile");
  return Object.assign(client, { uid: user.uid });
}

function game(gameId: string) {
  return adminFirestore().collection("games").doc(gameId);
}

/** The entry of a game in the list of a user, for their home screen. */
async function userGame(uid: string, gameId: string) {
  return (await adminFirestore().collection("users").doc(uid).collection("games").doc(gameId).get()).data();
}

describe("games", () => {
  let clients: TestClient[] = [];

  async function user() {
    const created = await newUser();
    clients.push(created);
    return created;
  }

  beforeEach(async () => {
    await clearEmulators();
    clients = [];
  });

  afterEach(async () => {
    await Promise.all(clients.map((client) => client.close()));
  });

  describe("createGame", () => {
    test("creates a waiting game run by the user, with a code that points to it", async () => {
      // Given
      const master = await user();

      // When
      const created = await master.call<Created>("createGame", { ...NEW_GAME, maxPlayers: 4 });

      // Then
      expect(created.joinCode).toMatch(new RegExp(`^[${JOIN_CODE_ALPHABET}]{6}$`));
      const stored = (await game(created.gameId).get()).data()!;
      const profile = (await adminFirestore().collection("users").doc(master.uid).get()).data()!;
      expect(stored).toMatchObject({
        title: "Le manoir",
        durationMinutes: 60,
        maxPlayers: 4,
        status: "WAITING",
        masterId: master.uid,
        master: { username: profile.username, avatarId: profile.avatarId },
        memberIds: [master.uid],
        playerCount: 0,
        joinCode: created.joinCode,
      });
      const code = (await adminFirestore().collection("joinCodes").doc(created.joinCode).get()).data();
      expect(code?.gameId).toBe(created.gameId);
      expect(await userGame(master.uid, created.gameId)).toMatchObject({
        title: "Le manoir",
        isMaster: true,
        master: { username: profile.username, avatarId: profile.avatarId },
      });
    });

    test("cleans the title and leaves the game without a maximum by default", async () => {
      // Given
      const master = await user();

      // When
      const created = await master.call<Created>("createGame", { title: "  Le   manoir ", durationMinutes: 90 });

      // Then
      expect((await game(created.gameId).get()).data()).toMatchObject({ title: "Le manoir", maxPlayers: null });
    });

    test("refuses a title, a duration or a maximum out of bounds", async () => {
      // Given
      const master = await user();

      // When / Then
      expect(await failureOf(master.call("createGame", { ...NEW_GAME, title: "Ab" }))).toEqual({
        code: "functions/invalid-argument",
        reason: "GAME_TITLE_INVALID",
      });
      expect(await failureOf(master.call("createGame", { ...NEW_GAME, durationMinutes: 62 }))).toEqual({
        code: "functions/invalid-argument",
        reason: "GAME_DURATION_INVALID",
      });
      expect(await failureOf(master.call("createGame", { ...NEW_GAME, maxPlayers: 21 }))).toEqual({
        code: "functions/invalid-argument",
        reason: "MAX_PLAYERS_INVALID",
      });
    });

    test("refuses a user without a profile", async () => {
      // Given
      const client = newClient();
      clients.push(client);
      await signInAnonymously(client.auth);

      // When
      const result = await failureOf(client.call("createGame", NEW_GAME));

      // Then
      expect(result).toEqual({ code: "functions/not-found", reason: "PROFILE_NOT_FOUND" });
    });

    test("refuses a sixth game run at once", async () => {
      // Given
      const master = await user();
      for (let index = 0; index < 5; index++) {
        await master.call("createGame", NEW_GAME);
      }

      // When
      const result = await failureOf(master.call("createGame", NEW_GAME));

      // Then
      expect(result).toEqual({ code: "functions/resource-exhausted", reason: "TOO_MANY_GAMES" });
    });

    test("refuses a call without a signed-in user", async () => {
      // Given
      const client = newClient();
      clients.push(client);

      // When
      const result = await failureOf(client.call("createGame", NEW_GAME));

      // Then
      expect(result).toEqual({ code: "functions/unauthenticated", reason: "UNAUTHENTICATED" });
    });
  });

  describe("joinGame", () => {
    test("lets a user in by the code, typed in lower case with spaces", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      const typed = ` ${created.joinCode.slice(0, 3)} ${created.joinCode.slice(3)} `.toLowerCase();

      // When
      const joined = await player.call<{ gameId: string }>("joinGame", { code: typed });

      // Then
      expect(joined.gameId).toBe(created.gameId);
      const stored = (await game(created.gameId).get()).data()!;
      expect(stored.memberIds).toEqual([master.uid, player.uid]);
      expect(stored.playerCount).toBe(1);
      const profile = (await adminFirestore().collection("users").doc(player.uid).get()).data()!;
      const playerDocument = (await game(created.gameId).collection("players").doc(player.uid).get()).data();
      expect(playerDocument).toMatchObject({ username: profile.username, avatarId: profile.avatarId });
      expect(playerDocument?.joinedAt).toBeInstanceOf(Timestamp);
      const entry = await userGame(player.uid, created.gameId);
      expect(entry).toMatchObject({ title: "Le manoir", isMaster: false, master: stored.master });
      expect(entry?.createdAt).toEqual(stored.createdAt);
    });

    test("changes nothing for a member who joins again, nor for the game master", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });

      // When
      await player.call("joinGame", { code: created.joinCode });
      const masterJoined = await master.call<{ gameId: string }>("joinGame", { code: created.joinCode });

      // Then
      expect(masterJoined.gameId).toBe(created.gameId);
      expect((await game(created.gameId).get()).data()).toMatchObject({
        memberIds: [master.uid, player.uid],
        playerCount: 1,
      });
    });

    test("refuses an unknown code, and a code that cannot be one", async () => {
      // Given
      const player = await user();

      // When / Then
      expect(await failureOf(player.call("joinGame", { code: "ABCDEF" }))).toEqual({
        code: "functions/not-found",
        reason: "GAME_NOT_FOUND",
      });
      expect(await failureOf(player.call("joinGame", { code: "ABC0" }))).toEqual({
        code: "functions/invalid-argument",
        reason: "JOIN_CODE_INVALID",
      });
    });

    test("refuses a full game", async () => {
      // Given
      const master = await user();
      const created = await master.call<Created>("createGame", { ...NEW_GAME, maxPlayers: 2 });
      await (await user()).call("joinGame", { code: created.joinCode });
      await (await user()).call("joinGame", { code: created.joinCode });

      // When
      const result = await failureOf((await user()).call("joinGame", { code: created.joinCode }));

      // Then
      expect(result).toEqual({ code: "functions/failed-precondition", reason: "GAME_FULL" });
    });

    test("refuses anyone once too many unknown codes were tried, even a right one", async () => {
      // Given
      const master = await user();
      const guesser = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      for (let index = 0; index < JOIN_FAILURES_LIMIT; index++) {
        await failureOf(guesser.call("joinGame", { code: "ABCDEF" }));
      }

      // When
      const result = await failureOf(guesser.call("joinGame", { code: created.joinCode }));

      // Then
      expect(result).toEqual({ code: "functions/resource-exhausted", reason: "TOO_MANY_ATTEMPTS" });
    });

    test("refuses a call without a signed-in user", async () => {
      // Given
      const client = newClient();
      clients.push(client);

      // When
      const result = await failureOf(client.call("joinGame", { code: "ABCDEF" }));

      // Then
      expect(result).toEqual({ code: "functions/unauthenticated", reason: "UNAUTHENTICATED" });
    });
  });

  describe("leaveGame", () => {
    test("takes a player out of the game", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });

      // When
      await player.call("leaveGame", { gameId: created.gameId });

      // Then
      expect((await game(created.gameId).get()).data()).toMatchObject({ memberIds: [master.uid], playerCount: 0 });
      expect((await game(created.gameId).collection("players").doc(player.uid).get()).exists).toBe(false);
      expect(await userGame(player.uid, created.gameId)).toBeUndefined();
    });

    test("is no error for a game already left, or deleted", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);

      // When
      const notIn = await player.call("leaveGame", { gameId: created.gameId });
      const deleted = await player.call("leaveGame", { gameId: "missing" });

      // Then
      expect(notIn).toEqual({ left: true });
      expect(deleted).toEqual({ left: true });
      expect((await game(created.gameId).get()).data()?.playerCount).toBe(0);
    });

    test("refuses the game master, who cancels the game instead", async () => {
      // Given
      const master = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);

      // When
      const result = await failureOf(master.call("leaveGame", { gameId: created.gameId }));

      // Then
      expect(result).toEqual({ code: "functions/failed-precondition", reason: "GAME_MASTER_CANNOT_LEAVE" });
    });
  });

  describe("kickPlayer", () => {
    test("takes a player out, and the code no longer lets them in", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });

      // When
      await master.call("kickPlayer", { gameId: created.gameId, playerId: player.uid });

      // Then
      expect((await game(created.gameId).get()).data()).toMatchObject({ memberIds: [master.uid], playerCount: 0 });
      expect(await userGame(player.uid, created.gameId)).toBeUndefined();
      expect(await failureOf(player.call("joinGame", { code: created.joinCode }))).toEqual({
        code: "functions/permission-denied",
        reason: "PLAYER_EXCLUDED",
      });
    });

    test("refuses a player who is not the game master", async () => {
      // Given
      const master = await user();
      const player = await user();
      const other = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });
      await other.call("joinGame", { code: created.joinCode });

      // When
      const result = await failureOf(player.call("kickPlayer", { gameId: created.gameId, playerId: other.uid }));

      // Then
      expect(result).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
      expect((await game(created.gameId).get()).data()?.playerCount).toBe(2);
    });

    test("refuses the game master excluding themself", async () => {
      // Given
      const master = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);

      // When
      const result = await failureOf(master.call("kickPlayer", { gameId: created.gameId, playerId: master.uid }));

      // Then
      expect(result).toEqual({ code: "functions/invalid-argument", reason: "INVALID_REQUEST" });
    });
  });

  describe("cancelGame", () => {
    test("deletes the game, its players and its code", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });

      // When
      await master.call("cancelGame", { gameId: created.gameId });

      // Then
      expect((await game(created.gameId).get()).exists).toBe(false);
      expect((await game(created.gameId).collection("players").get()).empty).toBe(true);
      expect((await adminFirestore().collection("joinCodes").doc(created.joinCode).get()).exists).toBe(false);
      expect(await userGame(master.uid, created.gameId)).toBeUndefined();
      expect(await userGame(player.uid, created.gameId)).toBeUndefined();
      expect(await failureOf(player.call("joinGame", { code: created.joinCode }))).toEqual({
        code: "functions/not-found",
        reason: "GAME_NOT_FOUND",
      });
    });

    test("leaves no entry behind when a player joins while the game is cancelled", async () => {
      for (let attempt = 0; attempt < 5; attempt++) {
        // Given
        const master = await user();
        const player = await user();
        const created = await master.call<Created>("createGame", NEW_GAME);

        // When
        await Promise.allSettled([
          master.call("cancelGame", { gameId: created.gameId }),
          player.call("joinGame", { code: created.joinCode }),
        ]);

        // Then
        expect((await game(created.gameId).get()).exists).toBe(false);
        expect(await userGame(player.uid, created.gameId)).toBeUndefined();
      }
    });

    test("refuses a player, and is no error once the game is deleted", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });

      // When
      const refused = await failureOf(player.call("cancelGame", { gameId: created.gameId }));
      await master.call("cancelGame", { gameId: created.gameId });
      const again = await master.call("cancelGame", { gameId: created.gameId });

      // Then
      expect(refused).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
      expect(again).toEqual({ cancelled: true });
    });
  });

  describe("deleteStaleGames", () => {
    test("deletes the waiting games created before the limit, with their codes, and keeps the others", async () => {
      // Given
      const master = await user();
      const old = await master.call<Created>("createGame", NEW_GAME);
      const recent = await master.call<Created>("createGame", NEW_GAME);
      await game(old.gameId).update({ createdAt: Timestamp.fromMillis(Date.now() - 25 * 60 * 60 * 1000) });
      const { deleteGamesWaitingSince } = await import("../src/game/deleteStaleGames");

      // When
      const deleted = await deleteGamesWaitingSince(Timestamp.fromMillis(Date.now() - 24 * 60 * 60 * 1000));

      // Then
      expect(deleted).toBe(1);
      expect((await game(old.gameId).get()).exists).toBe(false);
      expect((await adminFirestore().collection("joinCodes").doc(old.joinCode).get()).exists).toBe(false);
      expect((await game(recent.gameId).get()).exists).toBe(true);
    });

    test("deletes the started games untouched since the limit, and keeps a waiting game and a recent one", async () => {
      // Given
      const master = await user();
      const finished = await master.call<Created>("createGame", NEW_GAME);
      await master.call("startGame", { gameId: finished.gameId });
      await master.call("endGame", { gameId: finished.gameId });
      const running = await master.call<Created>("createGame", NEW_GAME);
      await master.call("startGame", { gameId: running.gameId });
      const waiting = await master.call<Created>("createGame", NEW_GAME);
      const longAgo = Timestamp.fromMillis(Date.now() - 25 * 60 * 60 * 1000);
      await game(finished.gameId).update({ updatedAt: longAgo });
      await game(waiting.gameId).update({ updatedAt: longAgo });
      const { deleteGamesStartedAndIdleSince } = await import("../src/game/deleteStaleGames");

      // When
      const deleted = await deleteGamesStartedAndIdleSince(Timestamp.fromMillis(Date.now() - 24 * 60 * 60 * 1000));

      // Then
      expect(deleted).toBe(1);
      expect((await game(finished.gameId).get()).exists).toBe(false);
      expect((await game(running.gameId).get()).exists).toBe(true);
      expect((await game(waiting.gameId).get()).exists).toBe(true);
    });
  });

  /** A game with one player, started by its game master. */
  async function startedGame() {
    const master = await user();
    const player = await user();
    const created = await master.call<Created>("createGame", NEW_GAME);
    await player.call("joinGame", { code: created.joinCode });
    await master.call("startGame", { gameId: created.gameId });
    return { master, player, gameId: created.gameId };
  }

  /** Moves the end of a running game to [msFromNow], as if its time had gone by. */
  async function endIn(gameId: string, msFromNow: number) {
    await game(gameId).update({ endsAt: Timestamp.fromMillis(Date.now() + msFromNow) });
  }

  async function stored(gameId: string) {
    return (await game(gameId).get()).data()!;
  }

  describe("startGame", () => {
    test("starts the timer from the duration, for the game and the lists of its members", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });

      // When
      const before = Date.now();
      const response = await master.call<{ status: string; remainingMs: number }>("startGame", { gameId: created.gameId });

      // Then
      const data = await stored(created.gameId);
      expect(data).toMatchObject({ status: "RUNNING", addedMinutes: 0, remainingMs: null });
      expect(data.startedAt).toBeInstanceOf(Timestamp);
      expect((data.endsAt as Timestamp).toMillis()).toBeGreaterThanOrEqual(before + 60 * 60 * 1000);
      expect(response.status).toBe("RUNNING");
      expect(response.remainingMs).toBeGreaterThan(59 * 60 * 1000);
      expect((await userGame(master.uid, created.gameId))?.status).toBe("RUNNING");
      expect((await userGame(player.uid, created.gameId))?.status).toBe("RUNNING");
    });

    test("starts a game without any player, and leaves a started game as it is when called again", async () => {
      // Given
      const master = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await master.call("startGame", { gameId: created.gameId });
      const endsAt = (await stored(created.gameId)).endsAt as Timestamp;

      // When
      await master.call("startGame", { gameId: created.gameId });

      // Then
      expect(((await stored(created.gameId)).endsAt as Timestamp).toMillis()).toBe(endsAt.toMillis());
    });

    test("refuses a player, and a started game no longer takes anyone", async () => {
      // Given
      const { player, gameId } = await startedGame();
      const code = (await stored(gameId)).joinCode as string;

      // When
      const refused = await failureOf(player.call("startGame", { gameId }));
      const late = await failureOf((await user()).call("joinGame", { code }));

      // Then
      expect(refused).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
      expect(late).toEqual({ code: "functions/failed-precondition", reason: "GAME_NOT_JOINABLE" });
    });
  });

  describe("pauseGame and resumeGame", () => {
    test("pausing keeps the time left, resuming counts down from it", async () => {
      // Given
      const { master, gameId } = await startedGame();
      await endIn(gameId, 10 * 60 * 1000);

      // When
      await master.call("pauseGame", { gameId });
      const paused = await stored(gameId);
      await game(gameId).update({ remainingMs: 4 * 60 * 1000 });
      const before = Date.now();
      await master.call("resumeGame", { gameId });

      // Then
      expect(paused.status).toBe("PAUSED");
      expect(paused.endsAt).toBeNull();
      expect(paused.remainingMs).toBeGreaterThan(9 * 60 * 1000);
      expect(paused.remainingMs).toBeLessThanOrEqual(10 * 60 * 1000);
      const resumed = await stored(gameId);
      expect(resumed).toMatchObject({ status: "RUNNING", remainingMs: null });
      expect((resumed.endsAt as Timestamp).toMillis()).toBeGreaterThanOrEqual(before + 4 * 60 * 1000);
      expect((resumed.endsAt as Timestamp).toMillis()).toBeLessThanOrEqual(Date.now() + 4 * 60 * 1000);
    });

    test("pausing twice or resuming twice changes nothing more", async () => {
      // Given
      const { master, gameId } = await startedGame();
      await master.call("pauseGame", { gameId });
      const paused = await stored(gameId);

      // When
      await master.call("pauseGame", { gameId });
      const pausedAgain = await stored(gameId);
      await master.call("resumeGame", { gameId });
      const resumed = await stored(gameId);
      await master.call("resumeGame", { gameId });

      // Then
      expect(pausedAgain.remainingMs).toBe(paused.remainingMs);
      expect(((await stored(gameId)).endsAt as Timestamp).toMillis()).toBe((resumed.endsAt as Timestamp).toMillis());
    });

    test("pausing a game whose time is up finishes it on time instead", async () => {
      // Given
      const { master, gameId } = await startedGame();
      await endIn(gameId, -1000);

      // When
      const response = await master.call<{ status: string }>("pauseGame", { gameId });

      // Then
      expect(response.status).toBe("FINISHED");
      expect(await stored(gameId)).toMatchObject({ status: "FINISHED", result: "TIME_UP", remainingMs: 0 });
    });

    test("refuses a game not started yet, and a player", async () => {
      // Given
      const master = await user();
      const waiting = await master.call<Created>("createGame", NEW_GAME);
      const { player, gameId } = await startedGame();

      // When
      const notStarted = await failureOf(master.call("pauseGame", { gameId: waiting.gameId }));
      const byPlayer = await failureOf(player.call("pauseGame", { gameId }));

      // Then
      expect(notStarted).toEqual({ code: "functions/failed-precondition", reason: "GAME_NOT_STARTED" });
      expect(byPlayer).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
    });
  });

  describe("addTime", () => {
    test("adds minutes to a running game and to a paused one", async () => {
      // Given
      const { master, gameId } = await startedGame();
      const endsAt = ((await stored(gameId)).endsAt as Timestamp).toMillis();

      // When
      const running = await master.call<{ addedMinutes: number }>("addTime", { gameId, minutes: 5, addedMinutes: 0 });
      await master.call("pauseGame", { gameId });
      const remainingMs = (await stored(gameId)).remainingMs as number;
      const paused = await master.call<{ addedMinutes: number; remainingMs: number }>("addTime", {
        gameId,
        minutes: 10,
        addedMinutes: 5,
      });

      // Then
      expect(running.addedMinutes).toBe(5);
      expect(paused).toEqual({ addedMinutes: 15, remainingMs: remainingMs + 10 * 60 * 1000 });
      const data = await stored(gameId);
      expect(data.addedMinutes).toBe(15);
      expect(data.remainingMs).toBe(remainingMs + 10 * 60 * 1000);
      expect(remainingMs).toBeGreaterThan(endsAt + 5 * 60 * 1000 - Date.now() - 1000);
    });

    test("adds nothing twice when the call is sent again", async () => {
      // Given
      const { master, gameId } = await startedGame();
      await master.call("addTime", { gameId, minutes: 5, addedMinutes: 0 });

      // When
      const again = await master.call<{ addedMinutes: number }>("addTime", { gameId, minutes: 5, addedMinutes: 0 });

      // Then
      expect(again.addedMinutes).toBe(5);
      expect((await stored(gameId)).addedMinutes).toBe(5);
    });

    test("refuses minutes out of bounds, and more than 180 in all", async () => {
      // Given
      const { master, gameId } = await startedGame();
      await game(gameId).update({ addedMinutes: 150 });

      // When
      const zero = await failureOf(master.call("addTime", { gameId, minutes: 0, addedMinutes: 150 }));
      const tooMany = await failureOf(master.call("addTime", { gameId, minutes: 31, addedMinutes: 150 }));

      // Then
      expect(zero).toEqual({ code: "functions/invalid-argument", reason: "ADDED_TIME_INVALID" });
      expect(tooMany).toEqual({ code: "functions/invalid-argument", reason: "ADDED_TIME_INVALID" });
    });

    test("refuses a game whose time is up, and finishes it on time", async () => {
      // Given
      const { master, gameId } = await startedGame();
      await endIn(gameId, -1000);

      // When
      const result = await failureOf(master.call("addTime", { gameId, minutes: 5, addedMinutes: 0 }));

      // Then
      expect(result).toEqual({ code: "functions/failed-precondition", reason: "GAME_FINISHED" });
      expect(await stored(gameId)).toMatchObject({ status: "FINISHED", result: "TIME_UP", addedMinutes: 0 });
    });
  });

  describe("endGame and expireGame", () => {
    test("the game master ends the game, with the time played pauses apart", async () => {
      // Given
      const { master, player, gameId } = await startedGame();
      await master.call("addTime", { gameId, minutes: 5, addedMinutes: 0 });
      await master.call("pauseGame", { gameId });
      await game(gameId).update({ remainingMs: 20 * 60 * 1000 });

      // When
      await master.call("endGame", { gameId });

      // Then
      const data = await stored(gameId);
      expect(data).toMatchObject({
        status: "FINISHED",
        result: "ENDED_BY_MASTER",
        endsAt: null,
        remainingMs: 20 * 60 * 1000,
        playedMs: 45 * 60 * 1000,
      });
      expect(data.finishedAt).toBeInstanceOf(Timestamp);
      expect((await userGame(player.uid, gameId))?.status).toBe("FINISHED");
    });

    test("a player records a time that is up, and only once it is up", async () => {
      // Given
      const { player, gameId } = await startedGame();
      await endIn(gameId, 60 * 1000);

      // When
      const early = await player.call<{ status: string; remainingMs: number }>("expireGame", { gameId });
      await endIn(gameId, -1000);
      const late = await player.call<{ status: string; remainingMs: number }>("expireGame", { gameId });
      const again = await player.call<{ status: string }>("expireGame", { gameId });

      // Then
      expect(early.status).toBe("RUNNING");
      expect(early.remainingMs).toBeGreaterThan(50 * 1000);
      expect(late).toEqual({ status: "FINISHED", remainingMs: 0 });
      expect(again.status).toBe("FINISHED");
      expect(await stored(gameId)).toMatchObject({ result: "TIME_UP", playedMs: 60 * 60 * 1000 });
    });

    test("ending a game whose time is up keeps it over on time, and ending it again changes nothing", async () => {
      // Given
      const { master, gameId } = await startedGame();
      await endIn(gameId, -1000);

      // When
      await master.call("endGame", { gameId });
      await master.call("endGame", { gameId });

      // Then
      expect((await stored(gameId)).result).toBe("TIME_UP");
    });

    test("refuses a user who is not a member, and a player ending the game", async () => {
      // Given
      const { player, gameId } = await startedGame();
      const stranger = await user();

      // When
      const byStranger = await failureOf(stranger.call("expireGame", { gameId }));
      const byPlayer = await failureOf(player.call("endGame", { gameId }));

      // Then
      expect(byStranger).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MEMBER" });
      expect(byPlayer).toEqual({ code: "functions/permission-denied", reason: "NOT_GAME_MASTER" });
    });

    test("a finished game no longer counts towards the games run at once", async () => {
      // Given
      const master = await user();
      const games: Created[] = [];
      for (let index = 0; index < 5; index++) {
        games.push(await master.call<Created>("createGame", NEW_GAME));
      }
      await master.call("startGame", { gameId: games[0].gameId });
      await master.call("endGame", { gameId: games[0].gameId });

      // When
      const created = await master.call<Created>("createGame", NEW_GAME);

      // Then
      expect(created.gameId).toBeTruthy();
    });
  });

  describe("getServerTime", () => {
    test("tells the time of the server", async () => {
      // Given
      const client = await user();

      // When
      const before = Date.now();
      const { nowMs } = await client.call<{ nowMs: number }>("getServerTime");

      // Then
      expect(nowMs).toBeGreaterThanOrEqual(before);
      expect(nowMs).toBeLessThanOrEqual(Date.now());
    });
  });

  describe("with the account and the profile", () => {
    test("deleting an account deletes the games the user runs and takes them out of the others", async () => {
      // Given
      const master = await user();
      const player = await user();
      const own = await player.call<Created>("createGame", NEW_GAME);
      const other = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: other.joinCode });

      // When
      await player.call("deleteAccount");

      // Then
      expect((await game(own.gameId).get()).exists).toBe(false);
      expect((await adminFirestore().collection("joinCodes").doc(own.joinCode).get()).exists).toBe(false);
      expect((await game(other.gameId).get()).data()).toMatchObject({ memberIds: [master.uid], playerCount: 0 });
      expect((await adminFirestore().collection("users").doc(player.uid).collection("games").get()).empty).toBe(true);
    });

    test("a new name or avatar shows in the games of the user", async () => {
      // Given
      const master = await user();
      const player = await user();
      const created = await master.call<Created>("createGame", NEW_GAME);
      await player.call("joinGame", { code: created.joinCode });

      // When
      await master.call("updateProfile", { username: "Maître", avatarId: 7 });
      await player.call("updateProfile", { username: "Joueuse", avatarId: 8 });

      // Then
      expect((await game(created.gameId).get()).data()?.master).toEqual({ username: "Maître", avatarId: 7 });
      expect((await userGame(master.uid, created.gameId))?.master).toEqual({ username: "Maître", avatarId: 7 });
      expect((await userGame(player.uid, created.gameId))?.master).toEqual({ username: "Maître", avatarId: 7 });
      expect((await game(created.gameId).collection("players").doc(player.uid).get()).data()).toMatchObject({
        username: "Joueuse",
        avatarId: 8,
      });
    });
  });
});

describe("game values", () => {
  test("a random code takes six characters of the alphabet", () => {
    for (let index = 0; index < 100; index++) {
      expect(randomJoinCode()).toMatch(new RegExp(`^[${JOIN_CODE_ALPHABET}]{6}$`));
    }
  });

  test("a code is read without its case and its spaces, and refuses the ambiguous characters", () => {
    expect(validateJoinCode(" ab c2 3d ")).toBe("ABC23D");
    expect(() => validateJoinCode("ABCDE0")).toThrow();
    expect(() => validateJoinCode("ABCDEI")).toThrow();
    expect(() => validateJoinCode("ABCDE")).toThrow();
    expect(() => validateJoinCode(123456)).toThrow();
  });

  test("a title has 3 to 40 characters, without a control character", () => {
    expect(validateGameTitle("Le  manoir hanté")).toBe("Le manoir hanté");
    expect(() => validateGameTitle("Ab")).toThrow();
    expect(() => validateGameTitle("a".repeat(41))).toThrow();
    expect(() => validateGameTitle("Le\u0000manoir")).toThrow();
  });

  test("a duration goes from 15 to 180 minutes, by 5", () => {
    expect(validateDurationMinutes(15)).toBe(15);
    expect(validateDurationMinutes(180)).toBe(180);
    expect(() => validateDurationMinutes(10)).toThrow();
    expect(() => validateDurationMinutes(185)).toThrow();
    expect(() => validateDurationMinutes(61)).toThrow();
    expect(() => validateDurationMinutes(60.5)).toThrow();
  });

  test("a maximum is absent, or from 2 to 20 players", () => {
    expect(validateMaxPlayers(undefined)).toBeNull();
    expect(validateMaxPlayers(null)).toBeNull();
    expect(validateMaxPlayers(2)).toBe(2);
    expect(validateMaxPlayers(20)).toBe(20);
    expect(() => validateMaxPlayers(1)).toThrow();
    expect(() => validateMaxPlayers(21)).toThrow();
  });

  test("added time goes from 1 to 60 minutes at once, 180 in all", () => {
    expect(validateAddedMinutes(1, 0)).toBe(1);
    expect(validateAddedMinutes(60, 120)).toBe(60);
    expect(() => validateAddedMinutes(0, 0)).toThrow();
    expect(() => validateAddedMinutes(61, 0)).toThrow();
    expect(() => validateAddedMinutes(5, 176)).toThrow();
    expect(() => validateAddedMinutes(2.5, 0)).toThrow();
  });

  test("the time left counts down while running, stays still while paused, and is none otherwise", () => {
    const nowMs = 1_000_000;
    expect(remainingMsOf({ status: "RUNNING", endsAt: Timestamp.fromMillis(nowMs + 5000) }, nowMs)).toBe(5000);
    expect(remainingMsOf({ status: "RUNNING", endsAt: Timestamp.fromMillis(nowMs - 5000) }, nowMs)).toBe(0);
    expect(remainingMsOf({ status: "PAUSED", remainingMs: 7000 }, nowMs)).toBe(7000);
    expect(remainingMsOf({ status: "WAITING" }, nowMs)).toBe(0);
    expect(isTimeUp({ status: "RUNNING", endsAt: Timestamp.fromMillis(nowMs) }, nowMs)).toBe(true);
    expect(isTimeUp({ status: "PAUSED", remainingMs: 0 }, nowMs)).toBe(false);
  });

  test("the time played is the time given, added time included, less the time left", () => {
    expect(playedMsOf({ durationMinutes: 60, addedMinutes: 5 }, 10 * 60 * 1000)).toBe(55 * 60 * 1000);
    expect(playedMsOf({ durationMinutes: 30 }, 0)).toBe(30 * 60 * 1000);
  });
});
