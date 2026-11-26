import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireExistingAccount, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { db, GAMES, JOIN_ATTEMPTS, JOIN_CODES, PLAYERS } from "../core/firestore";
import { hasRoomLeft, JOIN_FAILURES_LIMIT, JOIN_FAILURES_WINDOW_MINUTES, validateJoinCode } from "./game";
import { accessOf, readMember, userGameData, userGameOf } from "./gameData";

export interface JoinGameResponse {
  gameId: string;
}

/**
 * Lets the user who calls into the waiting game of a code, and returns its id. Safe to call again:
 * a member of the game, its game master too, gets its id without any change.
 */
export const joinGame = onCall(async (request): Promise<JoinGameResponse> => {
  const uid = requireUid(request);
  const joinCode = validateJoinCode(requestData(request).code);
  await requireExistingAccount(uid);
  await requireAttemptsLeft(uid);

  const code = await db().collection(JOIN_CODES).doc(joinCode).get();
  if (!code.exists) {
    await recordFailure(uid);
    throw failure("not-found", "GAME_NOT_FOUND", "No game has this code");
  }

  const game = db().collection(GAMES).doc(code.data()!.gameId as string);
  await db().runTransaction(async (transaction) => {
    const snapshot = await transaction.get(game);
    const access = await transaction.get(accessOf(game));
    // Reads come before writes in a transaction: the profile too
    const player = await readMember(uid, transaction);
    if (!snapshot.exists) {
      throw failure("not-found", "GAME_NOT_FOUND", "The game of this code was deleted");
    }
    const data = snapshot.data()!;
    if ((data.memberIds as string[]).includes(uid)) {
      return;
    }
    if (((access.data()?.excludedIds as string[] | undefined) ?? []).includes(uid)) {
      throw failure("permission-denied", "PLAYER_EXCLUDED", "The game master excluded this user");
    }
    if (data.status !== "WAITING") {
      throw failure("failed-precondition", "GAME_NOT_JOINABLE", "The game is no longer waiting for players");
    }
    if (!hasRoomLeft(data)) {
      throw failure("failed-precondition", "GAME_FULL", "The game has no room left");
    }
    transaction.create(game.collection(PLAYERS).doc(uid), { ...player, joinedAt: FieldValue.serverTimestamp() });
    transaction.set(userGameOf(uid, game.id), userGameData(data, false));
    transaction.update(game, {
      memberIds: FieldValue.arrayUnion(uid),
      playerCount: FieldValue.increment(1),
      updatedAt: FieldValue.serverTimestamp(),
    });
  });

  return { gameId: game.id };
});

function windowStart(): Timestamp {
  return Timestamp.fromMillis(Date.now() - JOIN_FAILURES_WINDOW_MINUTES * 60 * 1000);
}

/** Refuses a user who tried too many unknown codes lately: a code is not to be guessed. */
async function requireAttemptsLeft(uid: string): Promise<void> {
  const attempts = (await db().collection(JOIN_ATTEMPTS).doc(uid).get()).data();
  if (
    attempts &&
    (attempts.windowStartedAt as Timestamp).toMillis() > windowStart().toMillis() &&
    (attempts.failures as number) >= JOIN_FAILURES_LIMIT
  ) {
    throw failure("resource-exhausted", "TOO_MANY_ATTEMPTS", "Too many unknown codes, try again later");
  }
}

/** Counts an unknown code, in a window that starts again once it is over. */
async function recordFailure(uid: string): Promise<void> {
  const reference = db().collection(JOIN_ATTEMPTS).doc(uid);
  await db().runTransaction(async (transaction) => {
    const attempts = (await transaction.get(reference)).data();
    if (attempts && (attempts.windowStartedAt as Timestamp).toMillis() > windowStart().toMillis()) {
      transaction.update(reference, { failures: FieldValue.increment(1) });
    } else {
      transaction.set(reference, { failures: 1, windowStartedAt: Timestamp.now() });
    }
  });
}
