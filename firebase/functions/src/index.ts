import { initializeApp } from "firebase-admin/app";
import { setGlobalOptions } from "firebase-functions/v2";

initializeApp();

// Next to the Firestore database: a function and its data in the same region
setGlobalOptions({ region: "europe-west1", maxInstances: 10 });

export { deleteAccount } from "./account/deleteAccount";
export { cancelGame } from "./game/cancelGame";
export { createGame } from "./game/createGame";
export { deleteStaleGames } from "./game/deleteStaleGames";
export { joinGame } from "./game/joinGame";
export { kickPlayer } from "./game/kickPlayer";
export { leaveGame } from "./game/leaveGame";
export { ensureProfile } from "./profile/ensureProfile";
export { updateProfile } from "./profile/updateProfile";
