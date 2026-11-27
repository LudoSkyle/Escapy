import { Timestamp } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { onGame, requireStarted, setStatus, timerResponse, TimerResponse } from "./timer";

/**
 * Starts the timer of a paused game again, for its game master, from the time it had left. Safe to
 * call again: a running or finished game is left as it is.
 */
export const resumeGame = onCall(async (request): Promise<TimerResponse> => {
  const uid = requireUid(request);
  return onGame(uid, requestData(request).gameId, true, ({ transaction, game, data, nowMs }) => {
    requireStarted(data);
    if (data.status !== "PAUSED") {
      return timerResponse(data, nowMs);
    }
    const endsAt = Timestamp.fromMillis(nowMs + (data.remainingMs as number));
    setStatus(transaction, game, data, "RUNNING", { endsAt, remainingMs: null });
    return timerResponse({ ...data, status: "RUNNING", endsAt }, nowMs);
  });
});
