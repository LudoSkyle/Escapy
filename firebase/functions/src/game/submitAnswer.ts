import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { PLAYERS } from "../core/firestore";
import { ANSWERS_PER_MINUTE, isCorrectAnswer, SUBMITTED_ANSWER_MAX_LENGTH, validateMissionId } from "./mission";
import { answersThisMinute, countedAnswer, masterMissionOf, readMissions, solve } from "./missionData";
import { onGame, requireStarted } from "./timer";

export interface SubmitAnswerResponse {
  correct: boolean;
}

/**
 * Checks the answer of a member to an active mission of a running game, by the server only: the
 * answer is right or wrong, nothing more. A right answer solves the mission, with what follows. The
 * answers to one mission are limited per minute, all the players together, against guessing. A wrong
 * answer is an answer, not a failure: it counts.
 */
export const submitAnswer = onCall(async (request): Promise<SubmitAnswerResponse> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const missionId = validateMissionId(data.missionId);
  const answer = data.answer;
  if (typeof answer !== "string" || answer.length > SUBMITTED_ANSWER_MAX_LENGTH) {
    throw failure("invalid-argument", "INVALID_REQUEST", `An answer is a string of ${SUBMITTED_ANSWER_MAX_LENGTH} characters at most`);
  }

  const outcome = await onGame(uid, data.gameId, false, async ({ transaction, game, data: current, nowMs }) => {
    if (current.status === "FINISHED") return "FINISHED" as const;
    requireStarted(current);
    if (current.status === "PAUSED") {
      throw failure("failed-precondition", "GAME_PAUSED", "The game is paused: answers wait for it to go on");
    }
    // Reads first: the player's name, for who solved it, then the missions
    const player = current.masterId === uid ? null : await transaction.get(game.collection(PLAYERS).doc(uid));
    const missions = await readMissions(transaction, game);
    const mission = missions.find((candidate) => candidate.id === missionId);
    // A locked mission does not exist for the players: it reads as no mission at all
    if (!mission || mission.data.state === "LOCKED") {
      throw failure("not-found", "MISSION_NOT_FOUND", "No mission the players see has this id");
    }
    if (mission.data.state === "SOLVED") return true;
    if (answersThisMinute(mission.data, nowMs) >= ANSWERS_PER_MINUTE) {
      throw failure("resource-exhausted", "TOO_MANY_ANSWERS", `${ANSWERS_PER_MINUTE} answers a minute at most`);
    }

    const counted = countedAnswer(mission.data, nowMs);
    if (!isCorrectAnswer(mission.data as { type: "CODE" | "TEXT"; answers: string[] }, answer)) {
      transaction.update(masterMissionOf(game, missionId), counted);
      return false;
    }
    const username = player === null
      ? (current.master as { username: string }).username
      : (player.data()?.username as string | undefined) ?? "";
    solve(transaction, game, current, missions, mission, { userId: uid, username }, nowMs, counted);
    return true;
  });

  if (outcome === "FINISHED") {
    throw failure("failed-precondition", "GAME_FINISHED", "The game is over");
  }
  return { correct: outcome };
});
