import { Timestamp } from "firebase-admin/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { db, GAMES } from "../core/firestore";
import { STALE_GAME_HOURS } from "./game";
import { deleteGame } from "./gameData";

/** The games deleted by one run at most: the next run takes the rest. */
const BATCH_SIZE = 100;

/**
 * Deletes the waiting games nobody started, created before [before], with their codes. Returns how
 * many were deleted.
 */
export async function deleteGamesWaitingSince(before: Timestamp): Promise<number> {
  const stale = await db()
    .collection(GAMES)
    .where("status", "==", "WAITING")
    .where("createdAt", "<", before)
    .limit(BATCH_SIZE)
    .get();
  for (const game of stale.docs) {
    await deleteGame(game.ref);
  }
  return stale.size;
}

/**
 * Deletes the started games left untouched since [before]: a finished game since its end, and a
 * game abandoned running or paused, whose longest timer is far shorter. Returns how many were
 * deleted.
 */
export async function deleteGamesStartedAndIdleSince(before: Timestamp): Promise<number> {
  const stale = await db()
    .collection(GAMES)
    .where("status", "in", ["RUNNING", "PAUSED", "FINISHED"])
    .where("updatedAt", "<", before)
    .limit(BATCH_SIZE)
    .get();
  for (const game of stale.docs) {
    await deleteGame(game.ref);
  }
  return stale.size;
}

export const deleteStaleGames = onSchedule("every 60 minutes", async () => {
  const before = Timestamp.fromMillis(Date.now() - STALE_GAME_HOURS * 60 * 60 * 1000);
  await deleteGamesWaitingSince(before);
  await deleteGamesStartedAndIdleSince(before);
});
