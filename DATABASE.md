# Modelo de datos — AllinEdu

> **Fuente de verdad del esquema:** migraciones SQL de `supabase/migrations/`. Los nombres físicos de tablas y columnas están en español, `snake_case` y sin tildes. El código Java conserva nombres en inglés y mapea las entidades a esos nombres.

## Qué tabla sirve a qué parte de la cadena

El producto es la cadena descrita en [`ALCANCE.md`](ALCANCE.md). El esquema ya cubre la mayor parte:

| Eslabón de la cadena | Tablas disponibles | Estado |
|---|---|---|
| **Planificar** | `planificaciones_anuales`, `experiencias_aprendizaje`, `sesiones_planificadas`, `actividades_sesion`, `instrumentos_evaluacion`, `criterios_instrumento`, `niveles_instrumento`, `evidencias_previstas` | Esquema listo, **sin usar por el código** |
| **Aplicar en clase** | `jornadas_asistencia`, `registros_asistencia`, `ejecuciones_sesion`, `evidencias_aprendizaje` | Asistencia **implementada**; evidencia sin usar |
| **Evaluar** | `criterios_evaluacion`, `valoraciones_evidencia`, `retroalimentaciones`, `progreso_competencias` | Esquema listo, sin usar |
| **Refuerzo y repaso** | `actividades_aprendizaje`, `asignaciones_actividad`, `entregas_actividad`, `revisiones_actividad` | Parcial — **falta modelo de ítems**, ver brecha abajo |
| **Comunicación con familias** | `vinculos_apoderado_estudiante`, `eventos_notificacion`, `notificaciones`, `entregas_notificacion`, `intentos_notificacion`, `preferencias_notificacion`, `dispositivos_notificacion` | Esquema listo, sin usar |
| Base común | `instituciones_educativas`, `anios_escolares`, `grados_educativos`, `secciones`, `personas`, `cuentas_usuario`, `roles`, `estudiantes`, `matriculas`, `ubicaciones_matricula`, `asignaciones_docentes` | **Implementado** |

### Brecha pendiente: banco de ítems y práctica

Falta modelar el sustrato del refuerzo. Requiere una migración nueva y aprobada:

- **Ítems** por competencia, capacidad y nivel de dificultad, con su origen (generado por IA o escrito por el docente) y su estado de aprobación.
- **Intentos y respuestas** de práctica, con marca de tiempo, para poder comparar diagnóstico, práctica posterior y prueba diferida.
- **Programación de repaso espaciado**: qué ítem reaparece, cuándo y cuántas veces se practicó.

`actividades_aprendizaje` y `asignaciones_actividad` sirven para *asignar* una práctica, pero no guardan ítems ni respuestas. No forzarlas a cumplir ese rol.

### Lo que el esquema ya resuelve y conviene no duplicar

- La **generación con IA** no necesita tablas nuevas de contenido: las sesiones, actividades e instrumentos se guardan donde ya corresponde. El estado de aprobación y la distinción entre contenido generado y editado por una persona (Regla 0a de `AGENTS.md`) viven en `sesiones_planificadas` (`origen`, `editada_por/_at`, `aprobada_por/_at`) y `actividades_sesion` (`origen`, `editada_at`), desde `20261020000100_autoria_sesiones.sql`. Una actividad generada que el docente cambia pasa a `origen = 'DOCENTE'`.
- La **justificación de ausencias** se apoya en `registros_asistencia` más `archivos`; no crea un flujo paralelo.

La app/web nunca accede directamente a la base: ambos clientes usan la API Spring. Las claves privadas de PostgreSQL y de proveedores push no se distribuyen a los clientes.

## Relaciones principales

```mermaid
erDiagram
    INSTITUCIONES_EDUCATIVAS ||--o{ ANIOS_ESCOLARES : organiza
    ANIOS_ESCOLARES ||--o{ SECCIONES : contiene
    GRADOS_EDUCATIVOS ||--o{ SECCIONES : clasifica
    ESTUDIANTES ||--o{ MATRICULAS : tiene
    ANIOS_ESCOLARES ||--o{ MATRICULAS : recibe
    MATRICULAS ||--o{ UBICACIONES_MATRICULA : conserva
    SECCIONES ||--o{ UBICACIONES_MATRICULA : recibe
    PERSONAS ||--o| ESTUDIANTES : perfil
    PERSONAS ||--o| APODERADOS : perfil
    APODERADOS ||--o{ VINCULOS_APODERADO_ESTUDIANTE : autoriza
    ESTUDIANTES ||--o{ VINCULOS_APODERADO_ESTUDIANTE : vincula
    SECCIONES ||--o{ JORNADAS_ASISTENCIA : registra
    JORNADAS_ASISTENCIA ||--o{ REGISTROS_ASISTENCIA : incluye
    UBICACIONES_MATRICULA ||--o{ REGISTROS_ASISTENCIA : identifica
    PLANIFICACIONES_ANUALES ||--o{ EXPERIENCIAS_APRENDIZAJE : organiza
    EXPERIENCIAS_APRENDIZAJE ||--o{ SESIONES_PLANIFICADAS : secuencia
    SESIONES_PLANIFICADAS ||--o{ EJECUCIONES_SESION : realiza
    EXPERIENCIAS_APRENDIZAJE ||--o{ ACTIVIDADES_APRENDIZAJE : puede_generar
    ACTIVIDADES_APRENDIZAJE ||--o{ ASIGNACIONES_ACTIVIDAD : asigna
    MATRICULAS ||--o{ ASIGNACIONES_ACTIVIDAD : recibe
    ASIGNACIONES_ACTIVIDAD ||--o{ ENTREGAS_ACTIVIDAD : registra
    VINCULOS_APODERADO_ESTUDIANTE ||--o{ NOTIFICACIONES : destinatario_autorizado
    EVENTOS_NOTIFICACION ||--o{ NOTIFICACIONES : produce
    NOTIFICACIONES ||--o{ ENTREGAS_NOTIFICACION : intenta
```

## Catálogo de tablas por migración

### `20260928000100_base_escolar.sql`

Institución y trayectoria:

- `instituciones_educativas`, `sedes_educativas`, `anios_escolares`
- `niveles_educativos`, `ciclos_educativos`, `grados_educativos`, `turnos`
- `secciones`, `aulas`
- `personas`, `cuentas_usuario`, `roles`, `asignaciones_roles`
- `estudiantes`, `apoderados`, `contactos_persona`, `vinculos_apoderado_estudiante`
- `matriculas`, `ubicaciones_matricula`, `movimientos_matricula`
- `asignaciones_docentes`
- `jornadas_asistencia`, `registros_asistencia`

Reglas relevantes:

- Una matrícula corresponde a un estudiante, institución y año escolar.
- `ubicaciones_matricula` conserva el historial de grado/sección; un índice único parcial permite una sola ubicación vigente por matrícula.
- La matrícula, la sección y la ubicación usan claves foráneas simples; la coherencia de año escolar se valida en la aplicación.
- La capacidad de una sección no fija su cantidad de matriculados. La semilla usa 18 por sección, pero el esquema no impone ese número.
- Una pasada de lista es única por sección/fecha; cada detalle corresponde a una ubicación de matrícula.
- Un cambio de sección no elimina la asistencia histórica.

### `20260928000200_curriculo_planificacion.sql`

Currículo y planificación pedagógica:

- `versiones_curriculares`, `areas_curriculares`, `competencias`, `areas_competencias`
- `capacidades`, `estandares_aprendizaje`, `desempenos_grado`, `planes_estudio`, `planes_estudio_areas`
- `ofertas_curriculares`
- `periodos_evaluacion`, `eventos_calendario`
- `planificaciones_anuales`, `destinos_planificacion`
- `experiencias_aprendizaje`, `experiencias_competencias`, `experiencias_periodos`, `criterios_evaluacion`
- `instrumentos_evaluacion`, `criterios_instrumento`, `niveles_instrumento`
- `sesiones_planificadas`, `sesiones_competencias`, `actividades_sesion`, `evidencias_previstas`
- `ejecuciones_sesion`, `evidencias_aprendizaje`, `valoraciones_evidencia`, `retroalimentaciones`
- `progreso_competencias`
- `horarios_seccion`

Una experiencia puede ser unidad, proyecto, experiencia o módulo. Puede integrar varias áreas/competencias y dirigirse a varias secciones. Una experiencia puede abarcar uno o varios períodos de evaluación y viceversa (`experiencias_periodos`). Las sesiones tienen orden pedagógico separado de la fecha de ejecución. La relación unidad-sesión puede ser nula para una actividad independiente, por ejemplo, una evaluación diagnóstica o un refuerzo. El horario semanal previsto de cada sección vive en `horarios_seccion`; la ejecución real de una sesión se registra por separado en `ejecuciones_sesion`.

### `20260928000300_actividades_notificaciones.sql`

Actividades y comunicación:

- `actividades_aprendizaje`, `destinos_actividad`, `asignaciones_actividad`
- `entregas_actividad`, `archivos`, `revisiones_actividad`
- `tipos_notificacion`, `preferencias_notificacion`, `dispositivos_notificacion`
- `eventos_notificacion`, `notificaciones`, `entregas_notificacion`, `intentos_notificacion`
- `eventos_auditoria`

El registro de “no entregada” solo se crea al ser confirmado por un actor autorizado; no se infiere por ausencia de archivo. El evento de notificación es idempotente y puede producir distintas entregas por destinatario/dispositivo.

### `20260928000400_roles_y_alcances_personal.sql`

- Agrega el rol de acceso `SECRETARIA`.
- `asignaciones_roles.nivel_educativo_id` delimita asignaciones de dirección por nivel; `NULL` representa alcance institucional.
- `asignaciones_roles.reporta_a_cuenta_usuario_id` registra la relación de supervisión de una asignación, por ejemplo, una secretaria que reporta a la dirección general.
- `asignaciones_docentes.funcion` distingue a un docente regular de un auxiliar. Ambos usan el rol de acceso `DOCENTE`.
- Habilita `pgcrypto` para que el generador de datos de desarrollo convierta la contraseña configurada en un hash BCrypt sin guardarla en los archivos del repositorio.

### `20261020000100_autoria_sesiones.sql`

- `sesiones_planificadas`: `origen` (`DOCENTE` o `GENERADA`), `editada_por`, `editada_at`, `aprobada_por`, `aprobada_at`.
- `actividades_sesion`: `origen` y `editada_at`. Lo existente queda como `DOCENTE`.

## Restricciones críticas

- `UNIQUE(institucion_id, anio_escolar_id, grado_id, nombre)` en secciones.
- `UNIQUE(estudiante_id, anio_escolar_id)` en matrículas de la IE.
- Una única ubicación vigente por matrícula.
- `UNIQUE(seccion_id, fecha)` en jornadas de asistencia.
- `UNIQUE(jornada_id, ubicacion_matricula_id)` en detalle de asistencia.
- `UNIQUE(experiencia_id, numero_orden)` en sesiones planificadas.
- `UNIQUE(seccion_id, dia_semana, hora_inicio)` en horarios de sección.
- `UNIQUE(evento_id, vinculo_apoderado_estudiante_id)` en avisos al destinatario.
- `UNIQUE(clave_idempotencia)` en eventos de notificación, para evitar avisos duplicados en reintentos.
- Las valoraciones académicas guardan competencia/período/nivel; no se promedian letras automáticamente.

## Supabase, seguridad y migraciones

- Aplicar migraciones con Supabase CLI (`supabase db push`), después de enlazar el proyecto local al proyecto Supabase.
- Ejecutar `supabase/seed.sql` solo en desarrollo. El generador crea 2 secciones por grado y una cantidad configurable de estudiantes por sección (10 por defecto); genera una relación de apoderado para cada estudiante y agrupa hermanos mediante el parámetro `EDUFAST_DEMO_SIBLING_PAIRS`. Crea cuentas sintéticas solo para personal adulto y apoderados, nunca para estudiantes. Los correos de prueba usan `EDUFAST_DEMO_EMAIL_DOMAIN` y el hash de contraseña se deriva de `EDUFAST_DEMO_PASSWORD`, ambos definidos localmente en `backend/.env`. Las notificaciones de las relaciones demo quedan desactivadas mientras sus contactos ficticios no estén verificados.
- `supabase/seed_primaria.sql` se ejecuta **después** de `seed.sql` y convierte la demostración genérica en una IE de primaria: desactiva las secciones de otros niveles, crea una tutora por sección más los especialistas de Inglés y Educación Física, el subdirector y la secretaría, y desarrolla una unidad completa de 4.º B con sus doce sesiones. Renombra los correos para que el rol se lea en el prefijo (`tutor.4b@`, `especialista.ingles@`, `director@`, `subdirector@`, `secretaria@`, `apoderado.NNNN@`) y revoca las cuentas de muestra que esta IE no tiene. No escribe ninguna contraseña: reutiliza el hash que ya dejó `seed.sql`.
- La IE que queda simulada, con los valores por omisión de `aplicar_local.sh`:

| | |
|---|---|
| Secciones | 12 (dos por grado, 1.º a 6.º) |
| Estudiantes | 300, 25 por sección |
| Apoderados | 294 (menos que estudiantes por las 6 parejas de hermanos) |
| Docentes | 14: una tutora por sección más Inglés y Educación Física |
| Dirección | 1 director, 1 subdirector, 1 secretaría |

  El tamaño de sección sale de las rondas de campo (`EVIDENCIA.md`: aulas de 24 a 25
  estudiantes). Los estudiantes de inicial y secundaria que genera `seed.sql`, que es la
  demostración genérica de todos los niveles, se eliminan: si se dejaran con la matrícula
  cerrada el recuento de la institución diría 550 estudiantes para 12 secciones.
- Ambas semillas son reaplicables: el conflicto de una cuenta se resuelve por persona y no por correo, precisamente porque `seed_primaria.sql` renombra los correos.
- La semilla dinámica no se ejecuta automáticamente desde Supabase CLI. En local, usa `supabase db reset` y luego ejecútala con `psql` contra el puerto local configurado, o corre `./supabase/aplicar_local.sh`, que hace migraciones y semillas en un contenedor de Postgres sin necesidad de la CLI. En un proyecto remoto de desarrollo, despliega primero las migraciones con `supabase db push` y ejecuta luego las semillas con `psql` usando la URI `SUPABASE_DB_URL`. No ejecutes la semilla en producción.
- La clave de conexión solo vive en variables de entorno del backend. No agregar `service_role`, contraseña ni JWT secreto al repositorio.
- El backend se conecta con un rol de servicio que ignora RLS (equivalente al usuario `postgres` o `service_role` de Supabase, o a un rol local con `BYPASSRLS`). Las políticas RLS están pensadas para bloquear el acceso directo de clientes (`anon`/`authenticated`); el backend nunca publica credenciales a web/móvil.
- Las entidades JPA mapean estas tablas y Hibernate valida el esquema al arrancar (`ddl-auto=validate`); las migraciones son la única fuente de verdad de la estructura.

## Compatibilidad con SIAGIE

SIAGIE se trata como sistema externo de referencia. Los códigos externos se guardan en campos explícitos o en una futura tabla de correspondencias; no se duplica la aplicación ni se asume una API de integración no confirmada.

## Referencias oficiales usadas para el modelo

- [Disposiciones que regulan el proceso de matrícula en Educación Básica, RM N.° 010-2026-MINEDU](https://repositorio.minedu.gob.pe/handle/20.500.12799/11849): trayectoria/FUM, matrícula, secciones y traslados.
- [Currículo Nacional de la Educación Básica](https://www.minedu.gob.pe/curriculo/pdf/curriculo-nacional-de-la-educacion-basica.pdf) y programas curriculares de [Primaria](https://repositorio.minedu.gob.pe/handle/20.500.12799/4549) y [Secundaria](https://repositorio.minedu.gob.pe/handle/20.500.12799/4550): niveles, ciclos, grados, áreas y competencias.
- [Orientaciones de planificación, mediación y evaluación en Secundaria](https://repositorio.minedu.gob.pe/bitstream/handle/20.500.12799/6646/Planificaci%c3%b3n%2c%20mediaci%c3%b3n%20y%20evaluaci%c3%b3n%20de%20los%20aprendizajes%20en%20la%20Educaci%c3%b3n%20Secundaria.pdf?isAllowed=y&sequence=1): planificación flexible, criterios, evidencias y retroalimentación.
- [Planificación curricular en Primaria](https://repositorio.minedu.gob.pe/handle/20.500.12799/5310) y [sesiones multigrado](https://repositorio.minedu.gob.pe/handle/20.500.12799/10342): organización de anual → experiencia/unidad → sesiones y adaptación al contexto.
- [RVM N.° 094-2020-MINEDU](https://www.gob.pe/institucion/minedu/normas-legales/541161-094-2020-minedu), modificada por [RVM N.° 048-2024-MINEDU](https://www.gob.pe/institucion/minedu/normas-legales/5518274-048-2024-minedu): valoración por competencias, niveles de logro y conclusiones descriptivas.
