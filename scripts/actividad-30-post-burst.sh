#!/usr/bin/env bash
# Actividad 30 — Spike 5: ráfaga reproducible de 10 POST /api/affiliations (~3 s entre cada uno).
# Requisitos: bash, curl, python3 (stdlib).
# Uso: backend en marcha; en el admin elegir polling o SSE; ejecutar este script y leer
# media/mediana en la UI (ion-note). Ver también scripts/spike5-loc-by-strategy.sh

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
INTERVAL_SEC="${INTERVAL_SEC:-3}"
COUNT="${COUNT:-10}"

echo "POST burst → ${BASE_URL}/api/affiliations (${COUNT} requests, ${INTERVAL_SEC}s apart)"
echo

for i in $(seq 1 "${COUNT}"); do
  body="$(
    REQUEST_INDEX="$i" python3 <<'PY'
import json, os, time
i = int(os.environ["REQUEST_INDEX"])
print(json.dumps({
    "fullName": f"Medición A30 #{i}",
    "email": f"act30.{i}.{int(time.time())}@example.com",
    "reason": f"Actividad 30 — burst empírico #{i}",
}))
PY
  )"

  code=$(curl -sS -o /tmp/act30-post-body.json -w '%{http_code}' \
    -X POST "${BASE_URL}/api/affiliations" \
    -H 'Content-Type: application/json' \
    -d "${body}")

  summary="$(
    python3 <<'PY'
import json
try:
    with open("/tmp/act30-post-body.json") as f:
        d = json.load(f)
    print(json.dumps({"id": d.get("id"), "createdAt": d.get("createdAt")}))
except Exception as e:
    print("(parse error)", e)
PY
  )"

  echo "[${i}/${COUNT}] HTTP ${code} — ${summary}"

  if [[ "${i}" -lt "${COUNT}" ]]; then
    sleep "${INTERVAL_SEC}"
  fi
done

echo
echo "=== Actuator (copiar/pegar para idle test tras 60 s / 5 min) ==="
echo "curl -sS '${BASE_URL}/actuator/metrics/http.server.requests'"
echo "curl -sS '${BASE_URL}/actuator/threads'"
echo "curl -sS '${BASE_URL}/actuator/health'"
