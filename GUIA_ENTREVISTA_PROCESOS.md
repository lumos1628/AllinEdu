# Guía de entrevista — Levantamiento de procesos docentes

> Objetivo: obtener el **mapa completo de procesos pedagógicos y administrativos** de una docente
> para construir los **casos de uso del negocio** (qué hace la escuela hoy, con o sin software) y
> los **casos de uso del sistema** (qué parte de eso hará AllinEdu).
>
> Ya tenemos dolores y objetivos (`EVIDENCIA.md`). Esta entrevista **no busca más dolores**: busca
> el *cómo*: pasos, actores, documentos, reglas, plazos y excepciones.

Duración sugerida: 60–90 min. Idealmente en la escuela, con sus materiales a la mano.

---

## 0. Antes de empezar

- **Consentimiento** para grabar. Recordarle que se anonimiza (`Docente 2`, `Docente 3`…) y que no
  se registran nombres de estudiantes.
- Pedirle que tenga a la mano: registro auxiliar, una sesión de aprendizaje, una ficha, un
  instrumento (lista de cotejo/rúbrica), el celular con las fotos que usa, y si puede, acceso a SIAGIE.
- **Técnica principal: "muéstreme".** Lo que la gente *dice* que hace difiere de lo que *hace*.
  Cada vez que mencione un documento o herramienta: *"¿me lo puede mostrar?"* (foto, con permiso).
- **Pregunte por el último caso real**, no por lo general: *"¿Cómo lo hizo **ayer**?"* en vez de
  *"¿Cómo lo hace normalmente?"*.
- **No proponga soluciones** ni muestre la app durante el levantamiento. Contamina las respuestas.
- Repreguntas comodín: *"¿Y luego?"*, *"¿Qué pasa si…?"*, *"¿Quién le pide eso?"*,
  *"¿Dónde queda guardado?"*, *"¿Cuánto tiempo le toma?"*, *"¿Alguna vez salió mal?"*

### Ficha de registro por proceso (llenar durante o justo después)

Cada proceso que aparezca se anota con este molde. **Es la materia prima directa de un caso de uso.**

| Campo | Pregunta que lo llena |
|---|---|
| Nombre del proceso | ¿Cómo le llama usted a esto? |
| Disparador | ¿Qué hace que empiece? (hora, fecha, pedido de alguien, evento) |
| Frecuencia | ¿Diario, semanal, por unidad, bimestral, anual? |
| Actores | ¿Quién participa? ¿Quién lo pide? ¿Quién lo recibe? ¿Quién lo revisa/aprueba? |
| Entradas | ¿Qué necesita tener antes? (documento, dato, norma) |
| Pasos | Paso a paso, en orden |
| Salidas | ¿Qué produce? ¿A dónde va? |
| Herramientas | Papel, Excel, Word, WhatsApp, IA, SIAGIE, celular… |
| Tiempo | ¿Cuánto le toma? ¿En el colegio o en casa? |
| Reglas / normas | ¿Hay formato obligatorio, plazo, norma que lo exija? |
| Excepciones | ¿Qué sale mal? ¿Qué hace entonces? |
| Duplicidad | ¿Escribe el mismo dato en más de un lugar? |

---

## 1. Contexto (5 min)

1. ¿Qué grado(s) y cuántas secciones tiene? ¿Cuántos estudiantes por sección?
2. ¿Es polidocente o unidocente/multigrado? ¿Dicta todas las áreas o hay docentes de otras áreas
   (Educación Física, Inglés, Arte)?
3. ¿Cuál es su horario? ¿Cuánto duran los bloques? ¿Tiene horas no lectivas dentro del colegio?
4. ¿Qué otros cargos tiene además de enseñar? (tutoría, comisiones, coordinación, comités)
5. ¿Quiénes son las personas con las que más interactúa por trabajo? (director/a, subdirector/a,
   coordinador/a, auxiliar, psicólogo/a, otros docentes, apoderados, UGEL)

---

## 2. Tres líneas de tiempo (20 min)

Recorrerlas **en este orden**. Son el esqueleto: todo proceso que aparezca se ancla a una de ellas.

### 2a. El día de ayer, hora por hora
> *"Empecemos desde que se levantó ayer hasta que se acostó. ¿Qué fue lo primero que hizo
> relacionado con el trabajo?"*

- Antes de llegar al colegio
- Al llegar (formación, ingreso, asistencia)
- Cada bloque de clase: inicio, desarrollo, cierre
- Recreo / horas libres
- Salida
- **En casa** (esto es lo no remunerado: detallar cada tarea y cuánto le tomó)

### 2b. Una semana típica
- ¿Qué hace solo ciertos días? (reuniones colegiadas, GIA, entrega de documentos, planificación)
- ¿Cuándo planifica la semana siguiente? ¿Qué día imprime?
- **Pendiente de `EVIDENCIA.md` §5:** ¿cuántas horas le dedica a planificar **por semana**, contando
  sesiones, fichas, instrumentos, imágenes e impresión? ¿Y solo a "planificar" la sesión?
  (resolver la brecha 2–5 h/semana vs 2.5–4 h/día)

### 2c. El año escolar
> Dibujar con ella una línea de marzo a diciembre.

- Inicio del año: diagnóstico, planificación anual, matrícula, carpeta pedagógica
- Inicio y cierre de cada unidad / experiencia de aprendizaje
- Cierre de cada bimestre/trimestre: evaluación, registro, conclusiones descriptivas, libretas,
  reunión con padres
- Eventos: concursos, simulacros, actuaciones, ECE/evaluaciones censales o muestrales, visitas de monitoreo
- Fin de año: actas, informes, recuperación

---

## 3. Procesos pedagógicos (25 min)

### 3.1 Planificación
- ¿Qué documentos de planificación hace? (programación anual, unidad/experiencia, sesión).
  ¿Cuáles le exigen y cuáles hace por su cuenta?
- ¿De dónde parte? (CNEB, programación del colegio, cuadernos de trabajo del MINEDU, recursos de
  PerúEduca/Aprendo en casa, IA)
- ¿Planifica sola o en equipo con docentes del mismo grado? ¿Comparten material?
- ¿Alguien revisa o aprueba su planificación? ¿Cuándo y cómo se la entregan/devuelven?
- **Muéstreme el prompt que usa en la IA.** ¿Qué corrige siempre de lo que genera?
- ¿Qué elementos lleva una sesión? (propósito, competencias, capacidades, desempeños, criterios,
  evidencia, momentos, materiales)

### 3.2 Materiales: fichas y recursos
- ¿Cómo arma una ficha? ¿De dónde saca ejercicios e imágenes?
- ¿Cómo decide los niveles de dificultad? ¿Cuántos niveles? ¿Cómo sabe qué estudiante va en cuál?
- ¿Quién imprime, dónde y quién paga? ¿Qué pasa si no hay impresora o tinta?
- ¿Reutiliza material de años anteriores? ¿Dónde lo guarda?

### 3.3 Ejecución de la clase
- ¿Qué registra **durante** la clase? ¿En qué? (cuaderno, cartel, celular, memoria)
- ¿Cómo sabe quién participó, quién terminó la ficha, quién necesita ayuda?
- ¿Qué pasa cuando la clase no se pudo dar como estaba planificada? (charla imprevista, falla de
  equipo, ausencias) ¿Reprograma? ¿Lo registra en algún lado?

### 3.4 Evaluación
- ¿Qué instrumentos usa? (lista de cotejo, rúbrica, escala, guía de observación) ¿Quién los diseña?
- ¿Cuándo evalúa: en clase, en casa, al cierre de unidad?
- Paso a paso de **un criterio de una competencia** desde la clase hasta el registro oficial.
- ¿Qué escala usa (AD, A, B, C)? ¿Cómo decide la calificación final de la competencia en el
  bimestre? (último logro, promedio, juicio global)
- ¿Cómo redacta las conclusiones descriptivas? ¿Para quiénes? ¿Cuánto le toma?
- ¿Qué norma o directiva sigue? ¿Quién verifica que la cumpla?

### 3.5 Refuerzo, nivelación e inclusión
- ¿Qué hace con un estudiante que no logró el criterio? ¿Le da otra actividad, lo deriva?
- ¿Tiene estudiantes con NEE? ¿Hace adaptaciones? ¿Quién las aprueba o registra?
- ¿Hace repaso de temas anteriores? ¿Cómo decide qué repasar?
- ¿Cómo prepara a estudiantes para concursos o evaluaciones externas?

### 3.6 Tutoría y convivencia
- ¿Qué registra como tutora? (incidencias, entrevistas, derivaciones, SíseVe)
- ¿Quién más se entera y qué se hace con eso?

> Regla 0b: aquí solo **describimos** el proceso. No preguntamos "¿le gustaría que el sistema
> califique por usted?" — está fuera del alcance.

---

## 4. Procesos administrativos (20 min)

### 4.1 Asistencia
- Confirmar el flujo de I-2: cartel → foto → registro auxiliar. ¿Además va a SIAGIE u otro lugar?
- ¿Quién controla tardanzas? ¿Hay auxiliar que también pasa lista?
- ¿Cómo se justifica una falta? ¿Quién recibe la justificación, en qué formato, dónde se archiva?
- ¿Alguien le pide reportes de asistencia? ¿Cada cuánto, en qué formato?

### 4.2 Registro de notas y SIAGIE (bloquea la decisión D-1)
- **Muéstreme cómo carga notas en SIAGIE.** ¿Lo hace usted o alguien más (director, secretaría)?
- ¿Tiene opción de importar un archivo (Excel) o es todo manual? ¿Qué formato pide?
- ¿Cuántas veces tipea la misma nota? (registro auxiliar → Excel → SIAGIE…)
- ¿Cuándo se cierra el registro? ¿Qué pasa si se equivoca después del cierre?

### 4.3 Documentos que le piden
- Listar **todo** lo que entrega a dirección/UGEL: carpeta pedagógica, informes de avance,
  informes técnico-pedagógicos, actas, nóminas, fichas de monitoreo, evidencias fotográficas, planes
  de mejora, informes de tutoría…
- Para cada uno: ¿quién lo pide, con qué plazo, en qué formato (físico, Drive, WhatsApp), quién lo
  revisa, qué pasa si no se entrega?

### 4.4 Monitoreo y acompañamiento (bloquea la decisión D-4)
- ¿El director o coordinador observa sus clases? ¿Cómo? ¿Qué instrumento usa? ¿Qué le devuelven?
- ¿Cómo le **asignan** tareas o actividades? ¿Por dónde llegan? ¿Cómo reporta que las hizo?
- ¿Hay alguna evaluación de desempeño docente? ¿Qué cuenta en ella?

### 4.5 Reuniones y comisiones
- ¿Qué reuniones tiene (colegiadas, con padres, comisiones)? ¿Produce actas o documentos?

---

## 5. Comunicación con familias (10 min)

- ¿Por qué canales le escriben? ¿Grupo de WhatsApp del aula, privado, llamada, cuaderno de control?
- ¿Qué le preguntan más? (tareas, faltas, notas, quejas, permisos)
- ¿Qué le comunica usted a ellos y cuándo? (citaciones, avisos, libretas, reuniones)
- Confirmar I-4: ¿qué pasa concretamente si no responde fuera de horario? ¿Alguna vez hubo una
  queja formal? ¿A quién llegó, qué se hizo? *(vacío §8.4 de `EVIDENCIA.md`)*
- ¿Cómo se identifica quién es el apoderado autorizado de un estudiante? ¿Quién lo registra?
- ¿Tiene familias sin celular o sin internet? ¿Cómo se comunica con ellas?

---

## 6. Cierre (5 min)

1. *"Si tuviera que delegar **una** tarea a un asistente humano mañana, ¿cuál sería y por qué?"*
2. *"¿Hay algo que hace que nadie le pide pero hace igual?"* (procesos invisibles)
3. *"¿Qué le pregunté mal o qué me faltó preguntar?"*
4. ¿Quién en el colegio podría autorizar un piloto con estudiantes? *(D-3)*
5. ¿Conoce a otros docentes que podamos entrevistar? *(validar tiempos con n ≥ 3, §8.5)*
6. Pedir fotos de los documentos mostrados (sin datos de estudiantes) y agradecer.

---

## 7. Después de la entrevista: de procesos a casos de uso

1. **Transcribir y verificar** contra el audio antes de citar (regla de `EVIDENCIA.md`).
2. Con las fichas de la sección 0, construir el **inventario de procesos** agrupado por la cadena:
   planificar → aplicar → evaluar → refuerzo → comunicar, más un grupo *administrativo*.
3. **Casos de uso del negocio (CUN):** cada proceso completo tal como ocurre hoy, con actores del
   negocio (docente, director, apoderado, UGEL, SIAGIE como sistema externo). Se modelan sin
   AllinEdu. Útil diagramarlos con BPMN o diagrama de actividades con carriles por actor.
4. **Casos de uso del sistema (CUS):** para cada CUN, marcar qué pasos automatiza o soporta
   AllinEdu. Cada paso marcado se vuelve un CUS o se suma a uno de `USE_CASES.md`.
   Los pasos que quedan fuera explican la frontera del sistema (y van a `ALCANCE.md` §4 con su razón).
5. Señales para priorizar: pasos con **duplicidad** de datos, pasos hechos **en casa**, pasos hechos
   **de memoria**, y pasos que alguien **revisa o exige** (ahí hay reglas de negocio).
6. Registrar en `EVIDENCIA.md` lo nuevo (con fuente, `n` y cita anonimizada) y en `ALCANCE.md` §8
   cualquier cambio de alcance que resulte.
