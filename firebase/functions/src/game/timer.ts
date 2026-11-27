import { DocumentData, DocumentReference, FieldValue, Timestamp, Transaction } from "firebase-admin/firestore";
import { failure } from "../core/errors";
import { db, GAMES } from "../core/firestore";
import { GameResult, GameStatus, validateGameId } from "./game";
import { userGameOf } from "./gameData";

export const MINUTE_MS = 60 * 1000;

/**
 * The time left to a game at [nowMs]: counted down to endsAt while it runs, frozen in remainingMs
 * while it is paused, none before it starts or once it is over.
 */
export function remainingMsOf(game: DocumentData, nowMs: number): number {
  switch (game.status as GameStatus) {
    case "RUNNING":
      return Math.max(0, (game.endsAt as Timestamp).toMillis() - nowMs);
    case "PAUSED":
      return game.remainingMs as number;
    default:
      return 0;
  }
}

/**
 * Whether a running game is over at [nowMs]. Such a game is over for everyone, even before a call
 * records it: every action on the timer checks this first, so that a tap just after zero never
 * extends a game its players already see as lost.
 */
export function isTimeUp(game: DocumentData, nowMs: number): boolean {
  return game.status === "RUNNING" && (game.endsAt as Timestamp).toMillis() <= nowMs;
}

/** The time a game was played for, pauses apart: the time it had, less [remainingMs]. */
export function playedMsOf(game: DocumentData, remainingMs: number): number {
  const allottedMs = ((game.durationMinutes as number) + ((game.addedMinutes as number | undefined) ?? 0)) * MINUTE_MS;
  return allottedMs - remainingMs;
}

/** Changes the status of a game, in the game and in the list of each of its members. */
export function setStatus(
  transaction: Transaction,
  game: DocumentReference,
  data: DocumentData,
  status: GameStatus,
  change: DocumentData,
): void {
  transaction.update(game, { ...change, status, updatedAt: FieldValue.serverTimestamp() });
  // Each member has their entry, written with them: memberIds was read in this transaction
  for (const memberId of data.memberIds as string[]) {
    transaction.set(userGameOf(memberId, game.id), { status }, { merge: true });
  }
}

/** Finishes a started game for good, with what its recap shows. */
export function finish(
  transaction: Transaction,
  game: DocumentReference,
  data: DocumentData,
  result: GameResult,
  nowMs: number,
): void {
  // A game over on time had no time left, whenever this runs
  const remainingMs = result === "TIME_UP" ? 0 : remainingMsOf(data, nowMs);
  setStatus(transaction, game, data, "FINISHED", {
    result,
    endsAt: null,
    remainingMs,
    playedMs: playedMsOf(data, remainingMs),
    finishedAt: FieldValue.serverTimestamp(),
  });
}

export interface GameInTransaction {
  transaction: Transaction;
  game: DocumentReference;
  data: DocumentData;
  nowMs: number;
}

/**
 * Runs [action] in a transaction on a game the user who calls is a member of, or runs when
 * [masterOnly]. A running game whose time is up is finished first, and [action] sees it finished.
 */
export async function onGame<T>(
  uid: string,
  rawGameId: unknown,
  masterOnly: boolean,
  action: (current: GameInTransaction) => T,
): Promise<T> {
  const game = db().collection(GAMES).doc(validateGameId(rawGameId));
  return db().runTransaction(async (transaction) => {
    const snapshot = await transaction.get(game);
    if (!snapshot.exists) {
      throw failure("not-found", "GAME_NOT_FOUND", "The game was deleted");
    }
    let data = snapshot.data()!;
    if (masterOnly && data.masterId !== uid) {
      throw failure("permission-denied", "NOT_GAME_MASTER", "Only the game master runs the timer");
    }
    if (!(data.memberIds as string[]).includes(uid)) {
      throw failure("permission-denied", "NOT_GAME_MEMBER", "Only a member of the game reads its timer");
    }
    const nowMs = Date.now();
    if (isTimeUp(data, nowMs)) {
      finish(transaction, game, data, "TIME_UP", nowMs);
      data = { ...data, status: "FINISHED", result: "TIME_UP", remainingMs: 0, endsAt: null };
    }
    return action({ transaction, game, data, nowMs });
  });
}

/** Refuses an action on the timer of a game that has not started yet. */
export function requireStarted(data: DocumentData): void {
  if (data.status === "WAITING") {
    throw failure("failed-precondition", "GAME_NOT_STARTED", "The game has not started yet");
  }
}

/** The state of the timer a function answers with: what the game holds after the call. */
export interface TimerResponse {
  status: GameStatus;
  remainingMs: number;
}

export function timerResponse(data: DocumentData, nowMs: number): TimerResponse {
  return { status: data.status as GameStatus, remainingMs: remainingMsOf(data, nowMs) };
}

