import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { onGame, timerResponse, TimerResponse } from "./timer";

/**
 * Records that the time of a running game is up, for any of its members: their app calls it when its
 * timer reaches zero. The server decides by its own clock: a game not over yet is left as it is, and
 * the answer tells the time it has left.
 */
export const expireGame = onCall(async (request): Promise<TimerResponse> => {
  const uid = requireUid(request);
  // onGame finishes a game whose time is up, whoever calls
  return onGame(uid, requestData(request).gameId, false, ({ data, nowMs }) => timerResponse(data, nowMs));
});
