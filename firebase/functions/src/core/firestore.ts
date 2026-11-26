import { getFirestore } from "firebase-admin/firestore";

export const USERS = "users";
export const GAMES = "games";
export const PLAYERS = "players";
export const JOIN_CODES = "joinCodes";
export const JOIN_ATTEMPTS = "joinAttempts";

export function db() {
  return getFirestore();
}

// The gRPC status codes the Admin SDK sets on its errors
const NOT_FOUND = 5;
const ALREADY_EXISTS = 6;

export function isAlreadyExists(error: unknown): boolean {
  return (error as { code?: unknown })?.code === ALREADY_EXISTS;
}

export function isNotFound(error: unknown): boolean {
  return (error as { code?: unknown })?.code === NOT_FOUND;
}
