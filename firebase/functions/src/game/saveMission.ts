import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { MISSIONS_PER_GAME_LIMIT, MissionState, validateMission, validateMissionId } from "./mission";
import { isDue, masterMissionOf, readMissions, visibleMissionData, visibleMissionOf } from "./missionData";
import { onGame } from "./timer";

/**
 * Creates or changes a mission of a game, for its game master, before the game starts or while it is
 * played. The app chooses the id: sent again, a creation creates nothing twice. While the game is
 * played, a mission whose trigger already happened goes on at once, and the players see a change of
 * an active mission at once.
 */
export const saveMission = onCall(async (request): Promise<{ missionId: string }> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const missionId = validateMissionId(data.missionId);

  const outcome = await onGame(uid, data.gameId, true, async ({ transaction, game, data: current }) => {
    // Finished, possibly just now on time: nothing more is read nor written
    if (current.status === "FINISHED") return "FINISHED";
    const missions = await readMissions(transaction, game);
    const existing = missions.find((mission) => mission.id === missionId);
    if (!existing && missions.length >= MISSIONS_PER_GAME_LIMIT) {
      throw failure("resource-exhausted", "TOO_MANY_MISSIONS", `A game has ${MISSIONS_PER_GAME_LIMIT} missions at most`);
    }
    const otherTriggers = new Map(
      missions.filter((mission) => mission.id !== missionId).map((mission) => [mission.id, mission.data.trigger]),
    );
    const definition = validateMission(data, missionId, otherTriggers);

    const isStarted = current.status !== "WAITING";
    const state: MissionState = existing?.data.state ?? "LOCKED";
    const unlocksNow = isStarted && state === "LOCKED" && isDue(missions, definition.trigger);
    const unlocked = unlocksNow ? { state: "ACTIVE", unlockedAt: Timestamp.now() } : {};
    const stored = { ...(existing?.data ?? { state, createdAt: FieldValue.serverTimestamp() }), ...definition, ...unlocked };
    if (existing) {
      transaction.update(masterMissionOf(game, missionId), { ...definition, ...unlocked });
    } else {
      transaction.create(masterMissionOf(game, missionId), stored);
    }
    if (stored.state !== "LOCKED") {
      transaction.set(visibleMissionOf(game, missionId), visibleMissionData(stored));
    }
    return "SAVED";
  });

  if (outcome === "FINISHED") {
    throw failure("failed-precondition", "GAME_FINISHED", "The game is over");
  }
  return { missionId };
});
