import { randomInt } from "node:crypto";
import { DocumentData } from "firebase-admin/firestore";
import { failure } from "../core/errors";

/**
 * The state of a game: waiting for its players, then running or paused while the timer counts down,
 * then finished for good.
 */
export type GameStatus = "WAITING" | "RUNNING" | "PAUSED" | "FINISHED";

/** The games that still count towards ACTIVE_GAMES_LIMIT: a finished game only waits to be deleted. */
export const ACTIVE_STATUSES: GameStatus[] = ["WAITING", "RUNNING", "PAUSED"];

/** Why a game finished: its final missions solved, its time up, or its game master. */
export type GameResult = "WON" | "TIME_UP" | "ENDED_BY_MASTER";

export const GAME_TITLE_MIN_LENGTH = 3;
export const GAME_TITLE_MAX_LENGTH = 40;

export const GAME_DURATION_MIN_MINUTES = 15;
export const GAME_DURATION_MAX_MINUTES = 180;
export const GAME_DURATION_STEP_MINUTES = 5;

/** The most players a game takes, the game master apart: the default, and the highest maximum. */
export const PLAYER_LIMIT = 20;
export const MAX_PLAYERS_MIN = 2;

/** The waiting games a user may run at once: more would only fill the codes. */
export const ACTIVE_GAMES_LIMIT = 5;

/**
 * A waiting game nobody started is deleted this long after its creation; a started game, this long
 * after its last change (a finished game, after its end).
 */
export const STALE_GAME_HOURS = 24;

/** The time the game master adds at once, and in all, in minutes. */
export const ADDED_MINUTES_MIN = 1;
export const ADDED_MINUTES_MAX = 60;
export const ADDED_MINUTES_TOTAL_MAX = 180;

export const JOIN_CODE_LENGTH = 6;
// Neither 0 and O nor 1, I and L: a code is read aloud and typed by hand
export const JOIN_CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
const JOIN_CODE_PATTERN = new RegExp(`^[${JOIN_CODE_ALPHABET}]{${JOIN_CODE_LENGTH}}$`);

/** Unknown codes a user may try in a window, against guessing the code of a game. */
export const JOIN_FAILURES_LIMIT = 10;
export const JOIN_FAILURES_WINDOW_MINUTES = 10;

/** A member of a game as the others see them: copied from the profile, kept in step by updateProfile. */
export interface GameMember {
  username: string;
  avatarId: number;
}

export function randomJoinCode(): string {
  let code = "";
  for (let index = 0; index < JOIN_CODE_LENGTH; index++) {
    code += JOIN_CODE_ALPHABET[randomInt(JOIN_CODE_ALPHABET.length)];
  }
  return code;
}

/** The code as typed: case and spaces do not matter. */
export function validateJoinCode(raw: unknown): string {
  if (typeof raw !== "string") {
    throw failure("invalid-argument", "JOIN_CODE_INVALID", "The code must be a string");
  }
  const code = raw.replace(/\s+/gu, "").toUpperCase();
  if (!JOIN_CODE_PATTERN.test(code)) {
    throw failure("invalid-argument", "JOIN_CODE_INVALID", `The code has ${JOIN_CODE_LENGTH} characters of the code alphabet`);
  }
  return code;
}

/** The title as stored: trimmed, inner spaces collapsed, in its composed Unicode form. */
export function validateGameTitle(raw: unknown): string {
  if (typeof raw !== "string") {
    throw failure("invalid-argument", "GAME_TITLE_INVALID", "The title must be a string");
  }
  const title = raw.normalize("NFC").trim().replace(/\s+/gu, " ");
  const length = [...title].length;
  // No control character: a title is shown on one line
  if (length < GAME_TITLE_MIN_LENGTH || length > GAME_TITLE_MAX_LENGTH || /\p{Cc}/u.test(title)) {
    throw failure("invalid-argument", "GAME_TITLE_INVALID", "The title is too short, too long or has a control character");
  }
  return title;
}

export function validateDurationMinutes(raw: unknown): number {
  if (
    typeof raw !== "number" ||
    !Number.isInteger(raw) ||
    raw < GAME_DURATION_MIN_MINUTES ||
    raw > GAME_DURATION_MAX_MINUTES ||
    raw % GAME_DURATION_STEP_MINUTES !== 0
  ) {
    throw failure(
      "invalid-argument",
      "GAME_DURATION_INVALID",
      `The duration is from ${GAME_DURATION_MIN_MINUTES} to ${GAME_DURATION_MAX_MINUTES} minutes, by ${GAME_DURATION_STEP_MINUTES}`,
    );
  }
  return raw;
}

/** No maximum (null or absent) lets in up to PLAYER_LIMIT players. */
export function validateMaxPlayers(raw: unknown): number | null {
  if (raw === undefined || raw === null) {
    return null;
  }
  if (typeof raw !== "number" || !Number.isInteger(raw) || raw < MAX_PLAYERS_MIN || raw > PLAYER_LIMIT) {
    throw failure("invalid-argument", "MAX_PLAYERS_INVALID", `The maximum is an integer from ${MAX_PLAYERS_MIN} to ${PLAYER_LIMIT}`);
  }
  return raw;
}

/** The minutes to add to a game that already has [alreadyAdded] minutes added. */
export function validateAddedMinutes(raw: unknown, alreadyAdded: number): number {
  if (
    typeof raw !== "number" ||
    !Number.isInteger(raw) ||
    raw < ADDED_MINUTES_MIN ||
    raw > ADDED_MINUTES_MAX ||
    alreadyAdded + raw > ADDED_MINUTES_TOTAL_MAX
  ) {
    throw failure(
      "invalid-argument",
      "ADDED_TIME_INVALID",
      `From ${ADDED_MINUTES_MIN} to ${ADDED_MINUTES_MAX} minutes at once, ${ADDED_MINUTES_TOTAL_MAX} in all`,
    );
  }
  return raw;
}

/** The players a game still takes. */
export function hasRoomLeft(game: DocumentData): boolean {
  return (game.playerCount as number) < ((game.maxPlayers as number | null) ?? PLAYER_LIMIT);
}

/** The game id a function receives, refused when it cannot be a document id. */
export function validateGameId(raw: unknown): string {
  if (typeof raw !== "string" || raw.length === 0 || raw.length > 128 || raw.includes("/")) {
    throw failure("invalid-argument", "INVALID_REQUEST", "The game id must be a document id");
  }
  return raw;
}
