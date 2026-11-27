import { deleteApp, FirebaseApp, initializeApp } from "firebase/app";
import { Auth, connectAuthEmulator, getAuth } from "firebase/auth";
import { connectFunctionsEmulator, getFunctions, httpsCallable } from "firebase/functions";
import { getApps as getAdminApps, initializeApp as initializeAdminApp } from "firebase-admin/app";
import { getFirestore as getAdminFirestore } from "firebase-admin/firestore";

/** The demo project of the emulators: it needs no credentials and can never reach the real one. */
export const PROJECT_ID = "demo-escapy";
export const REGION = "europe-west1";

/**
 * The port of an emulator, from the host emulators:exec gives the tests, or the port of
 * firebase.json: the tests may run on emulators of their own, beside those the app uses.
 */
function portOf(variable: string, fallback: number): number {
  const host = process.env[variable];
  return host ? Number(host.split(":").pop()) : fallback;
}

export const AUTH_PORT = portOf("FIREBASE_AUTH_EMULATOR_HOST", 9099);
export const FIRESTORE_PORT = portOf("FIRESTORE_EMULATOR_HOST", 8080);
export const STORAGE_PORT = portOf("FIREBASE_STORAGE_EMULATOR_HOST", 9199);
export const FUNCTIONS_PORT = Number(process.env.ESCAPY_FUNCTIONS_EMULATOR_PORT ?? 5001);

/** A client of the emulators, as the app is: its own Auth session, its own calls. */
export interface TestClient {
  app: FirebaseApp;
  auth: Auth;
  call<T = unknown>(name: string, data?: unknown): Promise<T>;
  close(): Promise<void>;
}

let clientCount = 0;

export function newClient(): TestClient {
  const app = initializeApp({ projectId: PROJECT_ID, apiKey: "demo-api-key" }, `client-${clientCount++}`);
  const auth = getAuth(app);
  connectAuthEmulator(auth, `http://127.0.0.1:${AUTH_PORT}`, { disableWarnings: true });
  const functions = getFunctions(app, REGION);
  connectFunctionsEmulator(functions, "127.0.0.1", FUNCTIONS_PORT);
  return {
    app,
    auth,
    async call<T>(name: string, data?: unknown): Promise<T> {
      const result = await httpsCallable(functions, name)(data);
      return result.data as T;
    },
    close: () => deleteApp(app),
  };
}

/** The Admin SDK on the emulators (their hosts are set by emulators:exec), to check what was written. */
export function adminFirestore() {
  if (getAdminApps().length === 0) {
    initializeAdminApp({ projectId: PROJECT_ID });
  }
  return getAdminFirestore();
}

/** Deletes every document and every account of the emulators, between two tests. */
export async function clearEmulators(): Promise<void> {
  await fetch(`http://127.0.0.1:${FIRESTORE_PORT}/emulator/v1/projects/${PROJECT_ID}/databases/(default)/documents`, { method: "DELETE" });
  await fetch(`http://127.0.0.1:${AUTH_PORT}/emulator/v1/projects/${PROJECT_ID}/accounts`, { method: "DELETE" });
}

/** The reason a call was refused with, from the details the function sent. */
export async function failureOf(call: Promise<unknown>): Promise<{ code: string; reason: string | undefined }> {
  try {
    await call;
  } catch (error) {
    const functionsError = error as { code?: string; details?: { reason?: string } };
    return { code: functionsError.code ?? "", reason: functionsError.details?.reason };
  }
  throw new Error("The call succeeded");
}
