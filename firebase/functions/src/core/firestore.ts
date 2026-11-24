import { getFirestore } from "firebase-admin/firestore";

export const USERS = "users";

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
