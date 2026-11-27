import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { validateMissionId } from "./mission";
import { readMissions, solve } from "./missionData";
import { onGame, requireStarted } from "./timer";

/**
 * Validates a mission in the place of the players, for the game master of a started game, when they
 * found it "for real": as if solved, locked or not, with what follows (the missions after it, the
 * victory). Safe to call again: a mission already solved is left as it is.
 */
export const validateMission = onCall(async (request): Promise<{ validated: true }> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const missionId = validateMissionId(data.missionId);

  const outcome = await onGame(uid, data.gameId, true, async ({ transaction, game, data: current, nowMs }) => {
    if (current.status === "FINISHED") return "FINISHED";
    requireStarted(current);
    const missions = await readMissions(transaction, game);
    const mission = missions.find((candidate) => candidate.id === missionId);
    if (!mission) {
      throw failure("not-found", "MISSION_NOT_FOUND", "No mission has this id");
    }
    const master = current.master as { username: string };
    solve(transaction, game, current, missions, mission, { userId: uid, username: master.username }, nowMs);
    return "VALIDATED";
  });

  if (outcome === "FINISHED") {
    throw failure("failed-precondition", "GAME_FINISHED", "The game is over");
  }
  return { validated: true };
});
