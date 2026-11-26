import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { db, GAMES } from "../core/firestore";
import { validateGameId } from "./game";
import { deleteGame } from "./gameData";

/**
 * Deletes a game for everyone, for its game master only, and frees its code. Safe to call again: a
 * game already deleted is no error.
 */
export const cancelGame = onCall(async (request): Promise<{ cancelled: true }> => {
  const uid = requireUid(request);
  const game = db().collection(GAMES).doc(validateGameId(requestData(request).gameId));

  const snapshot = await game.get();
  if (!snapshot.exists) {
    return { cancelled: true };
  }
  if (snapshot.data()!.masterId !== uid) {
    throw failure("permission-denied", "NOT_GAME_MASTER", "Only the game master cancels the game");
  }
  await deleteGame(game);

  return { cancelled: true };
});
