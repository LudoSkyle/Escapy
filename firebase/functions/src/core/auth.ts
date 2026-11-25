import { getAuth } from "firebase-admin/auth";
import { CallableRequest } from "firebase-functions/v2/https";
import { failure } from "./errors";

/** The user who calls, refused when the call carries no Firebase Auth token. */
export function requireUid(request: CallableRequest<unknown>): string {
  const uid = request.auth?.uid;
  if (!uid) {
    throw failure("unauthenticated", "UNAUTHENTICATED", "The call needs a signed-in user");
  }
  return uid;
}

/**
 * Refuses a user whose account was deleted. Their ID token stays valid until it expires, up to an
 * hour: a callable that creates data checks the account first, or would create it for nobody.
 */
export async function requireExistingAccount(uid: string): Promise<void> {
  try {
    await getAuth().getUser(uid);
  } catch (error) {
    if ((error as { code?: string })?.code === "auth/user-not-found") {
      throw failure("unauthenticated", "UNAUTHENTICATED", "The account of this user was deleted");
    }
    throw error;
  }
}

/** Whether the token comes from a guest, signed in without an email. */
export function isAnonymous(request: CallableRequest<unknown>): boolean {
  return request.auth?.token.firebase.sign_in_provider === "anonymous";
}

/** The data of a call, refused when it is not an object. */
export function requestData(request: CallableRequest<unknown>): Record<string, unknown> {
  const data = request.data ?? {};
  if (typeof data !== "object" || Array.isArray(data)) {
    throw failure("invalid-argument", "INVALID_REQUEST", "The data of the call must be an object");
  }
  return data as Record<string, unknown>;
}
