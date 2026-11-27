import { onCall } from "firebase-functions/v2/https";
import { requireUid } from "../core/auth";

export interface ServerTimeResponse {
  nowMs: number;
}

/**
 * The time of the server, from which the app estimates how far its clock is from it: the timer of a
 * game is shown from the server time, never from the clock of the device.
 */
export const getServerTime = onCall(async (request): Promise<ServerTimeResponse> => {
  requireUid(request);
  return { nowMs: Date.now() };
});
