import { DocumentData, DocumentReference, Timestamp, Transaction } from "firebase-admin/firestore";
import { MissionDefinition, MissionState } from "./mission";
import { finish } from "./timer";

/** The missions as the game master wrote them, answers included: read by the game master only. */
export const MASTER_MISSIONS = "masterMissions";
/** The missions the players see, created when unlocked: never an answer. */
export const MISSIONS = "missions";

/** A mission of a game, read in a transaction: its definition and its state, which it holds. */
export interface StoredMission {
  id: string;
  data: DocumentData;
}

/** Who solved a mission, as the players see it: a player, or the game master in their place. */
export interface Solver {
  userId: string;
  username: string;
}

export function masterMissionOf(game: DocumentReference, missionId: string): DocumentReference {
  return game.collection(MASTER_MISSIONS).doc(missionId);
}

export function visibleMissionOf(game: DocumentReference, missionId: string): DocumentReference {
  return game.collection(MISSIONS).doc(missionId);
}

/** Every mission of a game, in the order they were created: read once per transaction. */
export async function readMissions(transaction: Transaction, game: DocumentReference): Promise<StoredMission[]> {
  const snapshot = await transaction.get(game.collection(MASTER_MISSIONS).orderBy("createdAt"));
  return snapshot.docs.map((document) => ({ id: document.id, data: document.data() }));
}

/** What the players see of a mission: its words and its state, never its answers. */
export function visibleMissionData(mission: DocumentData): DocumentData {
  return {
    title: mission.title,
    statement: mission.statement,
    type: mission.type,
    codeLength: mission.codeLength,
    isFinal: mission.isFinal,
    state: mission.state,
    createdAt: mission.createdAt,
    unlockedAt: mission.unlockedAt ?? null,
    solvedAt: mission.solvedAt ?? null,
    solvedBy: mission.solvedBy ?? null,
  };
}

/** The definition of a mission, as stored next to its state. */
export function definitionData(definition: MissionDefinition): DocumentData {
  return { ...definition };
}

/**
 * Unlocks a locked mission: active for the game master, and created for the players. The mission
 * read is updated too, for what follows in the same transaction.
 */
export function unlock(transaction: Transaction, game: DocumentReference, mission: StoredMission): void {
  if (mission.data.state !== "LOCKED") return;
  const now = Timestamp.now();
  const state: MissionState = "ACTIVE";
  mission.data = { ...mission.data, state, unlockedAt: now };
  transaction.update(masterMissionOf(game, mission.id), { state, unlockedAt: now });
  transaction.set(visibleMissionOf(game, mission.id), visibleMissionData(mission.data));
}

/** Unlocks the locked missions of the start, when the game starts or one is added while it runs. */
export function unlockOnStart(transaction: Transaction, game: DocumentReference, missions: StoredMission[]): void {
  for (const mission of missions) {
    if (mission.data.trigger?.type === "ON_START") {
      unlock(transaction, game, mission);
    }
  }
}

/**
 * Whether the trigger of a locked mission of a started game already happened: at the start, or after
 * a mission already solved. A mission added or changed while the game is played goes on at once,
 * rather than waiting for an event already past.
 */
export function isDue(missions: StoredMission[], trigger: DocumentData | undefined): boolean {
  return (
    trigger?.type === "ON_START" ||
    (trigger?.type === "AFTER_MISSION" &&
      missions.find((other) => other.id === trigger.missionId)?.data.state === "SOLVED")
  );
}

/**
 * Solves a mission, active or locked (the game master validates one the players found "for real"):
 * the missions after it unlock, and the game is won once every final mission is solved. Only a
 * mission solved wins a game: neither a change nor a deletion does. [extra] goes in the same write
 * of the mission: each document is written once per transaction.
 */
export function solve(
  transaction: Transaction,
  game: DocumentReference,
  gameData: DocumentData,
  missions: StoredMission[],
  mission: StoredMission,
  solver: Solver,
  nowMs: number,
  extra: DocumentData = {},
): void {
  if (mission.data.state === "SOLVED") return;
  const solvedAt = Timestamp.fromMillis(nowMs);
  const state: MissionState = "SOLVED";
  const change = { ...extra, state, solvedAt, solvedBy: solver, unlockedAt: mission.data.unlockedAt ?? solvedAt };
  mission.data = { ...mission.data, ...change };
  transaction.update(masterMissionOf(game, mission.id), change);
  transaction.set(visibleMissionOf(game, mission.id), visibleMissionData(mission.data));

  for (const next of missions) {
    if (next.data.trigger?.type === "AFTER_MISSION" && next.data.trigger.missionId === mission.id) {
      unlock(transaction, game, next);
    }
  }

  const finals = missions.filter((candidate) => candidate.data.isFinal === true);
  if (finals.length > 0 && finals.every((final) => final.data.state === "SOLVED")) {
    finish(transaction, game, gameData, "WON", nowMs);
  }
}

/** The changes that start the count of answers again, or add one to it, in the window of a minute. */
export function countedAnswer(mission: DocumentData, nowMs: number): DocumentData {
  const window = mission.answerWindow as { startedAt: Timestamp; count: number } | undefined;
  if (window && window.startedAt.toMillis() > nowMs - 60 * 1000) {
    return { answerWindow: { startedAt: window.startedAt, count: window.count + 1 } };
  }
  return { answerWindow: { startedAt: Timestamp.fromMillis(nowMs), count: 1 } };
}

/** The answers sent to a mission in the current minute. */
export function answersThisMinute(mission: DocumentData, nowMs: number): number {
  const window = mission.answerWindow as { startedAt: Timestamp; count: number } | undefined;
  return window && window.startedAt.toMillis() > nowMs - 60 * 1000 ? window.count : 0;
}

