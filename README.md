# AllinEdu

> Plataforma web y móvil que reduce la carga operativa del docente en la preparación,
> aplicación y evaluación de sus clases, y convierte la evidencia de ese ciclo en apoyo para
> reforzar aprendizajes.

**ODS 4** Educación de calidad · **ODS 8** Trabajo decente

---

## 🎯 El problema, con cifras

De una docente de primaria pública entrevistada en profundidad (`n=1`, es un caso, no una
muestra — ver [`EVIDENCIA.md`](EVIDENCIA.md)):

| Dato | Valor |
|---|---|
| Preparar 2 sesiones + 2 fichas diferenciadas, en casa | **2.5 a 4 horas por día** |
| Trabajo no remunerado en casa | **≈14 a 22 horas por semana** |
| Hora a la que se acuesta | **2:00 – 3:30 a.m.** (se levanta 5:30 a.m.) |
| Atención a apoderados | **8:00 a.m. – 11:00 p.m.**, incluidos domingos |
| Tiempo lectivo perdido por charlas imprevistas | 2 horas por evento |

Y cuatro hallazgos que explican más que los tiempos:

1. **Ya usa IA** (Claude, Gemini, ChatGPT con prompts propios). El dolor no es la falta de IA:
   es que la IA genérica le resuelve el texto y la deja sola con la corrección, las imágenes, el
   instrumento de evaluación y la impresión. Cuatro herramientas, cuatro horas.
2. **La asistencia se registra dos veces**: marca el cartel de papel, le toma una foto, y en
   casa la vuelca a un registro auxiliar también de papel.
3. **Evalúa de memoria**: en la noche revisa las fichas físicas y evoca quién participó.
4. **Responde a las 11 p.m. por temor** a reportes negativos de desempeño, no por
   disponibilidad. Necesita constancia de que respondió, no un chat más rápido.

---

## 🔗 La cadena

```
  PLANIFICAR              APLICAR EN CLASE           EVALUAR
  unidad → sesión    ──►  asistencia            ──►  criterios por
  ficha diferenciada      evidencia por criterio     competencia
  instrumento                   │                         │
                                │                         ▼
                                ▼                   EVIDENCIA POR
                         COMUNICACIÓN                COMPETENCIA
                         CON FAMILIAS                      │
                         (constancia                       ├──► refuerzo y repaso espaciado
                          y horario)                        │
                                                            └──► conclusiones descriptivas
```

No son dos módulos pegados: el dolor más caro en horas (las conclusiones descriptivas) solo se
puede atacar si existe evidencia por competencia, y esa evidencia la produce el ciclo de clase.

El alcance completo, con lo que entra, lo que no y por qué, está en
[`ALCANCE.md`](ALCANCE.md).

**Regla permanente:** la IA propone borradores, el docente aprueba. Nunca calificación ni
decisiones pedagógicas automáticas.

---

## 🧪 Estado del código

| Pieza | Estado |
|---|---|
| Login con JWT y roles | Funcionando |
| Consultar secciones y estudiantes | Funcionando |
| Registrar y consultar asistencia (web y móvil) | Funcionando |
| Pipeline de preparación de clases | **Parcial** — escritorio, unidad, sesión editable con aprobación, instrumento imprimible y **el telar**: la unidad completa se prepara en lote y se revisa respondiendo pocas preguntas («Está bien» / «Cámbialo»). Falta la ficha diferenciada |
| Captura de evidencia por criterio | Por construir |
| Banco de ítems, diagnóstico y repaso | Por construir |
| Canal con familias | Por construir |

El código usa el identificador `AllinEdu` (paquetes `com.allinedu`). Solo el rol `edufast` de
la base de datos y el código de la IE demo (`DEMO-EDUFAST`) conservan el nombre anterior, porque
viven en el Supabase compartido (decisión D-2 de [`ALCANCE.md`](ALCANCE.md)).

El esquema de base de datos ya anticipa buena parte de la cadena: las tablas de currículo,
planificación, actividades y notificaciones existen en `supabase/migrations/` aunque el código
todavía no las use.

---

## 🏗️ Arquitectura

Monorepo con **Clean Architecture estricta** (dominio 100% puro, puertos y adaptadores). Las 3
piezas se comunican por **HTTP + JSON**; los frontends **nunca tocan la base de datos**.

```
┌────────────┐  HTTP + JSON  ┌──────────────────┐   JPA/SQL   ┌────────────┐
│  React     │ ────────────► │ Backend (Spring) │ ──────────► │ PostgreSQL │
│  (web)     │ ◄──────────── │  (Java + Boot)   │ ◄────────── │            │
└────────────┘               └──────────────────┘             └────────────┘
┌────────────┐      ▲          un solo backend
│  Kotlin    │ ─────┘          sirve a los dos
│  (móvil)   │ ◄─────          frontends
└────────────┘     HTTP + JSON
```

El backend está organizado en 3 capas con dependencias que solo apuntan hacia adentro:

```
infrastructure/  (controller, persistence, security)  ← el mundo exterior
     │
     ▼
application/     (service, dto)                       ← la lógica de negocio
     │
     ▼
domain/          (model, port, exception)             ← puro, sin frameworks
```

Las reglas de capas están protegidas por **tests automáticos** (ArchUnit): si alguien las
rompe, `./gradlew test` falla.

### Estructura del monorepo

```
AllinEdu/
├── backend/         ← Java 25 + Spring Boot (API REST)
├── mobile/          ← Kotlin + Jetpack Compose
├── web/             ← React + Vite + TypeScript
├── supabase/        ← migraciones y semilla sintética
├── CURSOS/          ← material de los 3 cursos donde se presenta el proyecto
├── EVIDENCIA.md     ← 🔬 investigación: línea base, insights, limitaciones
├── ALCANCE.md       ← 🎯 qué entra, qué no y por qué
├── USE_CASES.md     ← flujos y actores
├── ARCHITECTURE.md  ← 📚 la arquitectura explicada desde cero
├── DATABASE.md      ← ERD, reglas y diccionario de datos
├── AGENTS.md        ← 🤖 reglas obligatorias para agentes de IA y colaboradores
└── README.md
```

---

## 📚 Documentación

| Documento | Para quién | Contenido |
|---|---|---|
| [`EVIDENCIA.md`](EVIDENCIA.md) | Todos | Línea base de tiempos, insights, contradicciones abiertas y límites metodológicos. **Toda cifra del proyecto se rastrea aquí** |
| [`ALCANCE.md`](ALCANCE.md) | Producto | La cadena, el catálogo de funcionalidades, exclusiones con razón, decisiones pendientes e historial de cambios de alcance |
| [`USE_CASES.md`](USE_CASES.md) | Producto y desarrollo | Actores, flujos y reglas transversales |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Quien aprende arquitectura | La arquitectura con analogías, el viaje de una petición real, evaluación honesta y tutorial para agregar features |
| [`DATABASE.md`](DATABASE.md) | Producto y desarrollo | Diagrama ER, tablas, restricciones, seguridad y migraciones |
| [`AGENTS.md`](AGENTS.md) | Agentes de IA y colaboradores | Reglas obligatorias, dónde va cada archivo, checklist y errores a evitar |

---

## ▶️ Cómo correr el proyecto

### Requisitos
- Java 25, Node 20+, PostgreSQL 16 (local, puerto 5432).

### Configuración para colaboradores

1. Copia la plantilla y completa los valores privados:
   ```bash
   cp backend/.env.example backend/.env
   ```
   Gradle carga `backend/.env` solo para `./gradlew test` y `./gradlew bootRun`; no hace falta exportarlo a mano. Si vas a ejecutar comandos `psql` o `supabase` CLI, expórtalo tú:
   ```bash
   set -a && source backend/.env && set +a
   ```
2. El backend ya apunta por defecto al **Supabase de desarrollo** (el host y el usuario no son secretos y viven en `application.properties`). La plantilla trae los **valores compartidos** (`ALLINEDU_DEMO_EMAIL_DOMAIN=example.test`, `ALLINEDU_DEMO_PASSWORD=demo123`). Solo debes completar los campos **privados** en `backend/.env`:
   - `DB_PASSWORD`: contraseña del Supabase de desarrollo, que se comparte por un canal privado. El `clone` del repositorio **no** concede acceso al proyecto.
   - `SUPABASE_DB_URL`: URI para `psql` (supabase/seed.sql), también privada.
   - `JWT_SECRET`: genera uno propio con `openssl rand -hex 32`.
3. No re-ejecutes la semilla en la base compartida sin acordarlo, porque cambiaría los datos para todo el equipo.

> Para trabajar contra un **Postgres local**, define `DB_URL`, `DB_USER` y `DB_PASSWORD` en `backend/.env`; esos valores ganan sobre el default de Supabase.

### Base de datos local (desarrollo)
```sql
CREATE ROLE edufast WITH LOGIN PASSWORD 'edufast123' BYPASSRLS;
CREATE DATABASE edufast OWNER edufast;
```
Si el rol `edufast` ya existía con otra contraseña, alínealo en vez de crearlo:
```sql
ALTER ROLE edufast WITH LOGIN PASSWORD 'edufast123' BYPASSRLS;
```
Aplica migraciones y semilla sobre esa base:
```bash
psql -d edufast -f supabase/migrations/20260928000100_base_escolar.sql
psql -d edufast -f supabase/migrations/20260928000200_curriculo_planificacion.sql
psql -d edufast -f supabase/migrations/20260928000300_actividades_notificaciones.sql
psql -d edufast -f supabase/migrations/20260928000400_roles_y_alcances_personal.sql
cp backend/.env.example backend/.env
set -a && source backend/.env && set +a
psql -d edufast -f supabase/seed.sql
```
El rol `edufast` usa `BYPASSRLS` para comportarse como el rol de servicio del backend.
Hibernate valida el esquema, no lo modifica.

### Migraciones Supabase del esquema

Hay dos semillas y se ejecutan en este orden: `seed.sql` crea la estructura de
demostración genérica y `seed_primaria.sql` la convierte en una IE de primaria
de 12 secciones con la docente de 4.º B como protagonista. `config.toml` deja
`[db.seed] enabled = false` a propósito, porque las semillas leen parámetros del
entorno local: `db reset` no las corre y hay que lanzarlas con `psql`.

**Sin Supabase CLI** (solo Docker y `psql`), que es el camino corto para ver la
base de datos funcionando:

```bash
./supabase/aplicar_local.sh
```

Levanta un Postgres 17 en el puerto 54322, aplica las migraciones en orden,
corre las dos semillas y muestra un resumen de lo cargado. Las variables de la
demo (`ALLINEDU_DEMO_*`) tienen valores por defecto y se pueden sobreescribir
desde el entorno. El script es reaplicable: no duplica filas.

**Con Supabase CLI instalado** y Docker activo, para tener además Studio:

```bash
cp backend/.env.example backend/.env
# Completa los valores locales en backend/.env antes de continuar.
set -a
source backend/.env
set +a
supabase start
supabase db reset
psql 'postgresql://postgres:postgres@127.0.0.1:54322/postgres' \
  -v ON_ERROR_STOP=1 -f supabase/seed.sql -f supabase/seed_primaria.sql
```

Si `supabase` no está en el PATH, `npx supabase@latest <orden>` funciona igual
sin instalar nada global.

Para aplicar migraciones al proyecto Supabase compartido de desarrollo, autentícate y enlázalo:

```bash
npx supabase@latest login
npx supabase@latest link --project-ref <DEV_PROJECT_REF>
npx supabase@latest db push --dry-run
npx supabase@latest db push
cp backend/.env.example backend/.env
# Completa las credenciales y los parámetros locales en backend/.env.
set -a
source backend/.env
set +a
psql "$SUPABASE_DB_URL" -v ON_ERROR_STOP=1 \
  -f supabase/seed.sql -f supabase/seed_primaria.sql
```

`db push` aplica el esquema; la semilla se ejecuta por separado con `psql` y solo en desarrollo, porque toma su configuración del entorno local. Genera 10 estudiantes por sección por defecto y una cuenta de apoderado por familia; las parejas de hermanos y el tamaño de cada sección son parámetros locales. También genera cuentas demo de docentes, directores y secretaría, con correos del dominio de prueba configurado y una contraseña que defines localmente. No se crean cuentas para estudiantes ni se guardan personas o contraseñas en Git. No cargues la semilla en producción. El backend se conecta a PostgreSQL con credenciales privadas; web y Android solo llaman a la API Spring.

### Variables de entorno del backend

| Variable | Tipo | Qué es | Valor |
|---|---|---|---|
| `DB_URL` | Default en repo | URL JDBC de PostgreSQL | Supabase dev; override en `backend/.env` para local |
| `DB_USER` | Default en repo | Usuario de la BD | `edufast`; override en `backend/.env` para local |
| `DB_PASSWORD` | Privado | Contraseña de la BD | canal privado (obligatoria) |
| `SUPABASE_DB_URL` | Privado | URI para ejecutar la semilla | Supabase → Connect |
| `JWT_SECRET` | Privado | Clave para firmar tokens | `openssl rand -hex 32` |
| `ALLINEDU_DEMO_EMAIL_DOMAIN` | Compartido | Dominio de correos demo | `example.test` |
| `ALLINEDU_DEMO_PASSWORD` | Compartido | Contraseña de las cuentas demo | `demo123` |
| `ALLINEDU_DEMO_STUDENTS_PER_SECTION` | Compartido | Alumnos por sección en la semilla | `10` |
| `ALLINEDU_DEMO_SIBLING_PAIRS` | Compartido | Pares de hermanos en la semilla | `10` |

Inicia el backend (Gradle carga `backend/.env` automáticamente):

```bash
cd backend && ./gradlew bootRun
```

`backend/.env` está excluido de Git. Los valores privados se comparten por un canal seguro; el `clone` del repositorio no concede acceso a Supabase. Mantén producción en un proyecto separado y no cargues allí la semilla demo.

### 1. Backend (Java + Spring Boot)
```bash
cd backend
./gradlew bootRun
```
- API: `http://localhost:8080`
- Swagger (docs interactivas): `http://localhost:8080/swagger-ui/index.html`
- Tests (incluyen las reglas de arquitectura): `./gradlew test`

### 2. Web (React + Vite)
```bash
cd web
npm install     # primera vez
npm run dev
```
- Web: `http://localhost:5173` (el proxy reenvía `/api` al backend)

### 3. Móvil (Kotlin + Android)
Requiere **Android Studio** y **JDK 21** para compilar. Abrir la carpeta `mobile/`, sincronizar Gradle y correr en un emulador.

- La URL de la API vive en `BuildConfig.API_BASE_URL` (no está en el código). En debug apunta a `http://10.0.2.2:8080`; se puede sobreescribir con la propiedad Gradle `allinedu.debugApiBaseUrl`. Para release, define `allinedu.apiBaseUrl`.
- El HTTP en claro al backend local solo se habilita en debug (`mobile/app/src/debug/AndroidManifest.xml`); release no lo incluye.

### Datos de prueba
```
Dominio:     example.test
Contraseña:  demo123

Personal:    personal.docente.regular@example.test
             personal.docente.auxiliar@example.test
             personal.director.primaria@example.test
             personal.director.general@example.test
             personal.secretaria@example.test
Apoderados:  apoderado.hermanos.0001@example.test (y los generados por familia)
```
El docente regular está asignado a **4.º de primaria - A**. Los datos sintéticos los genera `supabase/seed.sql` solo en desarrollo. Revisa `backend/.env.example` para los parámetros configurables.

---

## 🎓 Contexto académico

El proyecto se presenta en tres cursos de TECSUP (material en `CURSOS/`):

| Curso | Qué evalúa del proyecto |
|---|---|
| Investigación e Innovación Tecnológica | Formulación, evidencia, metodología y piloto |
| Java Web / Spring Boot | Backend y API REST |
| Kotlin / Android | App móvil |

> Nota para el curso de Kotlin: el temario enseña Retrofit, Room, Navigation Compose y MVVM.
> El móvil actual usa Ktor sin Room ni navigation-compose. Alinear el stack antes de agregar
> funcionalidades.
