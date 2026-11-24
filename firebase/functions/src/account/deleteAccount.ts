import { getAuth } from "firebase-admin/auth";
import { onCall } from "firebase-functions/v2/https";
import { isAnonymous, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { isRecentSignIn } from "./recentSignIn";
import { deleteUserData } from "./userData";

/**
 * Deletes the account of the user who calls, with all their data. A member signs in again first
 * when their sign-in is not recent; a guest cannot, and needs not: their account has no password.
 */
export const deleteAccount = onCall(async (request): Promise<{ deleted: true }> => {
  const uid = requireUid(request);

  const authTime = request.auth!.token.auth_time;
  if (!isAnonymous(request) && !isRecentSignIn(authTime, Math.floor(Date.now() / 1000))) {
    throw failure("failed-precondition", "RECENT_SIGN_IN_REQUIRED", "Sign in again before deleting the account");
  }

  // The data first: a failure leaves the account, and the user can try again
  await deleteUserData(uid);
  try {
    await getAuth().deleteUser(uid);
  } catch (error) {
    // Already deleted by a previous call that failed after it
    if ((error as { code?: string })?.code !== "auth/user-not-found") {
      throw error;
    }
  }

  return { deleted: true };
});
