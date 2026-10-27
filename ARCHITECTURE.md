# 🏛️ ARCHITECTURE.md — Arquitectura de AllinEdu (explicada desde cero)

> **Para quién es:** alguien que ya escribe código pero nunca estudió arquitectura formalmente.
> **Cómo leerlo:** de arriba a abajo. Cada término técnico se explica primero con una analogía y luego se muestra en el código real.

---

## 1. ¿Qué es una arquitectura de software?

### La analogía del restaurante 🍽️

Imagina un restaurante:

- El **mesero** recibe tu pedido y te trae el plato. **No cocina.**
- El **cocinero** prepara el plato. **No atiende mesas** ni sabe quién pidió.
- El **almacenero** guarda y busca ingredientes en la despensa. **No cocina ni atiende.**
- Las **recetas** definen qué es cada plato. No cambian si cambia el mesero o el proveedor.

Cada rol tiene **un solo trabajo** y conoce **solo lo que necesita**. Si cambias de mesero, la cocina sigue igual. Si cambias de proveedor de ingredientes, el cocinero ni se entera.

**Eso es una arquitectura en capas**: dividir el código en roles que solo hablan con su vecino inmediato.

### Código espagueti vs. código en capas

Sin estructura, todo queda mezclado (por eso se le dice "código espagueti": tiras de un fideo y viene todo pegado). En AllinEdu, la versión espagueti sería que el botón "Guardar asistencia" de React tuviera adentro el SQL, la validación de permisos y el envío de emails. Parece rápido hoy, pero:

- ¿Cambiar de base de datos? Tendrías que editar botones.
- ¿Probar la validación sin abrir el navegador? Imposible.
- ¿Dos personas trabajando a la vez? Conflictos eternos en el mismo archivo.

Con capas, cada pieza se puede **cambiar, probar y entender por separado**.

---

## 2. La arquitectura del prototipo de código, explicada desde cero

> **Contexto de producto:** AllinEdu reduce la carga operativa del docente en la preparación, aplicación y evaluación de sus clases. El alcance completo está en `ALCANCE.md` y los flujos en `USE_CASES.md`.
>
> Este documento usa la **asistencia** como ejemplo didáctico en todas sus secciones porque es el flujo que ya está implementado de punta a punta: web, móvil, API, servicio, dominio y base de datos. Sirve como plantilla para construir el resto de la cadena, empezando por el pipeline de preparación de clases, que es la prioridad 1.
>
> El proyecto se llamó EduFast en una iteración anterior. El código ya usa `AllinEdu`; solo el rol `edufast` de la base de datos conserva el nombre viejo (decisión D-2 de `ALCANCE.md`).

### Estado del esquema escolar

La API representa matrícula anual por grado y sección: el docente ve sus secciones asignadas, consulta la lista de estudiantes de cada sección y registra la asistencia diaria. El esquema físico está versionado en `supabase/migrations/` y las entidades JPA mapean esas tablas en español. Hibernate valida el esquema (`ddl-auto=validate`); las migraciones son la única fuente de verdad de la estructura.

Las migraciones SQL son infraestructura: describen la estructura física y se mantienen junto al ERD de `DATABASE.md`. El dominio continúa con modelos puros en inglés; las entidades JPA y mappers traducen entre el dominio y las tablas. Web y Android llaman únicamente a la API Spring, nunca a Supabase directamente.

### El patrón: Clean Architecture (Puertos y Adaptadores)

AllinEdu sigue **Clean Architecture**, también llamada "Hexagonal" o "Puertos y Adaptadores". Su regla de oro:

> **Las dependencias solo apuntan hacia adentro.** El centro (el negocio) no sabe nada del exterior (web, base de datos, frameworks).

```
┌──────────────────────────────────────────────────────┐
│  infrastructure/        (el mundo exterior)          │
│   ├── controller/    ← meseros (reciben HTTP)        │
│   ├── persistence/   ← el almacén (JPA/PostgreSQL)   │
│   ├── security/      ← el vigilante (JWT, BCrypt)    │
│   └── config/        ← puesta en marcha (seeder)     │
│  ┌────────────────────────────────────────────────┐  │
│  │  application/         (los cocineros)           │  │
│  │   ├── service/     ← la lógica de negocio       │  │
│  │   └── dto/         ← la carta del menú          │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │  domain/          (las recetas)           │  │  │
│  │  │   ├── model/     ← qué ES un curso        │  │  │
│  │  │   ├── port/      ← contratos              │  │  │
│  │  │   └── exception/ ← errores de negocio     │  │  │
│  │  │   (CERO imports de frameworks)            │  │  │
│  │  └──────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

**Traducción restaurante → código:**

| Restaurante | AllinEdu | Archivos de ejemplo |
|---|---|---|
| Recetas e ingredientes | `domain/model/` | `Section.java`, `Student.java`, `Attendance.java` |
| Contratos con proveedores | `domain/port/` | `AttendanceRepository.java`, `EmailSender` (futuro) |
| Errores de la casa | `domain/exception/` | `NotFoundException.java`, `ForbiddenException.java` |
| Cocineros | `application/service/` | `AttendanceServiceImpl.java` |
| Carta del menú | `application/dto/` | `AttendanceRequest.java`, `LoginResponse.java` |
| Meseros | `infrastructure/controller/` | `AttendanceController.java` |
| Almacén | `infrastructure/persistence/` | `entity/`, `mapper/`, `repository/`, `adapter/` |
| Migraciones del esquema objetivo | `supabase/migrations/` | SQL versionado aplicado con Supabase CLI |
| Vigilante de la puerta | `infrastructure/security/` | `JwtAuthenticationFilter.java` |

### Término clave: inyección de dependencias

**Analogía:** el cocinero no construye su propia cocina ni cría sus propias vacas. Todo le **llega listo**; él solo declara "necesito un almacén y una batidora".

**En el código:** `AttendanceServiceImpl` declara en su constructor lo que necesita:

```java
public AttendanceServiceImpl(SectionService sectionService,
                             AttendanceRepository attendanceRepository, ...) {
```

y **Spring** (el "gerente del restaurante") se lo entrega al arrancar. Por eso los servicios piden **interfaces** (puertos), no clases concretas: al cocinero le da igual *qué* almacén, mientras cumpla el contrato.

### El viaje de una petición real: "Guardar asistencia"

Sigamos el clic del profesor hasta PostgreSQL y de vuelta. Cada pieza se define la primera vez que aparece.

1. **El clic (web).** En `web/src/components/Attendance.tsx`, el profesor marca checkboxes y pulsa *Guardar asistencia*. `handleSave()` arma la lista y llama a `saveAttendance(section.id, date, attendance)`.

2. **La capa de servicios web.** `web/src/services/api.ts` es la **única** parte del frontend que habla con el backend. Envía `POST /api/v1/sections/5/attendance` con el JSON y el token en el header `Authorization: Bearer ...`.

3. **El vigilante (backend).** `JwtAuthenticationFilter.java` es un **filtro**: un guardia que revisa *toda* petición antes de dejarla pasar. Valida el **JWT** (una "pulsera de entrada" firmada que el backend te dio al hacer login) y carga tu usuario. Token falso o vencido → no pasas.

4. **El mesero.** `AttendanceController.java` es un **controller**: la clase que atiende una ruta HTTP. Su método `save(...)` recibe el JSON ya convertido en `AttendanceRequest` y al usuario identificado por el vigilante. El mesero **no aplica reglas de negocio**: solo entrega el pedido al cocinero.

5. **El cocinero.** `AttendanceServiceImpl.takeAttendance(...)` es un **service**: donde vive la lógica. En orden:
   - `sectionService.getAssignedSection(user, sectionId)` → la sección existe **y está asignada al docente** (si no: `NotFoundException`).
   - Elimina alumnos duplicados del pedido (gana el último).
   - El repositorio valida que cada alumno **está matriculado en esa sección** (`UbicacionMatricula` vigente).
   - Guarda la jornada y sus registros de una vez; la jornada queda confirmada.
   - Devuelve los registros guardados como `AttendanceResponse`.

6. **El contrato (puerto).** `AttendanceRepository` es una **interfaz** en `domain/port/`. El cocinero solo conoce ese contrato: no sabe si detrás hay PostgreSQL, un Excel o memoria.

7. **El almacenero (adaptador).** `AttendanceRepositoryAdapter.java` implementa el puerto: traduce modelos de dominio a `JornadaAsistenciaEntity` y `RegistroAsistenciaEntity` y llama a los repositorios Spring Data.

8. **La despensa.** PostgreSQL, tablas `jornadas_asistencia` y `registros_asistencia`. El esquema lo crean las migraciones de `supabase/migrations/`, no Hibernate.

9. **El camino de vuelta.** El cocinero devuelve `List<AttendanceResponse>` (un **DTO** = Data Transfer Object, "el plato servido": solo los datos que el cliente necesita, nunca la entidad cruda). El mesero lo convierte a JSON, viaja por HTTP, y React muestra *"Asistencia del 2026-09-14 guardada (18 presentes)"*.

### ¿Y el móvil?

Misma idea en Kotlin: `ApiAllinEduRepository.kt` equivale a `api.ts` (el único que habla HTTP) y las pantallas Compose (`AttendanceScreen.kt`) equivalen a los componentes React. El backend no sabe si le habla la web o el móvil: para él todo son peticiones HTTP con JSON.

---

## 3. Evaluación honesta de calidad

### Lo que está bien (y por qué)

- **Dominio 100% puro:** `domain/` no importa nada de frameworks. Verificado automáticamente (sección 5).
- **Puertos y adaptadores reales:** cambiar de PostgreSQL a otra base de datos = reescribir solo `infrastructure/persistence/`, sin tocar ni una línea de negocio.
- **Autorización por asignación:** `getAssignedSection()` impide que un docente vea o registre asistencia en secciones que no tiene asignadas.
- **Validación de matrícula:** no se puede guardar asistencia de alumnos que no pertenecen al curso.
- **Errores consistentes:** las excepciones de negocio se traducen a HTTP en un solo lugar (`GlobalExceptionHandler`).
- **Tests:** servicios con mocks + reglas de arquitectura automáticas.

### Los smells que TENÍA y cómo se arreglaron

> Un **code smell** ("olor de código") es algo que funciona hoy pero te traerá dolor mañana. Aprende a detectarlos:

1. **La cocina conocía al proveedor.** `application/` importaba una excepción de `infrastructure/`.
   *Dolor:* cambiar de framework web obligaría a tocar la lógica de negocio.
   *Fix:* excepciones puras en `domain/exception/`; la traducción a HTTP vive en `GlobalExceptionHandler`.

2. **Depender de la clase concreta.** `AttendanceServiceImpl` usaba `SectionServiceImpl` en vez de la interfaz `SectionService`.
   *Dolor:* no puedes cambiar ni probar un servicio sin arrastrar al otro.
   *Fix:* depender siempre de la interfaz.

3. **Recetas con etiquetas de almacén.** Los modelos de dominio eran entidades `@Entity` de JPA.
   *Dolor:* el negocio queda atado a Hibernate y las reglas de capas se difuminan.
   *Fix:* separación total — modelos puros + `entity/` + `mapper/` + `adapter/`.

4. **El N+1 oculto.** Ver la asistencia de 40 alumnos hacía 41 consultas a la base de datos, disimulado por `open-in-view`.
   *Dolor:* con reportes grandes, la base de datos se ahoga.
   *Fix:* `@EntityGraph` (todo en una consulta) + `open-in-view=false` para que nunca se vuelva a ocultar.

5. **Asistencia de alumnos ajenos.** Solo se verificaba que el alumno existiera, no que fuera del curso.
   *Dolor:* datos corruptos y un hueco de seguridad.
   *Fix:* validación de matrícula en `takeAttendance`.

6. **Secretos en el código.** La clave JWT y la contraseña de la BD estaban escritas en `application.properties`.
   *Dolor:* si el repo se hace público, cualquiera puede firmar tokens.
   *Fix:* variables de entorno (`JWT_SECRET`, `DB_PASSWORD`) con valores por defecto solo para desarrollo.

7. **Credenciales demo precargadas** en el formulario de login web.
   *Dolor:* si se olvidan ahí, cualquiera entra.
   *Fix:* campos vacíos; las credenciales demo solo se mencionan en el README.

8. **Race condition en la web.** Dos `useEffect` escribían el mismo estado; un clic rápido se perdía.
   *Dolor:* asistencias guardadas con valores mezclados.
   *Fix:* un solo flujo de carga con flag `cancelled` y estado `loading`.

---

## 4. Tutorial: agregar una feature nueva ("recordatorios por email")

**Escenario:** cuando un alumno falta, enviar un email al padre. Sigue los pasos **en orden**.

### Paso 1 — El contrato primero → `domain/port/EmailSender.java`

¿Por qué primero el contrato? Porque al negocio le da igual *qué* empresa envía el email (Gmail, SendGrid...). Solo necesita que **alguien** lo envíe.

```java
package com.allinedu.domain.port;

public interface EmailSender {
    void send(String to, String subject, String body);
}
```

### Paso 2 — La lógica → `application/service/ReminderServiceImpl.java`

Usa `AttendanceServiceImpl` como plantilla: interfaz + clase `...Impl` que recibe puertos por el constructor.

```java
@Service
public class ReminderServiceImpl implements ReminderService {

    private final EmailSender emailSender; // el PUERTO, no una clase concreta

    public ReminderServiceImpl(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void notifyAbsence(String parentEmail, String studentName, LocalDate date) {
        emailSender.send(parentEmail, "Inasistencia",
                studentName + " faltó el " + date);
    }
}
```

¿Por qué en `application/`? Porque "avisar al padre cuando falta" es una **regla de negocio**, no un detalle técnico.

### Paso 3 — El adaptador → `infrastructure/email/SmtpEmailSender.java`

```java
@Component
public class SmtpEmailSender implements EmailSender {
    @Override
    public void send(String to, String subject, String body) {
        // aquí va la librería real (JavaMailSender de Spring, etc.)
    }
}
```

¿Por qué en `infrastructure/`? Porque SMTP es un detalle del mundo exterior. Si mañana cambias a SendGrid, creas `SendGridEmailSender` y **no tocas el servicio**.

### Paso 4 — Conectar con la asistencia

En `AttendanceServiceImpl.takeAttendance`, después de guardar: por cada alumno ausente, llamar a `reminderService.notifyAbsence(...)`. Inyectas `ReminderService` (la interfaz) por el constructor, igual que los demás puertos.

### Paso 5 — Tests

Copia `AttendanceServiceImplTest` como plantilla: mock de `EmailSender` con Mockito y verifica con `verify(emailSender).send(...)`.

### Paso 6 — Verificar

`./gradlew test` → si el guardián (ArchUnit) pasa, respetaste las capas.

### La lección del tutorial

Fíjate en el orden: **contrato → lógica → adaptador → conexión → test**. Nunca empezamos preguntando "¿qué librería de email uso?", sino "¿qué necesita el negocio?". Eso es pensar en capas.

---

## 5. El guardián automático: ArchUnit 🤖

`backend/src/test/java/com/allinedu/ArchitectureTest.java` tiene las reglas de la arquitectura escritas **como tests**:

- `domain` no puede importar `jakarta.*`, `org.springframework.*` ni `infrastructure`
- `application` no puede importar `infrastructure`
- los controllers no pueden tocar `persistence`

Si tú (o un agente de IA) rompes una regla, `./gradlew test` **falla con un mensaje claro**. La arquitectura no depende de la memoria ni de la buena voluntad: está protegida por código.

---

## 6. Glosario del proyecto

| Término | Definición en una línea | Ejemplo en el código |
|---|---|---|
| Capa | Grupo de archivos con un solo rol | `domain/`, `application/`, `infrastructure/` |
| Dominio | Las reglas y conceptos del negocio, puros | `domain/model/Section.java` |
| Modelo | Clase que representa un concepto (curso, alumno) | `domain/model/Student.java` |
| Puerto | Interfaz: el contrato que el negocio necesita | `domain/port/AttendanceRepository.java` |
| Adaptador | Clase que cumple un puerto con tecnología real | `persistence/adapter/AttendanceRepositoryAdapter.java` |
| Servicio | Clase con la lógica de negocio (el cocinero) | `application/service/AttendanceServiceImpl.java` |
| Controller | Clase que recibe peticiones HTTP (el mesero) | `infrastructure/controller/AttendanceController.java` |
| DTO | Objeto solo para transportar datos (la carta) | `application/dto/AttendanceRequest.java` |
| Entidad JPA | Clase que describe una tabla de la base de datos | `persistence/entity/AttendanceEntity.java` |
| Mapper | Traductor entre modelo de dominio y entidad JPA | `persistence/mapper/AttendanceMapper.java` |
| Repositorio Spring Data | Interfaz que genera el SQL automáticamente | `persistence/repository/SpringDataAttendanceRepository.java` |
| Inyección de dependencias | Recibir lo que necesitas por el constructor | Constructor de `AttendanceServiceImpl` |
| Filtro | Guardia que revisa cada petición antes de que llegue | `security/JwtAuthenticationFilter.java` |
| JWT | Token firmado que prueba quién eres sin sesión en el servidor | `security/JwtService.java` |
| BCrypt | Algoritmo para guardar contraseñas de forma irreversible | `security/BCryptPasswordHasher.java` |
| Transacción | Operaciones de BD que se confirman o se cancelan juntas | `@Transactional` en `takeAttendance` |
| N+1 | 1 consulta para la lista + 1 por cada elemento (malo) | Evitado con `@EntityGraph` |
| Code smell | Código que funciona pero causará dolor futuro | Ver sección 3 |
| CORS | Reglas de qué páginas web pueden llamar a tu API | `SecurityConfig.corsConfigurationSource` |
| Seeder | Código que carga datos de ejemplo al arrancar | `infrastructure/config/DataSeeder.java` |
| Endpoint | Una URL + método HTTP que expone el backend | `POST /api/v1/sections/{id}/attendance` |
| ArchUnit | Librería que testea reglas de arquitectura | `src/test/java/com/allinedu/ArchitectureTest.java` |
| Proxy (Vite) | Redirección de `/api` del frontend al backend en desarrollo | `web/vite.config.ts` |
