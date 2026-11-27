import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { finish, onGame, requireStarted, timerResponse, TimerResponse } from "./timer";

/**
 * Ends a running or paused game for good, for its game master. Safe to call again: a finished game
 * is left as it is, and a game whose time was up stays over on time.
 */
export const endGame = onCall(async (request): Promise<TimerResponse> => {
  const uid = requireUid(request);
  return onGame(uid, requestData(request).gameId, true, ({ transaction, game, data, nowMs }) => {
    requireStarted(data);
    if (data.status === "FINISHED") {
      return timerResponse(data, nowMs);
    }
    finish(transaction, game, data, "ENDED_BY_MASTER", nowMs);
    return { status: "FINISHED", remainingMs: 0 };
  });
});
