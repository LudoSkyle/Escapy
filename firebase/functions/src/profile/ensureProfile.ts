import { FieldValue } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requireUid } from "../core/auth";
import { db, isAlreadyExists, USERS } from "../core/firestore";
import { defaultUsername, ProfileResponse, randomAvatarId, toProfileResponse } from "./profile";

/**
 * Creates the profile of the user who calls if it does not exist yet, and returns it. Called by
 * the app after every sign-in: safe to call again, and twice at once, it never overwrites a profile.
 */
export const ensureProfile = onCall(async (request): Promise<ProfileResponse> => {
  const uid = requireUid(request);
  const profile = db().collection(USERS).doc(uid);

  try {
    // create() fails when the document exists: the first call wins, the others read its profile
    await profile.create({
      username: defaultUsername(),
      avatarId: randomAvatarId(),
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });
  } catch (error) {
    if (!isAlreadyExists(error)) {
      throw error;
    }
  }

  const snapshot = await profile.get();
  return toProfileResponse(snapshot.data()!);
});
