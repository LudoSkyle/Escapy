import { DocumentData, DocumentReference, FieldValue, Transaction } from "firebase-admin/firestore";
import { failure } from "../core/errors";
import { db, GAMES, isNotFound, JOIN_CODES, PLAYERS, USER_GAMES, USERS } from "../core/firestore";
import { GameMember } from "./game";

/** Where a game keeps what no client reads: the players its game master excluded. */
export function accessOf(game: DocumentReference): DocumentReference {
  return game.collection("private").doc("access");
}

/**
 * The entry of a game in the list of a user (users/{uid}/games/{gameId}): what their home screen
 * shows, from documents they always read. A list made by a query on the games would keep a game the
 * user lost access to, which the Android SDK leaves in its cache (see docs/Backend/README.md).
 */
export function userGameOf(uid: string, gameId: string): DocumentReference {
  return db().collection(USERS).doc(uid).collection(USER_GAMES).doc(gameId);
}

/** The entry of a game for one of its members, from the game as stored. */
export function userGameData(game: DocumentData, isMaster: boolean): DocumentData {
  return {
    title: game.title,
    isMaster,
    master: game.master,
    createdAt: game.createdAt,
  };
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
  transaction.delete(userGameOf(playerId, game.id));
  transaction.update(game, {
    memberIds: FieldValue.arrayRemove(playerId),
    playerCount: FieldValue.increment(-1),
    updatedAt: FieldValue.serverTimestamp(),
  });
}

/** Deletes a game, its subcollections, its code and its entries in the lists of its members. Safe to run again. */
export async function deleteGame(game: DocumentReference): Promise<void> {
  const data = (await game.get()).data();
  // The code and the entries first: once the game is gone, nothing would point to them any more
  const batch = db().batch();
  const joinCode = data?.joinCode as string | undefined;
  if (joinCode) {
    batch.delete(db().collection(JOIN_CODES).doc(joinCode));
  }
  for (const memberId of (data?.memberIds as string[] | undefined) ?? []) {
    batch.delete(userGameOf(memberId, game.id));
  }
  await batch.commit();
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

/**
 * Copies a changed name or avatar into every game of the user, where the others see it: the game and
 * the entries of its members for a game master, their player document for a player.
 */
export async function copyMemberToGames(uid: string, member: GameMember): Promise<void> {
  const games = await db().collection(GAMES).where("memberIds", "array-contains", uid).get();
  for (const game of games.docs) {
    const data = game.data();
    const targets: [DocumentReference, DocumentData][] = data.masterId === uid
      ? [
        [game.ref, { master: member }],
        ...(data.memberIds as string[]).map((memberId): [DocumentReference, DocumentData] => [
          userGameOf(memberId, game.id),
          { master: member },
        ]),
      ]
      : [[game.ref.collection(PLAYERS).doc(uid), { ...member }]];
    for (const [target, change] of targets) {
      await updateIfExists(target, change);
    }
  }
}

// update(), not set(): a game deleted or left meanwhile must not come back
async function updateIfExists(target: DocumentReference, change: DocumentData): Promise<void> {
  try {
    await target.update(change);
  } catch (error) {
    if (!isNotFound(error)) {
      throw error;
    }
  }
}
