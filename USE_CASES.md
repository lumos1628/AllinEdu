# Casos de uso — AllinEdu

> Flujos del producto definido en [`ALCANCE.md`](ALCANCE.md), sustentado en
> [`EVIDENCIA.md`](EVIDENCIA.md). Los casos marcados **[EXISTE]** ya están implementados;
> el resto está por construir.

---

## Actores

| Actor | Responsabilidad |
|---|---|
| Docente | Prepara sesiones, fichas e instrumentos; aplica la clase; registra asistencia y evidencia; valora criterios; asigna refuerzo; atiende consultas de familias. |
| Apoderado autorizado | Consulta a los estudiantes vinculados, recibe avisos, justifica ausencias y hace consultas asincrónicas. |
| Director / administrador | Gestiona año escolar, secciones, matrícula, asignación de docentes y vínculos de apoderados. |
| Servicio de notificaciones | Encola, envía, reintenta y registra el resultado de cada aviso. |
| Estudiante | Participa en prácticas y diagnósticos. Puede estar registrado sin tener cuenta. |

Una persona puede tener más de un rol: por ejemplo, ser docente y apoderada.

---

## Grupo A — Planificar

Prioridad 1. Ataca el dolor más costoso: 2.5 a 4 horas diarias de trabajo no remunerado.

### UC-01 — Preparar las sesiones de la unidad **[EXISTE]**

- **Actor:** docente.
- **Precondición:** la unidad existe con sus sesiones, competencias y criterios.
- **Flujo:** desde la unidad, el docente pulsa «Preparar las N sesiones». El sistema genera en
  segundo plano, en orden, la sesión completa de cada sesión en borrador que no tiene secuencia:
  secuencia didáctica (cada actividad del desarrollo ligada a un paso de la institución),
  evidencia, DUA y materiales. Cada borrador ve a las sesiones anteriores, a «Mi aula» (UC-22) y
  a los desempeños del grado. La faja de la unidad se teje en pantalla mientras llegan, y el
  docente puede cerrar la pantalla.
- **Variante:** dentro de una sesión vacía, «Proponer un borrador» genera solo esa.
- **Regla:** el borrador nace en estado **no aprobado**. Nada generado se publica ni se imprime
  sin aprobación explícita del docente. Una sesión aprobada o en la que el docente ya escribió
  no entra al lote.
- **Regla:** antes de mostrar un borrador, las reglas fijas de UC-04 lo revisan; si encuentran
  algo, el generador tiene un segundo intento con esas correcciones.
- **Excepción:** si el generador no responde, esa sesión queda vacía y las demás siguen; volver a
  pulsar reintenta solo las que faltan.

### UC-02 — Generar ficha de trabajo diferenciada por niveles

- **Actor:** docente.
- **Flujo:** a partir de la sesión aprobada, el sistema propone ejercicios agrupados por nivel
  de dificultad. El docente filtra, reemplaza o reordena.
- **Regla:** la diferenciación por niveles es obligatoria; una ficha de un solo nivel es un caso
  particular, no el comportamiento por defecto.
- **Sustento:** la docente ya hace esta adaptación a mano (EVIDENCIA I-5).

### UC-03 — Generar instrumento de evaluación

- **Actor:** docente.
- **Flujo:** el sistema propone criterios de evaluación y niveles de logro ligados a la
  competencia de la sesión. El docente los ajusta.
- **Regla:** los criterios quedan vinculados a la competencia, para que la evidencia registrada
  en clase (UC-09) pueda valorarse contra ellos.

### UC-04 — Revisar un borrador respondiendo sus nudos **[EXISTE]**

- **Actor:** docente.
- **Flujo:** el borrador llega con sus **nudos**: las pocas cosas que necesitan la mirada del
  docente. Cada uno se responde con «Está bien» o «Cámbialo» (con lo que quiere en su lugar).
  Con los cambios pedidos, «Rehacer con tus respuestas» teje otro borrador que cambia solo lo
  pedido. Cuando le convence, lo aprueba. También puede editarlo a mano o pedir otro borrador con
  sus palabras («más corta», «sin salir del aula»).
- **De dónde salen los nudos:**
  - Reglas fijas, sin IA: los minutos no suman la duración de la clase; falta un paso didáctico
    de la institución o van en otro orden; el inicio no comunica los criterios; el desempeño pide
    material concreto y no hay ninguno.
  - El generador: lo que tuvo que suponer de la comunidad o los recursos («supuse que hay un
    mercado cerca»), como máximo tres, con la frase del borrador, que se señala en el documento.
    Lo que el docente ya contó en «Mi aula» no se pregunta.
- **Regla:** el sistema conserva qué fue generado y qué fue editado por una persona, y guarda cada
  borrador tal como llegó (modelo, versión del prompt, intentos, duración) para medir la tasa de
  edición.
- **Regla:** un borrador nuevo nunca pisa lo que el docente escribió: si editó la sesión, se
  cambia desde el editor.
- **Excepción:** un borrador rechazado no se descarta en silencio; queda disponible para
  regenerarse con indicaciones nuevas.

### UC-05 — Exportar material para aplicar

- **Actor:** docente.
- **Flujo:** descarga o manda a imprimir la sesión, la ficha y el instrumento ya aprobados.
- **Regla:** solo se exporta material aprobado.

### UC-22 — Contar cómo es mi aula **[EXISTE]**

- **Actor:** docente.
- **Flujo:** en la unidad, el docente escribe una vez y con sus palabras lo que los borradores
  deben saber: qué hay cerca de la escuela, qué materiales tienen, cómo trabajan. Todos sus
  borradores lo usan. Al responder «Cámbialo» a un supuesto, puede marcar «Recordarlo en Mi aula»
  y la respuesta se agrega ahí.
- **Regla:** nunca lleva datos de estudiantes. La pantalla lo advierte y el texto se envía al
  generador, por eso se limita a la comunidad y el aula. Hasta 2000 caracteres.

---

## Grupo B — Aplicar en clase

### UC-06 — Registrar asistencia de la sección **[EXISTE]**

- **Actor:** docente.
- **Flujo:** selecciona su sección y la fecha; el sistema presenta a los estudiantes
  matriculados en esa sección para esa fecha; marca a cada uno como asistió, tardanza o faltó (tarjetas por color) y guarda.
- **Reglas:** registrar asistencia no crea ni modifica una matrícula. Solo las jornadas
  confirmadas quedan como registro oficial.
- **Objetivo de diseño:** eliminar el doble registro vía fotografía (EVIDENCIA I-2). La
  asistencia se captura una sola vez, en el aula, en digital.
- **Fallos de red:** se informa el error y se permite reintentar; hoy no hay almacenamiento
  local.

### UC-07 — Consultar la asistencia registrada **[EXISTE]**

- **Actor:** docente o apoderado autorizado.
- **Flujo:** consulta por sección y fecha (docente) o por estudiante vinculado (apoderado).
- **Regla:** si no hay registro para esa fecha, devuelve lista vacía y se muestran los alumnos
  sin estado confirmado.

### UC-08 — Corregir asistencia **[EXISTE]**

- **Actor:** docente autorizado o administrador.
- **Flujo:** corrige un registro, indica el motivo y queda guardado quién y cuándo lo cambió.
- **Regla:** si la ausencia ya se notificó, se registra un evento de corrección; no se borra el
  evento anterior ni se duplica el aviso.

### UC-09 — Registrar evidencia por criterio durante la clase

- **Actor:** docente.
- **Flujo:** durante la sesión marca participación, exposición o logro por criterio para los
  estudiantes que corresponda.
- **Regla:** el registro debe poder hacerse en segundos y sin interrumpir la clase; si no, el
  docente volverá a evaluar de memoria en la noche.
- **Sustento:** hoy evalúa evocando desde el recuerdo (EVIDENCIA I-3).

---

## Grupo C — Evaluar

### UC-10 — Valorar criterios por competencia

- **Actor:** docente.
- **Flujo:** revisa la evidencia registrada en clase y las fichas recibidas, y asigna el nivel
  de logro por criterio.
- **Regla:** las valoraciones guardan competencia, período y nivel. **No se promedian letras
  automáticamente.**

### UC-11 — Consultar evidencia acumulada por competencia

- **Actor:** docente.
- **Flujo:** consulta el avance de un estudiante o de la sección por competencia a lo largo del
  período.
- **Para qué sirve:** es el insumo de las conclusiones descriptivas y la base para decidir el
  refuerzo.

---

## Grupo D — Refuerzo y retención

### UC-12 — Aplicar diagnóstico por competencia

- **Actor:** docente.
- **Precondición:** existe banco de ítems para la competencia y el tema.
- **Flujo:** asigna un diagnóstico breve; el sistema devuelve el mapa de brechas por criterio.
- **Regla:** es práctica formativa de bajo riesgo. **No es calificación oficial ni vigilancia.**

### UC-13 — Asignar práctica progresiva

- **Actor:** docente.
- **Flujo:** a partir del diagnóstico, el sistema propone una secuencia de ítems de dificultad
  creciente; el docente la ajusta y la asigna.
- **Regla:** el docente decide siempre qué se asigna; el sistema propone.

### UC-14 — Programar y aplicar repaso espaciado

- **Actor:** docente; el sistema programa.
- **Flujo:** los ítems ya practicados reaparecen con intervalos para sostener la retención.
- **Sustento:** Agarwal (2021) y Latimier (2021). Ataca el olvido de temas ya enseñados.

### UC-15 — Consultar resultados de práctica

- **Actor:** docente.
- **Flujo:** compara resultados de diagnóstico, práctica posterior y prueba diferida del tema.
- **Regla:** el sistema presenta evidencia; **no emite juicios ni decide el siguiente paso
  pedagógico.**

---

## Grupo E — Comunicación con familias

### UC-16 — Notificar ausencia confirmada

- **Actor:** servicio de notificaciones.
- **Disparador:** asistencia confirmada con una o más ausencias.
- **Flujo:** resuelve los apoderados vigentes y autorizados, aplica sus preferencias y crea una
  notificación por destinatario y una entrega por canal o dispositivo.
- **Idempotencia:** reprocesar el mismo evento no envía duplicados.
- **Fuera de cobertura:** un teléfono apagado no puede mostrar avisos; el proveedor puede
  retenerlos hasta la reconexión, sujeto a expiración.

### UC-17 — Justificar una ausencia

- **Actor:** apoderado autorizado; aprueba el docente.
- **Flujo:** el apoderado responde al aviso adjuntando el documento o la razón; el docente
  aprueba u observa; el estado queda registrado junto a la ausencia.
- **Regla:** la justificación no borra la ausencia; la acompaña.

### UC-18 — Atender una consulta de apoderado

- **Actor:** docente y apoderado autorizado.
- **Flujo:** hilo asincrónico por estudiante, iniciado por el docente. El sistema registra
  cuándo se recibió y cuándo se respondió, y muestra el horario de atención.
- **Regla de diseño:** el valor es la **constancia de que el docente respondió** y el límite de
  horario visible, no la rapidez. La docente contesta a las 11 de la noche por temor a reportes
  negativos de desempeño (EVIDENCIA I-4); la trazabilidad es lo que la protege.
- **Acotado deliberadamente:** sin tiempo real, sin grupos, sin chat libre. Si se vuelve una
  mensajería, el caso de uso falló.

### UC-19 — Vincular un apoderado con un estudiante

- **Actor:** director / administrador.
- **Flujo:** registra la relación, el tipo de vínculo, su vigencia y los permisos para consultar
  información y recibir avisos. Verifica al menos un medio de contacto antes de activar
  notificaciones.
- **Variantes:** un estudiante puede tener varios apoderados y una persona puede estar vinculada
  a varios estudiantes. El vínculo puede vencer o revocarse sin borrar su historial.
- **Precondición de los casos UC-16 a UC-18:** sin vínculo vigente y autorizado no hay avisos ni
  consultas.

### UC-20 — Registrar dispositivo y preferencias de aviso

- **Actor:** apoderado.
- **Flujo:** inicia sesión en Android o en la web instalada, autoriza las notificaciones y
  registra el token del dispositivo. Puede activar o desactivar categorías de aviso.
- **Variantes:** el permiso puede denegarse o revocarse; en ese caso los avisos permanecen en la
  bandeja interna de la aplicación si la cuenta está habilitada.

### UC-21 — Abrir y consultar una notificación

- **Actor:** apoderado autorizado.
- **Flujo:** toca el aviso del sistema, inicia sesión si hace falta y abre el detalle del
  estudiante y la fecha. El servidor vuelve a verificar que el vínculo esté vigente.
- **Privacidad:** el texto visible en la pantalla bloqueada debe revelar lo mínimo; el detalle
  solo se obtiene dentro de una sesión autenticada.

---

## Reglas transversales

1. **La IA propone, la persona aprueba.** Nada generado llega al estudiante ni a un registro
   oficial sin aprobación explícita del docente.
2. **El sistema entrega evidencia; el docente decide.** Sin calificación automática ni
   recomendaciones pedagógicas vinculantes.
3. La matrícula es por institución, año, grado y sección; nunca por curso individual.
4. Asistencia, evidencia de aprendizaje y valoración por competencias son registros distintos.
5. Un estado sin confirmar no produce avisos.
6. Las correcciones y los cambios de matrícula conservan historial.
7. La entrega de un aviso por el proveedor no significa que el apoderado lo haya leído.
8. Los simulacros y prácticas son formativos y de bajo riesgo. No son evaluación de alto
   impacto ni mecanismo de vigilancia.
9. Los datos de prueba son sintéticos. Nunca datos personales reales de menores.

---

## Fuera del alcance

Generación de videos con IA, integración automática con SIAGIE, registro oficial de notas
(decisión pendiente D-1), portal familiar completo, chat libre, cuentas de estudiante con acceso
general y calificación automática.

El detalle de cada exclusión y su razón está en [`ALCANCE.md`](ALCANCE.md) §4.
