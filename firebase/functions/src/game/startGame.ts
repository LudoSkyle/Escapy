import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { readMissions, unlockOnStart } from "./missionData";
import { MINUTE_MS, onGame, setStatus, timerResponse, TimerResponse } from "./timer";

/**
 * Starts a waiting game, for its game master: the timer counts down from its duration, and the
 * missions of the start unlock. Safe to call again: a game already started is left as it is.
 */
export const startGame = onCall(async (request): Promise<TimerResponse> => {
  const uid = requireUid(request);
  return onGame(uid, requestData(request).gameId, true, async ({ transaction, game, data, nowMs }) => {
    if (data.status !== "WAITING") {
      return timerResponse(data, nowMs);
    }
    // Read before any write: the missions of the start go on with the timer
    const missions = await readMissions(transaction, game);
    const endsAt = Timestamp.fromMillis(nowMs + (data.durationMinutes as number) * MINUTE_MS);
    setStatus(transaction, game, data, "RUNNING", {
      startedAt: FieldValue.serverTimestamp(),
      endsAt,
      remainingMs: null,
      addedMinutes: 0,
    });
    unlockOnStart(transaction, game, missions);
    return timerResponse({ ...data, status: "RUNNING", endsAt }, nowMs);
  });
});
