import { signInAnonymously } from "firebase/auth";
import { afterEach, beforeEach, describe, expect, test } from "vitest";
import { adminFirestore, clearEmulators, failureOf, newClient, TestClient } from "./support/emulators";

interface Profile {
  username: string;
  avatarId: number;
}

describe("ensureProfile", () => {
  let client: TestClient;

  beforeEach(async () => {
    await clearEmulators();
    client = newClient();
  });

  afterEach(async () => {
    await client.close();
  });

  test("refuses a call without a signed-in user", async () => {
    // When
    const result = await failureOf(client.call("ensureProfile"));

    // Then
    expect(result).toEqual({ code: "functions/unauthenticated", reason: "UNAUTHENTICATED" });
  });

  test("creates a default profile without any email", async () => {
    // Given
    const { user } = await signInAnonymously(client.auth);

    // When
    const profile = await client.call<Profile>("ensureProfile");

    // Then
    expect(profile.username).toMatch(/^Player_[A-Z2-9]{6}$/);
    expect(profile.avatarId).toBeGreaterThanOrEqual(1);
    expect(profile.avatarId).toBeLessThanOrEqual(50);
    const stored = (await adminFirestore().collection("users").doc(user.uid).get()).data();
    expect(Object.keys(stored ?? {}).sort()).toEqual(["avatarId", "createdAt", "updatedAt", "username"]);
  });

  test("never overwrites an existing profile", async () => {
    // Given
    await signInAnonymously(client.auth);
    const first = await client.call<Profile>("ensureProfile");

    // When
    const second = await client.call<Profile>("ensureProfile");

    // Then
    expect(second).toEqual(first);
  });

  test("creates a single profile when called twice at once", async () => {
    // Given
    await signInAnonymously(client.auth);

    // When
    const [first, second] = await Promise.all([
      client.call<Profile>("ensureProfile"),
      client.call<Profile>("ensureProfile"),
    ]);

    // Then
    expect(second).toEqual(first);
  });
});

describe("updateProfile", () => {
  let client: TestClient;

  beforeEach(async () => {
    await clearEmulators();
    client = newClient();
    await signInAnonymously(client.auth);
  });

  afterEach(async () => {
    await client.close();
  });

  test("changes the username and the avatar", async () => {
    // Given
    await client.call("ensureProfile");

    // When
    const profile = await client.call<Profile>("updateProfile", { username: "  Énigme   Master ", avatarId: 42 });

    // Then
    expect(profile).toEqual({ username: "Énigme Master", avatarId: 42 });
  });

  test("keeps what it is not given", async () => {
    // Given
    const before = await client.call<Profile>("ensureProfile");

    // When
    const profile = await client.call<Profile>("updateProfile", { avatarId: 7 });

    // Then
    expect(profile).toEqual({ username: before.username, avatarId: 7 });
  });

  test.each([
    ["too short", "ab"],
    ["too long", "a".repeat(21)],
    ["a forbidden character", "<script>"],
    ["only spaces", "     "],
    ["not a string", 42],
  ])("refuses a username %s", async (_case, username) => {
    // Given
    await client.call("ensureProfile");

    // When
    const result = await failureOf(client.call("updateProfile", { username }));

    // Then
    expect(result).toEqual({ code: "functions/invalid-argument", reason: "USERNAME_INVALID" });
  });

  test.each([
    ["zero", 0],
    ["above the last avatar", 51],
    ["not an integer", 2.5],
    ["not a number", "3"],
  ])("refuses an avatar %s", async (_case, avatarId) => {
    // Given
    await client.call("ensureProfile");

    // When
    const result = await failureOf(client.call("updateProfile", { avatarId }));

    // Then
    expect(result).toEqual({ code: "functions/invalid-argument", reason: "AVATAR_INVALID" });
  });

  test("leaves a field sent as null as it is", async () => {
    // Given
    const before = await client.call<Profile>("ensureProfile");

    // When
    const profile = await client.call<Profile>("updateProfile", { username: null, avatarId: 9 });

    // Then
    expect(profile).toEqual({ username: before.username, avatarId: 9 });
  });

  test("refuses a call that changes nothing", async () => {
    // Given
    await client.call("ensureProfile");

    // When
    const result = await failureOf(client.call("updateProfile", {}));

    // Then
    expect(result).toEqual({ code: "functions/invalid-argument", reason: "NOTHING_TO_UPDATE" });
  });

  test("refuses to update a profile that does not exist", async () => {
    // When
    const result = await failureOf(client.call("updateProfile", { avatarId: 3 }));

    // Then
    expect(result).toEqual({ code: "functions/not-found", reason: "PROFILE_NOT_FOUND" });
  });
});
