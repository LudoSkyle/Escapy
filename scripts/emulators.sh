#!/usr/bin/env bash
# Starts the Firebase emulators on the demo project, which needs no credentials and can never reach
# the real one, with the Cloud Functions built. Every Android device connected gets its ports
# forwarded: an app targeting Android 17 cannot reach the Mac through 10.0.2.2, it uses localhost.
set -euo pipefail

cd "$(dirname "$0")/../firebase/functions"
npm run build

if command -v adb >/dev/null 2>&1; then
  for serial in $(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }'); do
    for port in 9099 8080 5001 9199; do
      adb -s "$serial" reverse "tcp:$port" "tcp:$port" >/dev/null
    done
    echo "Ports of the emulators forwarded to $serial"
  done
fi

exec npx firebase --config ../firebase.json emulators:start --project demo-escapy
