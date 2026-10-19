set -uo pipefail

: "${SONAR_TOKEN:?Falta SONAR_TOKEN}"
PROJECT="${SONAR_PROJECT_KEY:-julian7965_calidad-software}"
MAX_COMPLEXITY="${MAX_COMPLEXITY:-250}"
MAX_COGNITIVE="${MAX_COGNITIVE:-120}"
MIN_COVERAGE="${MIN_COVERAGE:-80}"
MAX_METHOD_CCN="${MAX_METHOD_CCN:-10}"
API="https://sonarcloud.io/api"
fail=0

resp=$(curl -sS -u "${SONAR_TOKEN}:" --get "${API}/measures/component" \
  --data-urlencode "component=${PROJECT}" \
  --data-urlencode "metricKeys=complexity,cognitive_complexity,coverage,ncloc")
if echo "$resp" | jq -e '.errors' >/dev/null 2>&1; then
  echo "No se pudieron leer las metricas: $(echo "$resp" | jq -r '.errors[0].msg')"; exit 1
fi
measure() { echo "$resp" | jq -r --arg m "$1" '.component.measures[] | select(.metric==$m) | .value' | head -n1; }

check() { # check NOMBRE VALOR OPERADOR LIMITE
  local name="$1" value="$2" op="$3" limit="$4"
  if [[ -z "$value" ]]; then echo "  [aviso] ${name}: sin dato todavia"; return; fi
  if awk -v v="$value" -v l="$limit" -v op="$op" 'BEGIN { exit !((op=="<=" && v<=l) || (op==">=" && v>=l)) }'; then
    echo "  [ok]    ${name}: ${value} (${op} ${limit})"
  else
    echo "  [FALLA] ${name}: ${value} (se exigia ${op} ${limit})"; fail=1
  fi
}

echo "== Metricas de SonarCloud para ${PROJECT} (lineas de codigo: $(measure ncloc))"
check "Complejidad ciclomatica total" "$(measure complexity)" "<=" "$MAX_COMPLEXITY"
check "Complejidad cognitiva total"   "$(measure cognitive_complexity)" "<=" "$MAX_COGNITIVE"
check "Cobertura (%)"                 "$(measure coverage)" ">=" "$MIN_COVERAGE"

echo "== Complejidad ciclomatica por metodo (lizard, tope ${MAX_METHOD_CCN})"
if command -v lizard >/dev/null 2>&1; then
  if lizard -l java -C "$MAX_METHOD_CCN" -i 0 -w src/main/java; then
    echo "  [ok]    ningun metodo supera ${MAX_METHOD_CCN}"
  else
    echo "  [FALLA] hay metodos que superan ${MAX_METHOD_CCN} (ver detalle arriba)"; fail=1
  fi
else
  echo "  [aviso] lizard no esta instalado (pip install lizard); se omite esta verificacion"
fi

if [[ $fail -ne 0 ]]; then echo "Verificacion de umbrales: FALLO"; exit 1; fi
echo "Verificacion de umbrales: OK"
