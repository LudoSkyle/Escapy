import { initializeApp } from "firebase-admin/app";
import { setGlobalOptions } from "firebase-functions/v2";

initializeApp();

// Next to the Firestore database: a function and its data in the same region
setGlobalOptions({ region: "europe-west1", maxInstances: 10 });

export { deleteAccount } from "./account/deleteAccount";
export { addTime } from "./game/addTime";
export { cancelGame } from "./game/cancelGame";
export { createGame } from "./game/createGame";
export { deleteMission } from "./game/deleteMission";
export { deleteStaleGames } from "./game/deleteStaleGames";
export { endGame } from "./game/endGame";
export { expireGame } from "./game/expireGame";
export { getServerTime } from "./game/getServerTime";
export { joinGame } from "./game/joinGame";
export { kickPlayer } from "./game/kickPlayer";
export { leaveGame } from "./game/leaveGame";
export { pauseGame } from "./game/pauseGame";
export { resumeGame } from "./game/resumeGame";
export { saveMission } from "./game/saveMission";
export { startGame } from "./game/startGame";
export { submitAnswer } from "./game/submitAnswer";
export { unlockMission } from "./game/unlockMission";
export { validateMission } from "./game/validateMission";
export { ensureProfile } from "./profile/ensureProfile";
export { updateProfile } from "./profile/updateProfile";
