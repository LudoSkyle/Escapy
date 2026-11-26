import { DocumentReference, FieldValue, Transaction } from "firebase-admin/firestore";
import { failure } from "../core/errors";
import { db, GAMES, isNotFound, JOIN_CODES, PLAYERS, USERS } from "../core/firestore";
import { GameMember } from "./game";

/** Where a game keeps what no client reads: the players its game master excluded. */
export function accessOf(game: DocumentReference): DocumentReference {
  return game.collection("private").doc("access");
}

/** The name and avatar of a user, copied into the games they join. */
export async function readMember(uid: string, transaction?: Transaction): Promise<GameMember> {
  const reference = db().collection(USERS).doc(uid);
  const snapshot = transaction ? await transaction.get(reference) : await reference.get();
  if (!snapshot.exists) {
    throw failure("not-found", "PROFILE_NOT_FOUND", "Call ensureProfile first");
  }
  const data = snapshot.data()!;
  return { username: data.username, avatarId: data.avatarId };
}

/**
 * Takes a player out of a game, in a transaction that already read the game: their document, their
 * place in memberIds, and the count. Nothing happens to a user who is not in the game.
 */
export function removePlayer(
  transaction: Transaction,
  game: DocumentReference,
  memberIds: string[],
  playerId: string,
): void {
  if (!memberIds.includes(playerId)) {
    return;
  }
  transaction.delete(game.collection(PLAYERS).doc(playerId));
  transaction.update(game, {
    memberIds: FieldValue.arrayRemove(playerId),
    playerCount: FieldValue.increment(-1),
    updatedAt: FieldValue.serverTimestamp(),
  });
}

/** Deletes a game, its subcollections and its code. Safe to run again. */
export async function deleteGame(game: DocumentReference): Promise<void> {
  const snapshot = await game.get();
  const joinCode = snapshot.data()?.joinCode as string | undefined;
  // The code first: once the game is gone, nothing would point to it any more
  if (joinCode) {
    await db().collection(JOIN_CODES).doc(joinCode).delete();
  }
  await db().recursiveDelete(game);
}

/**
 * Takes a user out of every game, before their account is deleted: the games they run are deleted
 * for everyone, they leave the others.
 */
export async function removeUserFromGames(uid: string): Promise<void> {
  const games = await db().collection(GAMES).where("memberIds", "array-contains", uid).get();
  for (const game of games.docs) {
    if (game.data().masterId === uid) {
      await deleteGame(game.ref);
    } else {
      await db().runTransaction(async (transaction) => {
        const current = await transaction.get(game.ref);
        if (current.exists) {
          removePlayer(transaction, game.ref, current.data()!.memberIds as string[], uid);
        }
      });
    }
  }
}

/** Copies a changed name or avatar into every game of the user, where the others see it. */
export async function copyMemberToGames(uid: string, member: GameMember): Promise<void> {
  const games = await db().collection(GAMES).where("memberIds", "array-contains", uid).get();
  for (const game of games.docs) {
    const target = game.data().masterId === uid ? game.ref : game.ref.collection(PLAYERS).doc(uid);
    try {
      // update(), not set(): a game deleted or left meanwhile must not come back
      await target.update(game.data().masterId === uid ? { master: member } : { ...member });
    } catch (error) {
      if (!isNotFound(error)) {
        throw error;
      }
    }
  }
}
