import { FieldValue } from "firebase-admin/firestore";
import { onCall } from "firebase-functions/v2/https";
import { requestData, requireUid } from "../core/auth";
import { failure } from "../core/errors";
import { db, isNotFound, USERS } from "../core/firestore";
import { ProfileResponse, toProfileResponse, validateAvatarId, validateUsername } from "./profile";

/** Changes the username, the avatar, or both, of the user who calls, and returns the profile. */
export const updateProfile = onCall(async (request): Promise<ProfileResponse> => {
  const uid = requireUid(request);
  const data = requestData(request);

  const changes: Record<string, unknown> = {};
  if (data.username !== undefined) {
    changes.username = validateUsername(data.username);
  }
  if (data.avatarId !== undefined) {
    changes.avatarId = validateAvatarId(data.avatarId);
  }
  if (Object.keys(changes).length === 0) {
    throw failure("invalid-argument", "NOTHING_TO_UPDATE", "Give a username, an avatar, or both");
  }

  const profile = db().collection(USERS).doc(uid);
  try {
    await profile.update({ ...changes, updatedAt: FieldValue.serverTimestamp() });
  } catch (error) {
    if (isNotFound(error)) {
      throw failure("not-found", "PROFILE_NOT_FOUND", "Call ensureProfile first");
    }
    throw error;
  }

  const snapshot = await profile.get();
  return toProfileResponse(snapshot.data()!);
});
