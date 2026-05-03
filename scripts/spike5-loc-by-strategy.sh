#!/usr/bin/env bash
# Actividad 30 — líneas de código por superficie de transporte (aproximación para el informe).
# Cuenta archivos dedicados + nota sobre código compartido (controller, QueryService, facade, UI).

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BE="${ROOT}/backend/src/main/java/com/example/ch_users_e2e_sandbox"
FE="${ROOT}/frontend/src/app"
AFF="${BE}/affiliation"

echo "Spike 5 — LoC (wc -l), workspace: ${ROOT}"
echo

count_file() {
  local f="$1"
  if [[ -f "$f" ]]; then
    wc -l < "$f" | tr -d ' '
  else
    echo "0"
  fi
}

polling_fe="$(count_file "${FE}/services/affiliation-polling.service.ts")"
sse_fe="$(count_file "${FE}/services/affiliation-sse.service.ts")"
facade_fe="$(count_file "${FE}/services/affiliation-feed.facade.ts")"
admin_ts="$(count_file "${FE}/pages/admin-affiliations/admin-affiliations.page.ts")"
admin_html="$(count_file "${FE}/pages/admin-affiliations/admin-affiliations.page.html")"
admin_scss="$(count_file "${FE}/pages/admin-affiliations/admin-affiliations.page.scss")"

ctrl="$(count_file "${AFF}/AffiliationController.java")"
sse_ctrl="$(count_file "${AFF}/AffiliationSseController.java")"
broadcaster="$(count_file "${AFF}/AffiliationSseBroadcaster.java")"
heartbeat="$(count_file "${AFF}/AffiliationSseHeartbeatScheduler.java")"
registry="$(count_file "${AFF}/SseEmitterRegistry.java")"
query="$(count_file "${AFF}/AffiliationQueryService.java")"

echo "Frontend — estrategia A (polling): affiliation-polling.service.ts  → ${polling_fe} líneas"
echo "Frontend — estrategia B (SSE):     affiliation-sse.service.ts       → ${sse_fe} líneas"
echo "Frontend — compartido:             facade + admin page (ts/html/scss)"
echo "  affiliation-feed.facade.ts       → ${facade_fe}"
echo "  admin-affiliations.page.ts       → ${admin_ts}"
echo "  admin-affiliations.page.html     → ${admin_html}"
echo "  admin-affiliations.page.scss     → ${admin_scss}"
shared_fe=$((facade_fe + admin_ts + admin_html + admin_scss))
echo "  Subtotal compartido (UI+fachada) → ${shared_fe} líneas"
echo

echo "Backend — polling path: AffiliationController (delta/list + ETag) → ${ctrl} líneas"
echo "Backend — SSE path: AffiliationSseController                      → ${sse_ctrl}"
echo "           AffiliationSseBroadcaster                             → ${broadcaster}"
echo "           AffiliationSseHeartbeatScheduler                       → ${heartbeat}"
echo "           SseEmitterRegistry                                    → ${registry}"
echo "Backend — lecturas compartidas: AffiliationQueryService         → ${query}"
sse_be=$((sse_ctrl + broadcaster + heartbeat + registry))
echo "  Subtotal backend solo-SSE (sin QueryService)                   → ${sse_be} líneas"
echo
echo "Nota metodológica: AffiliationController y AffiliationQueryService son compartidos;"
echo "el delta GET /changes es exclusivo del polling; el stream SSE es paralelo en otro controller."
