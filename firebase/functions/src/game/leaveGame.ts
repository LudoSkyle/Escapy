import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { db, GAMES } from "../core/firestore";
import { validateGameId } from "./game";
import { removePlayer } from "./gameData";

/**
 * Takes the user who calls out of a game they play. Safe to call again: a game deleted or already
 * left is no error. The game master cancels their game instead.
 */
export const leaveGame = onCall(async (request): Promise<{ left: true }> => {
  const uid = requireUid(request);
  const game = db().collection(GAMES).doc(validateGameId(requestData(request).gameId));

  await db().runTransaction(async (transaction) => {
    const snapshot = await transaction.get(game);
    if (!snapshot.exists) {
      return;
    }
    const data = snapshot.data()!;
    if (data.masterId === uid) {
      throw failure("failed-precondition", "GAME_MASTER_CANNOT_LEAVE", "The game master cancels the game instead");
    }
    removePlayer(transaction, game, data.memberIds as string[], uid);
  });

  return { left: true };
});
