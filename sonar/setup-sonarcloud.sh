set -uo pipefail

: "${SONAR_TOKEN:?Falta SONAR_TOKEN}"
: "${SONAR_ORGANIZATION:?Falta SONAR_ORGANIZATION}"
: "${SONAR_PROJECT_KEY:?Falta SONAR_PROJECT_KEY}"

API="https://sonarcloud.io/api"
GATE_NAME="Calculadora Quality Gate"
PROFILE_NAME="Calculadora Java Profile"
BASE_PROFILE="Sonar way"

GATE_CONDITIONS=(
  "new_coverage|LT|80"                       # cobertura del codigo nuevo >= 80 %
  "coverage|LT|80"                           # cobertura total >= 80 %
  "new_violations|GT|0"                      # sin issues nuevos (incluye las reglas de complejidad del profile)
  "new_duplicated_lines_density|GT|3"        # duplicacion en codigo nuevo <= 3 %
  "new_reliability_rating|GT|1"              # fiabilidad nueva = A
  "new_security_rating|GT|1"                 # seguridad nueva = A
  "new_maintainability_rating|GT|1"          # mantenibilidad nueva = A
  "new_security_hotspots_reviewed|LT|100"    # 100 % de hotspots revisados
  "complexity|GT|250"                        # complejidad ciclomatica total <= 250
  "cognitive_complexity|GT|120"              # complejidad cognitiva total <= 120
)

# Quality Profile: regla | severidad | parametros
PROFILE_RULES=(
  "java:S1541|MAJOR|Max=10"                  # complejidad ciclomatica por metodo <= 10
  "java:S3776|CRITICAL|threshold=15"         # complejidad cognitiva por metodo  <= 15
)
# -----------------------------------------------------------------------------

call() { # call METHOD ENDPOINT [curl args...]
  local method="$1" endpoint="$2"; shift 2
  curl -sS -u "${SONAR_TOKEN}:" -X "$method" "${API}/${endpoint}" "$@"
}

echo "== 1. Quality Gate: ${GATE_NAME}"
resp=$(call POST qualitygates/create --data-urlencode "name=${GATE_NAME}" --data-urlencode "organization=${SONAR_ORGANIZATION}")
echo "$resp" | jq -r 'if .errors then "   (aviso) " + (.errors[0].msg) else "   creado" end'

for cond in "${GATE_CONDITIONS[@]}"; do
  cond="${cond%%#*}"; cond="$(echo "$cond" | xargs)"      # quita comentarios y espacios
  IFS='|' read -r metric op error <<<"$cond"
  resp=$(call POST qualitygates/create_condition \
    --data-urlencode "gateName=${GATE_NAME}" --data-urlencode "organization=${SONAR_ORGANIZATION}" \
    --data-urlencode "metric=${metric}" --data-urlencode "op=${op}" --data-urlencode "error=${error}")
  echo "$resp" | jq -r --arg m "$metric" 'if .errors then "   [\($m)] " + .errors[0].msg else "   [\($m)] condicion creada" end'
done

echo "== 2. Asociar el Quality Gate al proyecto ${SONAR_PROJECT_KEY}"
call POST qualitygates/select \
  --data-urlencode "gateName=${GATE_NAME}" --data-urlencode "organization=${SONAR_ORGANIZATION}" \
  --data-urlencode "projectKey=${SONAR_PROJECT_KEY}" | jq -r 'if .errors then "   " + .errors[0].msg else "   ok" end'

echo "== 3. Quality Profile: ${PROFILE_NAME} (copia de '${BASE_PROFILE}')"
base_key=$(call GET qualityprofiles/search --get \
  --data-urlencode "organization=${SONAR_ORGANIZATION}" --data-urlencode "language=java" \
  --data-urlencode "qualityProfile=${BASE_PROFILE}" | jq -r '.profiles[0].key')
if [[ -z "$base_key" || "$base_key" == "null" ]]; then echo "   No se encontro el profile base '${BASE_PROFILE}'"; exit 1; fi

call POST qualityprofiles/copy \
  --data-urlencode "fromKey=${base_key}" --data-urlencode "toName=${PROFILE_NAME}" \
  | jq -r 'if .errors then "   (aviso) " + .errors[0].msg else "   copiado" end'

profile_key=$(call GET qualityprofiles/search --get \
  --data-urlencode "organization=${SONAR_ORGANIZATION}" --data-urlencode "language=java" \
  --data-urlencode "qualityProfile=${PROFILE_NAME}" | jq -r '.profiles[0].key')
if [[ -z "$profile_key" || "$profile_key" == "null" ]]; then echo "   No se pudo obtener la clave del profile"; exit 1; fi

echo "== 4. Activar reglas de complejidad con sus umbrales"
for item in "${PROFILE_RULES[@]}"; do
  item="${item%%#*}"; item="$(echo "$item" | xargs)"
  IFS='|' read -r rule severity params <<<"$item"
  resp=$(call POST qualityprofiles/activate_rule \
    --data-urlencode "key=${profile_key}" --data-urlencode "rule=${rule}" \
    --data-urlencode "severity=${severity}" --data-urlencode "params=${params}")
  echo "$resp" | jq -r --arg r "$rule" --arg p "$params" 'if .errors then "   [\($r)] " + .errors[0].msg else "   [\($r)] activada con \($p)" end'
done

echo "== 5. Asociar el Quality Profile al proyecto"
call POST qualityprofiles/add_project \
  --data-urlencode "key=${profile_key}" --data-urlencode "project=${SONAR_PROJECT_KEY}" \
  --data-urlencode "organization=${SONAR_ORGANIZATION}" | jq -r 'if .errors then "   " + .errors[0].msg else "   ok" end'

echo "Listo. Verifica en https://sonarcloud.io/project/quality_gate?id=${SONAR_PROJECT_KEY}"
