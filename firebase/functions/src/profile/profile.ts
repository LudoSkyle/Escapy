import { randomInt } from "node:crypto";
import { DocumentData } from "firebase-admin/firestore";
import { failure } from "../core/errors";

/** The avatars the app ships, numbered from 1. */
export const AVATAR_COUNT = 50;

export const USERNAME_MIN_LENGTH = 3;
export const USERNAME_MAX_LENGTH = 20;

const DEFAULT_USERNAME_PREFIX = "Player_";
const DEFAULT_USERNAME_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
const DEFAULT_USERNAME_SUFFIX_LENGTH = 6;

// Letters of any language, digits, spaces, and _ - . between them
const USERNAME_PATTERN = /^[\p{L}\p{N}](?:[\p{L}\p{N} _.-]*[\p{L}\p{N}])?$/u;

/** What the app receives of a profile. Never an email: users/{uid} holds none. */
export interface ProfileResponse {
  username: string;
  avatarId: number;
}

/** A name until the user picks one, the same for a guest and for a member. */
export function defaultUsername(): string {
  let suffix = "";
  for (let index = 0; index < DEFAULT_USERNAME_SUFFIX_LENGTH; index++) {
    suffix += DEFAULT_USERNAME_ALPHABET[randomInt(DEFAULT_USERNAME_ALPHABET.length)];
  }
  return DEFAULT_USERNAME_PREFIX + suffix;
}

export function randomAvatarId(): number {
  return randomInt(1, AVATAR_COUNT + 1);
}

/** The username as stored: trimmed, inner spaces collapsed, in its composed Unicode form. */
export function validateUsername(raw: unknown): string {
  if (typeof raw !== "string") {
    throw failure("invalid-argument", "USERNAME_INVALID", "The username must be a string");
  }
  const username = raw.normalize("NFC").trim().replace(/\s+/gu, " ");
  const length = [...username].length;
  if (length < USERNAME_MIN_LENGTH || length > USERNAME_MAX_LENGTH || !USERNAME_PATTERN.test(username)) {
    throw failure("invalid-argument", "USERNAME_INVALID", "The username is too short, too long or has a forbidden character");
  }
  return username;
}

export function validateAvatarId(raw: unknown): number {
  if (typeof raw !== "number" || !Number.isInteger(raw) || raw < 1 || raw > AVATAR_COUNT) {
    throw failure("invalid-argument", "AVATAR_INVALID", `The avatar must be an integer from 1 to ${AVATAR_COUNT}`);
  }
  return raw;
}

export function toProfileResponse(data: DocumentData): ProfileResponse {
  return {
    username: data.username,
    avatarId: data.avatarId,
  };
}
