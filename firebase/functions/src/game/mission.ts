import { failure } from "../core/errors";

/** What a player types to solve a mission: digits of a fixed length, or a word or a sentence. */
export type MissionType = "CODE" | "TEXT";

/** What makes a mission visible to the players. The chat keyword comes in lot 8b. */
export type MissionTrigger =
  | { type: "MANUAL" }
  | { type: "ON_START" }
  | { type: "AFTER_MISSION"; missionId: string };

/** Locked, a mission does not exist for the players; active, they solve it; solved, it is done. */
export type MissionState = "LOCKED" | "ACTIVE" | "SOLVED";

export const MISSION_TITLE_MAX_LENGTH = 60;
export const MISSION_STATEMENT_MAX_LENGTH = 1000;
export const MISSION_ANSWERS_MAX = 10;
export const TEXT_ANSWER_MAX_LENGTH = 100;
export const CODE_LENGTH_MIN = 3;
export const CODE_LENGTH_MAX = 8;
/** The longest answer a player may send, whatever the mission: more is no answer. */
export const SUBMITTED_ANSWER_MAX_LENGTH = 200;

/** The missions of a game at most: every action on a mission reads them all, in its transaction. */
export const MISSIONS_PER_GAME_LIMIT = 30;

/** The answers all the players together may send to one mission in a minute, against guessing. */
export const ANSWERS_PER_MINUTE = 10;

/** A mission as the game master writes it. */
export interface MissionDefinition {
  title: string;
  statement: string;
  type: MissionType;
  answers: string[];
  // The number of digits of a code, null for a text
  codeLength: number | null;
  trigger: MissionTrigger;
  isFinal: boolean;
}

const CODE_PATTERN = /^[0-9]+$/u;

function invalid(message: string): never {
  throw failure("invalid-argument", "MISSION_INVALID", message);
}

function cleanLine(raw: unknown, maxLength: number, what: string, allowEmpty: boolean): string {
  if (typeof raw !== "string") invalid(`The ${what} must be a string`);
  const text = raw.normalize("NFC").trim();
  const length = [...text].length;
  if ((!allowEmpty && length === 0) || length > maxLength) {
    invalid(`The ${what} has ${allowEmpty ? 0 : 1} to ${maxLength} characters`);
  }
  return text;
}

/**
 * A mission as sent by the app, checked. [otherTriggers] are the triggers of the other missions of
 * the game, by id: a mission after another must follow one that exists, and never itself, even
 * through others.
 */
export function validateMission(
  raw: Record<string, unknown>,
  missionId: string,
  otherTriggers: Map<string, MissionTrigger>,
): MissionDefinition {
  const title = cleanLine(raw.title, MISSION_TITLE_MAX_LENGTH, "title", false).replace(/\s+/gu, " ");
  const statement = cleanLine(raw.statement ?? "", MISSION_STATEMENT_MAX_LENGTH, "statement", true);
  const type = raw.type;
  if (type !== "CODE" && type !== "TEXT") invalid("The type is CODE or TEXT");
  if (!Array.isArray(raw.answers) || raw.answers.length === 0 || raw.answers.length > MISSION_ANSWERS_MAX) {
    invalid(`A mission has 1 to ${MISSION_ANSWERS_MAX} answers`);
  }

  let answers: string[];
  let codeLength: number | null = null;
  if (type === "CODE") {
    answers = (raw.answers as unknown[]).map((answer) => {
      if (typeof answer !== "string" || !CODE_PATTERN.test(answer)) invalid("A code is made of digits");
      return answer;
    });
    codeLength = answers[0].length;
    if (codeLength < CODE_LENGTH_MIN || codeLength > CODE_LENGTH_MAX || answers.some((answer) => answer.length !== codeLength)) {
      invalid(`The codes have the same length, from ${CODE_LENGTH_MIN} to ${CODE_LENGTH_MAX} digits`);
    }
  } else {
    answers = (raw.answers as unknown[]).map((answer) => cleanLine(answer, TEXT_ANSWER_MAX_LENGTH, "answer", false));
    if (answers.some((answer) => normalizeText(answer).length === 0)) invalid("An answer has letters or digits");
  }

  const trigger = validateTrigger(raw.trigger, missionId, otherTriggers);
  if (typeof raw.isFinal !== "boolean") invalid("isFinal is a boolean");

  return { title, statement, type, answers: [...new Set(answers)], codeLength, trigger, isFinal: raw.isFinal };
}

function validateTrigger(raw: unknown, missionId: string, otherTriggers: Map<string, MissionTrigger>): MissionTrigger {
  const trigger = raw as Record<string, unknown> | null;
  switch (trigger?.type) {
    case "MANUAL":
      return { type: "MANUAL" };
    case "ON_START":
      return { type: "ON_START" };
    case "AFTER_MISSION": {
      const target = trigger.missionId;
      if (typeof target !== "string" || !otherTriggers.has(target) || target === missionId) {
        invalid("A mission follows another mission of the game");
      }
      // Up the chain from the target: reaching this mission would make a circle nobody unlocks
      let current: string | undefined = target;
      const seen = new Set<string>();
      while (current !== undefined && !seen.has(current)) {
        if (current === missionId) invalid("A mission cannot follow itself, even through others");
        seen.add(current);
        const next = otherTriggers.get(current);
        current = next?.type === "AFTER_MISSION" ? next.missionId : undefined;
      }
      return { type: "AFTER_MISSION", missionId: target };
    }
    default:
      return invalid("The trigger is MANUAL, ON_START or AFTER_MISSION");
  }
}

/**
 * A text as compared: without case, accents nor punctuation marks around words, typographic
 * apostrophes and ligatures folded, spaces reduced. "L’Œuvre  d'Été" reads "l'oeuvre d'ete".
 */
export function normalizeText(text: string): string {
  return text
    .normalize("NFD")
    .replace(/\p{M}/gu, "")
    .replace(/[’‘`´]/gu, "'")
    .replace(/œ/gu, "oe")
    .replace(/Œ/gu, "OE")
    .replace(/æ/gu, "ae")
    .replace(/Æ/gu, "AE")
    .toLowerCase()
    .replace(/[.,;:!?"«»()]/gu, " ")
    .replace(/\s+/gu, " ")
    .trim();
}

/**
 * Whether [answer] solves a mission: a code exactly, as a string (leading zeros count), a text once
 * normalized.
 */
export function isCorrectAnswer(mission: Pick<MissionDefinition, "type" | "answers">, answer: string): boolean {
  if (mission.type === "CODE") {
    return mission.answers.includes(answer.trim());
  }
  const normalized = normalizeText(answer);
  return normalized.length > 0 && mission.answers.some((expected) => normalizeText(expected) === normalized);
}

/** The id of a mission, chosen by the app so that a creation sent again creates nothing twice. */
export function validateMissionId(raw: unknown): string {
  if (typeof raw !== "string" || !/^[A-Za-z0-9_-]{1,64}$/u.test(raw)) {
    throw failure("invalid-argument", "INVALID_REQUEST", "The mission id is 1 to 64 letters, digits, - or _");
  }
  return raw;
}
