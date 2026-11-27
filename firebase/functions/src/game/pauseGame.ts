import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { onGame, remainingMsOf, requireStarted, setStatus, timerResponse, TimerResponse } from "./timer";

/**
 * Pauses the timer of a running game, for its game master: the time left is kept. Safe to call
 * again: a paused or finished game is left as it is.
 */
export const pauseGame = onCall(async (request): Promise<TimerResponse> => {
  const uid = requireUid(request);
  return onGame(uid, requestData(request).gameId, true, ({ transaction, game, data, nowMs }) => {
    requireStarted(data);
    if (data.status !== "RUNNING") {
      return timerResponse(data, nowMs);
    }
    const remainingMs = remainingMsOf(data, nowMs);
    setStatus(transaction, game, data, "PAUSED", { endsAt: null, remainingMs });
    return timerResponse({ ...data, status: "PAUSED", remainingMs }, nowMs);
  });
});
