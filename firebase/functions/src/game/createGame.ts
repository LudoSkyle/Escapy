import { FieldValue } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireExistingAccount, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { db, GAMES, JOIN_CODES } from "../core/firestore";
import {
  ACTIVE_GAMES_LIMIT,
  GameStatus,
  randomJoinCode,
  validateDurationMinutes,
  validateGameTitle,
  validateMaxPlayers,
} from "./game";
import { readMember } from "./gameData";

/** A clash among 31^6 codes is rare: a few tries are plenty. */
const JOIN_CODE_TRIES = 5;

export interface CreateGameResponse {
  gameId: string;
  joinCode: string;
}

/**
 * Creates a waiting game run by the user who calls, with a code nobody else has, and returns both.
 * The user joins no game: they run it.
 */
export const createGame = onCall(async (request): Promise<CreateGameResponse> => {
  const uid = requireUid(request);
  const data = requestData(request);
  const title = validateGameTitle(data.title);
  const durationMinutes = validateDurationMinutes(data.durationMinutes);
  const maxPlayers = validateMaxPlayers(data.maxPlayers);
  await requireExistingAccount(uid);

  const running = await db().collection(GAMES).where("masterId", "==", uid).count().get();
  if (running.data().count >= ACTIVE_GAMES_LIMIT) {
    throw failure("resource-exhausted", "TOO_MANY_GAMES", `A user runs at most ${ACTIVE_GAMES_LIMIT} games at once`);
  }

  const master = await readMember(uid);
  const game = db().collection(GAMES).doc();
  for (let attempt = 0; attempt < JOIN_CODE_TRIES; attempt++) {
    const joinCode = randomJoinCode();
    const code = db().collection(JOIN_CODES).doc(joinCode);
    const created = await db().runTransaction(async (transaction) => {
      if ((await transaction.get(code)).exists) {
        return false;
      }
      const status: GameStatus = "WAITING";
      transaction.create(code, { gameId: game.id, createdAt: FieldValue.serverTimestamp() });
      transaction.create(game, {
        title,
        durationMinutes,
        maxPlayers,
        status,
        masterId: uid,
        master,
        memberIds: [uid],
        playerCount: 0,
        joinCode,
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
      });
      return true;
    });
    if (created) {
      return { gameId: game.id, joinCode };
    }
  }
  throw new Error("No free join code after several tries");
});
