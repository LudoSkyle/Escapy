/** How long after a sign-in an account may be deleted without signing in again. */
export const RECENT_SIGN_IN_SECONDS = 5 * 60;

/**
 * Whether the user signed in a short while ago. The Admin SDK deletes an account whatever its
 * sign-in time: this keeps the protection the client SDKs have, against a phone left unlocked.
 */
export function isRecentSignIn(authTimeSeconds: number, nowSeconds: number): boolean {
  return nowSeconds - authTimeSeconds <= RECENT_SIGN_IN_SECONDS;
}
