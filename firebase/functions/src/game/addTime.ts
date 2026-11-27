import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { validateAddedMinutes } from "./game";
import { MINUTE_MS, onGame, remainingMsOf, requireStarted, timerResponse } from "./timer";

export interface AddTimeResponse {
  addedMinutes: number;
  remainingMs: number;
}

/**
 * Adds minutes to a running or paused game, for its game master. The request carries the minutes
 * already added that the app saw: a call sent again after a lost answer finds them changed, and adds
 * nothing twice.
 */
export const addTime = onCall(async (request): Promise<AddTimeResponse> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const seen = data.addedMinutes;
  if (typeof seen !== "number" || !Number.isInteger(seen) || seen < 0) {
    throw failure("invalid-argument", "INVALID_REQUEST", "addedMinutes must be the minutes already added");
  }

  const outcome = await onGame(uid, data.gameId, true, ({ transaction, game, data: current, nowMs }) => {
    requireStarted(current);
    const alreadyAdded = (current.addedMinutes as number | undefined) ?? 0;
    // Finished, possibly just now on time: the transaction keeps that, the call is refused after it
    if (current.status === "FINISHED") {
      return { finished: true, addedMinutes: alreadyAdded, remainingMs: 0 };
    }
    if (alreadyAdded !== seen) {
      return { finished: false, addedMinutes: alreadyAdded, remainingMs: remainingMsOf(current, nowMs) };
    }
    const minutes = validateAddedMinutes(data.minutes, alreadyAdded);
    const change = current.status === "RUNNING"
      ? { endsAt: Timestamp.fromMillis((current.endsAt as Timestamp).toMillis() + minutes * MINUTE_MS) }
      : { remainingMs: (current.remainingMs as number) + minutes * MINUTE_MS };
    transaction.update(game, { ...change, addedMinutes: alreadyAdded + minutes, updatedAt: FieldValue.serverTimestamp() });
    return {
      finished: false,
      addedMinutes: alreadyAdded + minutes,
      remainingMs: timerResponse({ ...current, ...change }, nowMs).remainingMs,
    };
  });

  if (outcome.finished) {
    throw failure("failed-precondition", "GAME_FINISHED", "The game is over");
  }
  return { addedMinutes: outcome.addedMinutes, remainingMs: outcome.remainingMs };
});
