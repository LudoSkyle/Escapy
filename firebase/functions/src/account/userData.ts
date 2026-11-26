import { db, JOIN_ATTEMPTS, USERS } from "../core/firestore";
import { removeUserFromGames } from "../game/gameData";

/**
 * Deletes everything that belongs to a user, before their account. Safe to run again after a
 * failure. Each new kind of data of a user (games they run or play, scenarios, files) adds its
 * deletion here, in the lot that creates it.
 */
export async function deleteUserData(uid: string): Promise<void> {
  // The games first: they show the name of the user, read from the profile
  await removeUserFromGames(uid);
  // delete() succeeds when the document is already gone
  await db().collection(JOIN_ATTEMPTS).doc(uid).delete();
  await db().collection(USERS).doc(uid).delete();
}
