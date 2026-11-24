import { createUserWithEmailAndPassword, signInAnonymously } from "firebase/auth";
import { afterEach, beforeEach, describe, expect, test } from "vitest";
import { isRecentSignIn, RECENT_SIGN_IN_SECONDS } from "../src/account/recentSignIn";
import { adminFirestore, clearEmulators, failureOf, newClient, TestClient } from "./support/emulators";

describe("deleteAccount", () => {
  let client: TestClient;

  beforeEach(async () => {
    await clearEmulators();
    client = newClient();
  });

  afterEach(async () => {
    await client.close();
  });

  test("deletes the profile and the account of a guest", async () => {
    // Given
    const { user } = await signInAnonymously(client.auth);
    await client.call("ensureProfile");

    // When
    const result = await client.call("deleteAccount");

    // Then
    expect(result).toEqual({ deleted: true });
    expect((await adminFirestore().collection("users").doc(user.uid).get()).exists).toBe(false);
    await expect(user.reload()).rejects.toMatchObject({ code: "auth/user-token-expired" });
  });

  test("deletes the account of a member who just signed in", async () => {
    // Given
    const { user } = await createUserWithEmailAndPassword(client.auth, "member@example.com", "Escapy-2026!");
    await client.call("ensureProfile");

    // When
    await client.call("deleteAccount");

    // Then
    expect((await adminFirestore().collection("users").doc(user.uid).get()).exists).toBe(false);
  });

  test("deletes an account without a profile", async () => {
    // Given
    await signInAnonymously(client.auth);

    // When
    const result = await client.call("deleteAccount");

    // Then
    expect(result).toEqual({ deleted: true });
  });

  test("refuses a call without a signed-in user", async () => {
    // When
    const result = await failureOf(client.call("deleteAccount"));

    // Then
    expect(result).toEqual({ code: "functions/unauthenticated", reason: "UNAUTHENTICATED" });
  });
});

describe("isRecentSignIn", () => {
  const now = 1_800_000_000;

  test("accepts a sign-in up to five minutes old", () => {
    expect(isRecentSignIn(now - RECENT_SIGN_IN_SECONDS, now)).toBe(true);
  });

  test("refuses an older sign-in", () => {
    expect(isRecentSignIn(now - RECENT_SIGN_IN_SECONDS - 1, now)).toBe(false);
  });
});
