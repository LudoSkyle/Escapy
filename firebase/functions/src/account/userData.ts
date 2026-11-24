import { db, USERS } from "../core/firestore";

/**
 * Deletes everything that belongs to a user, before their account. Safe to run again after a
 * failure. Each new kind of data of a user (games they run or play, scenarios, files) adds its
 * deletion here, in the lot that creates it.
 */
export async function deleteUserData(uid: string): Promise<void> {
  // delete() succeeds when the document is already gone
  await db().collection(USERS).doc(uid).delete();
}
