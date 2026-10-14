# AGENTS.md — Reglas OBLIGATORIAS para agentes de IA y colaboradores

> Si eres un agente de IA trabajando en este repo: **lee esto completo antes de tocar cualquier archivo.**
> Estas reglas no son sugerencias. Si las rompes, `./gradlew test` fallará (el guardián ArchUnit te atrapará).

---

## 1. Qué es este proyecto

**AllinEdu**: plataforma web y móvil que reduce la carga operativa del docente en la preparación, aplicación y evaluación de sus clases, y convierte la evidencia de ese ciclo en apoyo para reforzar aprendizajes. ODS 4 (educación de calidad) y ODS 8 (trabajo decente).

El producto es **una cadena**, no módulos sueltos:

```
planificar (sesión → ficha diferenciada → instrumento)
   → aplicar en clase (asistencia + evidencia por criterio)
      → evaluar por competencia
         → refuerzo y repaso espaciado · conclusiones descriptivas
```

**Prioridad 1 es el pipeline de preparación de clases** (2.5–4 h/día de trabajo no remunerado). La asistencia ya está construida y es el eslabón "aplicar en clase".

Antes de proponer o construir una feature, lee:

| Documento | Qué te dice |
|---|---|
| `ALCANCE.md` | Qué entra, qué **no** y por qué. Decisiones pendientes. Historial de cambios de alcance |
| `EVIDENCIA.md` | La línea base y los insights que justifican cada decisión |
| `USE_CASES.md` | Los flujos y qué está marcado `[EXISTE]` |

Monorepo con 3 piezas que se comunican por HTTP + JSON:

| Carpeta | Tecnología | Rol |
|---|---|---|
| `backend/` | Java 25 + Spring Boot + PostgreSQL | API REST. ÚNICA pieza que toca la base de datos |
| `web/` | React 19 + Vite + TypeScript | Frontend web. NUNCA habla con la BD, solo con el backend |
| `mobile/` | Kotlin + Jetpack Compose | App Android (esqueleto). Igual: solo habla con el backend |
| `supabase/` | SQL + Supabase CLI | Migraciones versionadas y semilla sintética del esquema escolar |

---

## 2. Comandos esenciales

```bash
# Backend: correr tests (OBLIGATORIO antes de dar trabajo por terminado)
# Gradle carga backend/.env solo; no hace falta exportarlo a mano.
cd backend && ./gradlew test

# Backend: levantar la API
cd backend && ./gradlew bootRun

# Web: lint + build (OBLIGATORIO si tocaste web/)
cd web && npm run lint && npm run build

# Web: desarrollo
cd web && npm run dev
```

Por defecto el backend apunta al **Supabase de desarrollo compartido** (host y usuario no son secretos y ya son el default en `application.properties`). Para conectar solo falta `DB_PASSWORD`, que se pide al equipo por un canal seguro: clonar el repo no da acceso a la base.

```bash
cp backend/.env.example backend/.env   # completa DB_PASSWORD (y JWT_SECRET)
```

Gradle carga `backend/.env` automáticamente para `./gradlew test` y `./gradlew bootRun`. Sin `DB_PASSWORD`, los tests de arquitectura y de servicio pasan, pero `contextLoads` falla al no conectar.

Para desarrollar contra un **Postgres local** en vez de Supabase, define `DB_URL`, `DB_USER` y `DB_PASSWORD` en `backend/.env` (esos valores ganan sobre el default). En ese caso el rol se crea una sola vez:
```sql
CREATE ROLE edufast WITH LOGIN PASSWORD 'edufast123' BYPASSRLS;
CREATE DATABASE edufast OWNER edufast;
-- Si el rol ya existe con otra contraseña, alinéalo:
-- ALTER ROLE edufast WITH LOGIN PASSWORD 'edufast123' BYPASSRLS;
```

Las tablas las crean las migraciones de `supabase/migrations/`; no uses `ddl-auto=update`.

---

## 3. Las 10 reglas de oro (NUNCA las rompas)

Las dos primeras son de producto; las ocho siguientes, de arquitectura.

### Regla 0a — La IA propone borradores, el docente aprueba
Nada generado automáticamente llega al estudiante, se imprime o entra a un registro oficial sin aprobación explícita del docente. Todo contenido generado nace en estado **no aprobado** y el sistema conserva qué fue generado y qué fue editado por una persona.

### Regla 0b — El sistema entrega evidencia; el docente decide
Prohibido implementar calificación automática, decisiones pedagógicas automáticas o cualquier función de vigilancia de estudiantes. Las prácticas y simulacros son formativos y de bajo riesgo.

Si una tarea te pide romper 0a o 0b, **detente y pregunta**. Están en el alcance por razones éticas y de perfil de proyecto, no por conveniencia técnica.

---

### Regla 1 — Las dependencias solo apuntan hacia adentro
```
infrastructure ──► application ──► domain
```
`domain/` NO importa nada de `application/` ni de `infrastructure/`. `application/` NO importa nada de `infrastructure/`.

### Regla 2 — El dominio es 100% puro
`domain/` no puede importar **nada** de frameworks: ni `jakarta.*`, ni `org.springframework.*`.

```
❌ MAL  (en domain/model/Section.java):
   import jakarta.persistence.Entity;

✅ BIEN: las anotaciones JPA van en
   infrastructure/persistence/entity/SeccionEntity.java
```

### Regla 3 — Los servicios dependen de INTERFACES, nunca de clases concretas
```
❌ MAL:  private final SectionServiceImpl sectionService;
✅ BIEN: private final SectionService sectionService;
```

### Regla 4 — Los controllers NO tienen lógica de negocio ni tocan la base de datos
Un controller solo: recibe la petición → llama a UN servicio → devuelve el DTO.
```
❌ MAL:  un controller que inyecta AttendanceRepository o AttendanceEntity
✅ BIEN: un controller que inyecta AttendanceService y devuelve AttendanceResponse
```

### Regla 5 — La API NUNCA expone entidades ni modelos de dominio
Solo DTOs (records en `application/dto/`). Nunca devuelvas `AttendanceEntity` ni `Attendance` en un controller.

### Regla 6 — Los errores de negocio son excepciones de dominio
```
❌ MAL:  throw new ResponseStatusException(HttpStatus.NOT_FOUND, ...)  // HTTP en la lógica
✅ BIEN: throw new NotFoundException("Sección no encontrada")            // domain/exception
```
`GlobalExceptionHandler` las traduce a HTTP. Disponibles: `NotFoundException` (404), `ForbiddenException` (403), `UnauthorizedException` (401). Si necesitas otra, créala en `domain/exception/` extendiendo `DomainException` y agrégale su handler.

### Regla 7 — Los secretos NUNCA se escriben en el código
Usa variables de entorno con default de desarrollo: `${JWT_SECRET:valor-dev}`. Ver `application.properties`.

### Regla 8 — Si `./gradlew test` falla, NO has terminado
Incluye `ArchitectureTest`, que verifica las reglas 1, 2 y 4 automáticamente. Un test rojo = arquitectura rota.

---

## 4. ¿Dónde va cada archivo? (backend)

| Necesitas crear... | Carpeta EXACTA | Nombre | Plantilla a copiar |
|---|---|---|---|
| Un concepto de negocio | `domain/model/` | `Grade.java` | `domain/model/Section.java` |
| Un contrato de persistencia | `domain/port/` | `GradeRepository.java` | `domain/port/SectionRepository.java` |
| Un contrato de servicio externo | `domain/port/` | `EmailSender.java` | `domain/port/TokenProvider.java` |
| Un error de negocio | `domain/exception/` | `ConflictException.java` | `domain/exception/NotFoundException.java` |
| Un formato de entrada/salida | `application/dto/` | `GradeRequest.java` (record) | `application/dto/SectionResponse.java` |
| Lógica de negocio | `application/service/` | `GradeService.java` + `GradeServiceImpl.java` | `application/service/SectionService*.java` |
| Una tabla de BD | `infrastructure/persistence/entity/` | `GradeEntity.java` | `persistence/entity/SeccionEntity.java` |
| Traductor dominio↔entidad | `infrastructure/persistence/mapper/` | `GradeMapper.java` | `persistence/mapper/SectionMapper.java` |
| Consultas SQL automáticas | `infrastructure/persistence/repository/` | `SpringDataGradeRepository.java` | `persistence/repository/SpringDataAsignacionDocenteRepository.java` |
| Implementación del puerto | `infrastructure/persistence/adapter/` | `GradeRepositoryAdapter.java` | `persistence/adapter/SectionRepositoryAdapter.java` |
| Un endpoint HTTP | `infrastructure/controller/` | `GradeController.java` | `infrastructure/controller/SectionController.java` |
| Un test de servicio | `src/test/.../application/service/` | `GradeServiceImplTest.java` | `AttendanceServiceImplTest.java` |

### Base de datos y documentación

| Necesitas crear... | Carpeta exacta | Convención |
|---|---|---|
| Migración PostgreSQL/Supabase | `supabase/migrations/` | SQL versionado; tablas/columnas en español y `snake_case` |
| Datos de desarrollo | `supabase/seed.sql` | Solo datos sintéticos; nunca menores reales ni secretos |
| Casos de uso | raíz, `USE_CASES.md` | Actores, flujos y excepciones; marca `[EXISTE]` lo implementado |
| ERD y diccionario | raíz, `DATABASE.md` | Debe coincidir con las migraciones |
| Decisión de alcance | raíz, `ALCANCE.md` | Catálogo, exclusiones con razón e historial de cambios |
| Colores y piezas de marca | raíz, `IDENTIDAD.md` | Tokens compartidos web/Android; sin hex sueltos en componentes |
| Hallazgo de investigación | raíz, `EVIDENCIA.md` | Con fuente, `n` y cita anonimizada |

No uses el dashboard remoto como fuente única para cambios de esquema: todas las modificaciones deben quedar en migraciones versionadas. La web y Android nunca reciben credenciales de PostgreSQL, claves `service_role` ni acceso directo a las tablas.

---

## 5. Checklist para agregar una feature (en este orden)

1. ☐ **Modelo** en `domain/model/` — POJO puro, sin anotaciones
2. ☐ **Puerto** en `domain/port/` — solo los métodos que el negocio necesita
3. ☐ **Entidad JPA** en `infrastructure/persistence/entity/` — con constraints (`nullable=false`, únicos)
4. ☐ **Mapper** en `infrastructure/persistence/mapper/` — métodos estáticos `toDomain`/`toEntity`
5. ☐ **SpringData repo + Adapter** en `persistence/repository/` y `persistence/adapter/` — usa `@EntityGraph` si la entidad tiene relaciones que siempre se leen (evita N+1)
6. ☐ **DTO + Service** en `application/` — el servicio valida reglas de negocio y lanza excepciones de dominio
7. ☐ **Controller + tests** — controller delgado; tests de servicio con Mockito
8. ☐ **Verificar**: `cd backend && ./gradlew test` en verde. Si tocaste `web/`: `npm run lint && npm run build`

---

## 6. Errores ya cometidos en este repo (NO los repitas)

| Error | Por qué dolió | Dónde se arregló |
|---|---|---|
| `application` importando excepción de `infrastructure` | Dependencia al revés | Excepciones movidas a `domain/exception/` |
| Servicio dependiendo de clase concreta `SectionServiceImpl` | Imposible testear/cambiar por separado | Ahora depende de la interfaz `SectionService` |
| Modelos de dominio como `@Entity` | Negocio atado a JPA | Separación total: `entity/` + `mapper/` + `adapter/` |
| N+1 oculto por `open-in-view` | 41 consultas para 40 alumnos | `@EntityGraph` + `open-in-view=false` |
| Guardar asistencia sin validar matrícula | Hueco de seguridad | Validación de ubicación de matrícula por sección en `takeAttendance` |
| Guardar registros uno por uno en un loop | N idas a la BD | `saveAll` en lote |
| JWT secret y password de BD en el código | Riesgo si el repo es público | Variables de entorno con default dev |
| Credenciales demo precargadas en el login web | Cualquiera entra | Campos vacíos en `Login.tsx` |

---

## 7. Convenciones

- **Idioma del código:** inglés para clases/métodos; español para mensajes al usuario y comentarios.
- **DTOs:** siempre `record` de Java, con validación `jakarta.validation` (`@NotNull`, `@NotBlank`...).
- **Transacciones:** `@Transactional` en métodos de servicio que escriben; `@Transactional(readOnly = true)` en los que solo leen.
- **Endpoints:** prefijo `/api/v1/`, recursos en plural.
- **Commits:** un commit por cambio coherente, mensaje en español que diga QUÉ y POR QUÉ.

---

## 8. Referencias

- `EVIDENCIA.md` — línea base, insights y límites metodológicos. **Toda cifra del proyecto se rastrea aquí.** Si vas a afirmar algo sobre el problema docente, cítalo de este documento o márcalo como hipótesis.
- `ALCANCE.md` — qué entra, qué no y por qué; decisiones pendientes; historial de cambios de alcance.
- `USE_CASES.md` — actores, flujos y qué está marcado `[EXISTE]`.
- `DATABASE.md` — modelo de datos y qué tablas sirven a cada parte de la cadena.
- `ARCHITECTURE.md` — explicación didáctica completa de la arquitectura (léela si no entiendes POR QUÉ una regla existe).
- `README.md` — contexto del producto y cómo correr el proyecto.

### Al terminar un cambio de producto

Si tu cambio altera el alcance, actualiza `ALCANCE.md` (catálogo, exclusiones o historial de cambios) en el mismo commit. Un alcance que no coincide con el código es la causa raíz del desorden que este repositorio ya tuvo una vez.
