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
| Pipeline de preparación de clases | Por construir — **prioridad 1** |
| Captura de evidencia por criterio | Por construir |
| Banco de ítems, diagnóstico y repaso | Por construir |
| Canal con familias | Por construir |

El código usa el identificador técnico `EduFast` de una iteración anterior (paquetes, nombre de
base de datos, cuentas de prueba). Renombrarlo es la decisión pendiente D-2 de
[`ALCANCE.md`](ALCANCE.md).

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

### Base de datos local (desarrollo)
```sql
CREATE ROLE edufast WITH LOGIN PASSWORD 'edufast123' BYPASSRLS;
CREATE DATABASE edufast OWNER edufast;
```
Aplica migraciones y semilla sobre esa base:
```bash
psql -d edufast -f supabase/migrations/20260928000100_base_escolar.sql
psql -d edufast -f supabase/migrations/20260928000200_curriculo_planificacion.sql
psql -d edufast -f supabase/migrations/20260928000300_actividades_notificaciones.sql
psql -d edufast -f supabase/seed.sql
```
El rol `edufast` usa `BYPASSRLS` para comportarse como el rol de servicio del backend.
Hibernate valida el esquema, no lo modifica.

### Migraciones Supabase del esquema

Con Supabase CLI instalado y Docker activo, ejecuta localmente:

```bash
supabase start
supabase db reset
```

Para aplicar migraciones a un proyecto remoto, autentícate y enlaza el proyecto:

```bash
supabase login
supabase link --project-ref <PROJECT_REF>
supabase db push
```

El archivo `supabase/seed.sql` crea datos sintéticos de prueba: 22 secciones, 396 estudiantes y
cuentas demo. No guardes contraseñas ni claves en Git. El backend se conecta con un rol de
servicio que ignora RLS; las políticas RLS están pensadas para bloquear el acceso directo de
clientes (`anon`/`authenticated`), no del servidor.

### Variables de entorno del backend
En producción, define:

| Variable | Qué es | Default (solo dev) |
|---|---|---|
| `DB_URL` | URL de PostgreSQL | `jdbc:postgresql://localhost:5432/edufast` |
| `DB_USER` | Usuario de la BD | `edufast` |
| `DB_PASSWORD` | Contraseña de la BD | `edufast123` |
| `JWT_SECRET` | Clave para firmar tokens | valor de desarrollo |

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
Requiere **Android Studio**. Abrir la carpeta `mobile/`, sincronizar Gradle y correr en un
emulador. La app se conecta a `http://10.0.2.2:8080`.

### Datos de prueba
```
Docente:   profesor@edufast.com / 123456   (asignado a 4.º de primaria, sección A)
Apoderado: apoderado@edufast.com / 123456  (vinculado a un estudiante de esa sección)
```
Los datos los crea `supabase/seed.sql` solo en desarrollo.

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
