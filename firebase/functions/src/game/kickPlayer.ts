import { FieldValue } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { db, GAMES } from "../core/firestore";
import { validateGameId } from "./game";
import { accessOf, removePlayer } from "./gameData";

/**
 * Excludes a player from a game, for its game master only: the player leaves it, and its code no
 * longer lets them in. Safe to call again.
 */
export const kickPlayer = onCall(async (request): Promise<{ kicked: true }> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const game = db().collection(GAMES).doc(validateGameId(data.gameId));
  const playerId = data.playerId;
  if (typeof playerId !== "string" || playerId.length === 0 || playerId === uid) {
    throw failure("invalid-argument", "INVALID_REQUEST", "The player id must be another user");
  }

  await db().runTransaction(async (transaction) => {
    const snapshot = await transaction.get(game);
    if (!snapshot.exists) {
      throw failure("not-found", "GAME_NOT_FOUND", "The game was deleted");
    }
    const current = snapshot.data()!;
    if (current.masterId !== uid) {
      throw failure("permission-denied", "NOT_GAME_MASTER", "Only the game master excludes a player");
    }
    transaction.set(accessOf(game), { excludedIds: FieldValue.arrayUnion(playerId) }, { merge: true });
    removePlayer(transaction, game, current.memberIds as string[], playerId);
  });

  return { kicked: true };
});
