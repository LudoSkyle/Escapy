import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { validateMissionId } from "./mission";
import { readMissions, unlock } from "./missionData";
import { onGame, requireStarted } from "./timer";

/**
 * Unlocks a mission by hand, for the game master of a started game, whatever its trigger. Safe to
 * call again: a mission already unlocked is left as it is.
 */
export const unlockMission = onCall(async (request): Promise<{ unlocked: true }> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const missionId = validateMissionId(data.missionId);

  const outcome = await onGame(uid, data.gameId, true, async ({ transaction, game, data: current }) => {
    if (current.status === "FINISHED") return "FINISHED";
    requireStarted(current);
    const mission = (await readMissions(transaction, game)).find((candidate) => candidate.id === missionId);
    if (!mission) {
      throw failure("not-found", "MISSION_NOT_FOUND", "No mission has this id");
    }
    unlock(transaction, game, mission);
    return "UNLOCKED";
  });

  if (outcome === "FINISHED") {
    throw failure("failed-precondition", "GAME_FINISHED", "The game is over");
  }
  return { unlocked: true };
});
