import { FunctionsErrorCode, HttpsError } from "firebase-functions/v2/https";

/**
 * Why a call was refused, sent in `details.reason`: a stable value the app maps to a typed
 * exception. The message is in English, for the logs only, and never shown to the user.
 */
export type FailureReason =
  | "UNAUTHENTICATED"
  | "INVALID_REQUEST"
  | "USERNAME_INVALID"
  | "AVATAR_INVALID"
  | "NOTHING_TO_UPDATE"
  | "PROFILE_NOT_FOUND"
  | "RECENT_SIGN_IN_REQUIRED"
  | "GAME_TITLE_INVALID"
  | "GAME_DURATION_INVALID"
  | "MAX_PLAYERS_INVALID"
  | "TOO_MANY_GAMES"
  | "JOIN_CODE_INVALID"
  | "TOO_MANY_ATTEMPTS"
  | "GAME_NOT_FOUND"
  | "GAME_NOT_JOINABLE"
  | "GAME_FULL"
  | "PLAYER_EXCLUDED"
  | "NOT_GAME_MASTER"
  | "GAME_MASTER_CANNOT_LEAVE"
  | "NOT_GAME_MEMBER"
  | "GAME_NOT_STARTED"
  | "GAME_FINISHED"
  | "ADDED_TIME_INVALID"
  | "GAME_PAUSED"
  | "MISSION_INVALID"
  | "MISSION_NOT_FOUND"
  | "TOO_MANY_MISSIONS"
  | "TOO_MANY_ANSWERS";

export function failure(code: FunctionsErrorCode, reason: FailureReason, message: string): HttpsError {
  return new HttpsError(code, message, { reason });
}
