import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { validateMissionId } from "./mission";
import { masterMissionOf, readMissions, visibleMissionOf } from "./missionData";
import { onGame } from "./timer";

/**
 * Deletes a mission of a game, for its game master, before the game starts or while it is played.
 * The missions that followed it are unlocked by hand from then on. Safe to call again: a mission
 * already deleted is no error. A deletion never wins a game.
 */
export const deleteMission = onCall(async (request): Promise<{ deleted: true }> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const missionId = validateMissionId(data.missionId);

  const outcome = await onGame(uid, data.gameId, true, async ({ transaction, game, data: current }) => {
    if (current.status === "FINISHED") return "FINISHED";
    const missions = await readMissions(transaction, game);
    if (!missions.some((mission) => mission.id === missionId)) return "DELETED";
    for (const mission of missions) {
      if (mission.data.trigger?.type === "AFTER_MISSION" && mission.data.trigger.missionId === missionId) {
        transaction.update(masterMissionOf(game, mission.id), { trigger: { type: "MANUAL" } });
      }
    }
    transaction.delete(masterMissionOf(game, missionId));
    transaction.delete(visibleMissionOf(game, missionId));
    return "DELETED";
  });

  if (outcome === "FINISHED") {
    throw failure("failed-precondition", "GAME_FINISHED", "The game is over");
  }
  return { deleted: true };
});
