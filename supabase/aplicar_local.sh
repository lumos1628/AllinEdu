#!/usr/bin/env bash
# Aplica migraciones + semillas en un Postgres local dentro de Docker.
# No necesita la CLI de Supabase. Es el mismo orden que ejecutaría `supabase db reset`.
#
#   ./aplicar_local.sh            # levanta el contenedor y aplica todo
#   ./aplicar_local.sh --psql     # al terminar, abre una sesión psql
#
# Los parámetros de la demo (incluida la contraseña) se leen de backend/.env,
# que está en .gitignore. Este archivo no contiene ninguna credencial.
set -euo pipefail

CONTENEDOR="${CONTENEDOR:-allinedu-pg}"
PUERTO="${PUERTO:-54322}"
IMAGEN="${IMAGEN:-postgres:17-alpine}"
BD="${BD:-postgres}"
RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ---- Parámetros de la semilla ----
# Se toman de backend/.env (ignorado por git). Si no existe, se copia del
# ejemplo para que el guion funcione a la primera.
ENTORNO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/backend/.env"
if [[ ! -f "$ENTORNO" ]]; then
  cp "${ENTORNO}.example" "$ENTORNO"
  echo "Creado $ENTORNO a partir del ejemplo. Cámbiale la contraseña de demo si vas a compartir la base."
fi
# De backend/.env solo se toma la credencial de la demo. Lo demas describe la
# IE simulada y vive aqui, porque backend/.env trae los valores genericos del
# backend (10 estudiantes por seccion, dominio example.test) y encogerian la
# institucion.
set -a; source "$ENTORNO"; set +a

if [[ -z "${ALLINEDU_DEMO_PASSWORD:-}" ]]; then
  echo "Falta ALLINEDU_DEMO_PASSWORD. Definela en $ENTORNO antes de continuar." >&2
  exit 1
fi

# El contenedor es desechable y local; la contrasena de Postgres no es un secreto.
export PGPASSWORD="${PGPASSWORD:-postgres}"

# ---- La IE simulada: 12 secciones de 25, como la del prototipo ----
# Cada constante se puede sobreescribir desde la linea de ordenes, p. ej.
#   ESTUDIANTES_POR_SECCION=10 ./supabase/aplicar_local.sh
export ALLINEDU_DEMO_STUDENTS_PER_SECTION="${ESTUDIANTES_POR_SECCION:-25}"
export ALLINEDU_DEMO_SIBLING_PAIRS="${PAREJAS_DE_HERMANOS:-6}"
export ALLINEDU_DEMO_EMAIL_DOMAIN="${DOMINIO_CORREO:-allinedu.test}"
export ALLINEDU_DEMO_SCHOOL_YEAR="${ANIO_ESCOLAR:-2026}"
export ALLINEDU_DEMO_START_DATE="${INICIO_ANIO:-2026-03-16}"
export ALLINEDU_DEMO_END_DATE="${FIN_ANIO:-2026-12-18}"

paso() { printf '\n\033[1;33m==> %s\033[0m\n' "$*"; }

paso "IE simulada: 12 secciones de $ALLINEDU_DEMO_STUDENTS_PER_SECTION, dominio $ALLINEDU_DEMO_EMAIL_DOMAIN"

paso "Contenedor $CONTENEDOR ($IMAGEN) en el puerto $PUERTO"
docker rm -f "$CONTENEDOR" >/dev/null 2>&1 || true
docker run -d --name "$CONTENEDOR" \
  -e POSTGRES_PASSWORD="$PGPASSWORD" \
  -p "$PUERTO:5432" "$IMAGEN" >/dev/null

printf 'esperando a Postgres'
for _ in $(seq 1 60); do
  if docker exec "$CONTENEDOR" pg_isready -U postgres -q 2>/dev/null; then echo ' listo'; break; fi
  printf '.'; sleep 1
done

PSQL=(psql -h 127.0.0.1 -p "$PUERTO" -U postgres -d "$BD" -v ON_ERROR_STOP=1 -q)

paso "Esquema extensions (lo crea Supabase; aquí hay que crearlo a mano)"
"${PSQL[@]}" -c 'create schema if not exists extensions;'

paso "Migraciones"
for f in "$RAIZ"/migrations/*.sql; do
  printf '    %s\n' "$(basename "$f")"
  "${PSQL[@]}" -f "$f"
done

paso "Semillas"
for f in seed.sql seed_primaria.sql; do
  printf '    %s\n' "$f"
  "${PSQL[@]}" -f "$RAIZ/$f"
done

paso "Resumen"
"${PSQL[@]}" -P pager=off -c "
select 'competencias'           as tabla, count(*) from public.competencias
union all select 'capacidades',            count(*) from public.capacidades
union all select 'estandares_aprendizaje', count(*) from public.estandares_aprendizaje
union all select 'desempenos_grado',       count(*) from public.desempenos_grado
union all select 'areas_curriculares',     count(*) from public.areas_curriculares
union all select 'personas',               count(*) from public.personas
union all select 'cuentas_usuario',        count(*) from public.cuentas_usuario
union all select 'estudiantes',            count(*) from public.estudiantes
union all select 'secciones activas',      count(*) from public.secciones where activa
union all select 'ofertas_curriculares',   count(*) from public.ofertas_curriculares
union all select 'horarios_seccion',       count(*) from public.horarios_seccion
union all select 'experiencias_aprendizaje', count(*) from public.experiencias_aprendizaje
union all select 'sesiones_planificadas',  count(*) from public.sesiones_planificadas
union all select 'actividades_sesion',     count(*) from public.actividades_sesion
order by 1;"

paso "Cuentas de acceso por rol"
"${PSQL[@]}" -P pager=off -c "
select r.codigo as rol, count(*) as cuentas,
       min(cu.correo) as ejemplo
  from public.cuentas_usuario cu
  join public.asignaciones_roles ar on ar.cuenta_usuario_id = cu.id
                                  and ar.vigente_hasta is null
  join public.roles r on r.id = ar.rol_id
 group by r.codigo order by 2 desc, 1;"

cat <<FIN

Entran con los correos de arriba y la ALLINEDU_DEMO_PASSWORD de $ENTORNO.
La docente protagonista del prototipo es tutor.4b@$ALLINEDU_DEMO_EMAIL_DOMAIN.

Cadena de conexión:
  postgresql://postgres:$PGPASSWORD@127.0.0.1:$PUERTO/$BD

Para entrar:
  psql "postgresql://postgres:$PGPASSWORD@127.0.0.1:$PUERTO/$BD"

Para apagar y borrar:
  docker rm -f $CONTENEDOR
FIN

if [[ "${1:-}" == "--psql" ]]; then exec "${PSQL[@]}" -q; fi
