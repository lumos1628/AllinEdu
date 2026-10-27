# Plan hasta la sustentación — AllinEdu

> Plan de ejecución con la restricción real: **~7 semanas** (saliendo de Semana 9, sustentación
> en Semana 16 según [`MOBILE_ROADMAP.md`](MOBILE_ROADMAP.md)), una persona, y un curso de
> Kotlin con entregas semanales propias.
>
> Las épicas y su justificación están en [`EPICAS.md`](EPICAS.md). Este documento decide **qué
> se construye, en qué orden y qué se deja fuera**.

---

## 1. La conversación incómoda primero

**El producto completo no entra en 7 semanas.** E0 + E1 + E1b + E2 + E3 + E4 + E5 son meses de
trabajo para un equipo. Intentar todo garantiza llegar a la Semana 16 con siete mitades y nada
demostrable.

La buena noticia es que `ALCANCE.md` §6 ya delimitó el piloto —una competencia, un tema— y el
esquema de base de datos ya está construido. Lo que falta es **elegir la rebanada vertical que
prueba la tesis** y documentar el resto como roadmap, que es exactamente lo que un proyecto
descriptivo debe hacer.

### Qué hay que poder demostrar en la Semana 16

La tesis del proyecto es la historia principal de `EPICAS.md` §2: el docente revisa y aprueba en
20 minutos lo que hoy le toma horas. Para sustentar eso hace falta:

1. La cadena funcionando de punta a punta **para un grado, un área y una unidad**:
   unidad aprobada → sesiones generadas en lote → revisión por excepción → imprimible.
2. La captura de evidencia en clase sobre esa unidad, apoyada en la asistencia que ya existe.
3. **La medición**: línea base contra tiempo con el producto, y SUS.
4. Los documentos de investigación, que ya están sólidos.

Nada más es necesario para defender. Todo lo demás es ambición.

---

## 2. Corrección importante: E0 estaba mal planteada

En `EPICAS.md` puse E0 como "banco de ítems y plantillas" y dije que bloqueaba E1, E3 y E4. **Eso
es incorrecto y hace parecer el plan imposible.** Son dos cosas distintas:

| | Qué es | Qué bloquea | ¿Entra? |
|---|---|---|---|
| **E0a** | **Carga del currículo normativo**: `competencias`, `capacidades`, `estandares_aprendizaje`, `desempenos_grado`, `areas_curriculares`, `planes_estudio` | **E1 y E3** | **Obligatorio** |
| **E0b** | **Banco de ítems de práctica** con niveles de dificultad, intentos y repaso espaciado | **Solo E4** | **Fuera — a roadmap** |

E1 no necesita el banco de ítems: necesita el currículo cargado para no inventar competencias.
Las tablas de currículo están **vacías** hoy (`supabase/seed.sql` no inserta ni una fila en
ninguna de ellas). Eso sí es bloqueante y sí es alcanzable, porque se acota a un grado y un área.

Separar E0a de E0b es lo que hace viable el plan. Con E0b dentro, no lo era.

---

## 3. Qué entra y qué sale

### Entra

| Épica | Alcance recortado |
|---|---|
| **E0a** carga de currículo | **Un grado, un área.** Competencias con sus capacidades, estándar del ciclo y desempeños del grado |
| **E1** planificar | Unidad → sesiones en lote → ficha diferenciada → instrumento → imprimible. **Una unidad completa** |
| **E2** aplicar en clase | Asistencia **ya existe**. Se agrega captura de evidencia por criterio |
| **E3** evaluar | Valorar criterios y consultar evidencia acumulada por competencia |

### Sale, documentado como roadmap

| Fuera | Por qué |
|---|---|
| **E0b** banco de ítems | Es contenido, no código. 40–60 ítems revisados uno por uno no caben |
| **E4** refuerzo, práctica progresiva, repaso espaciado | Depende de E0b |
| **E5** familias | No depende de nada y es barata, pero no sustenta la tesis. Candidata si sobra tiempo |
| **E1b** replanificación automática | El esquema ya lo soporta (`ejecuciones_sesion.estado`). Se **registra** la interrupción; no se re-acomoda el plan automáticamente |
| Conclusiones descriptivas, registro de notas | Ya estaban en roadmap en `ALCANCE.md` §3 |

Dejar E4 fuera cuesta algo y hay que decirlo en la sustentación: el ODS 4 queda sostenido por
la **evidencia por competencia** (E3), no por el refuerzo. Es defendible —`ALCANCE.md` §2 ya
argumenta que sin evidencia no hay insumo para nada— pero conviene anticiparlo.

---

## 4. La jugada que hace que esto quepa: converger curso y producto

El roadmap móvil tiene tema obligatorio por semana: Room (S7), MVVM (S8), Retrofit (S9),
CRUD y listas (S10). **Esos temas se pueden cumplir construyendo las pantallas del producto en
lugar de ejercicios desechables.**

| Semana del curso | Tema | Hacerlo sobre |
|---|---|---|
| S10 | CRUD + listas optimizadas | Lista de sesiones de la unidad, con su estado de aprobación |
| S11–S15 | Firebase, animaciones, notificaciones | Pantalla de revisión por excepción; avisos de "paquete de mañana listo" |

Así el trabajo se hace una vez y cuenta doble. **Si no haces esto, no llegas.**

---

## 4b. El otro curso: Investigación e Innovación Tecnológica

Son **dos cursos con el mismo proyecto**, y ambos cierran en S15–S16:

| Curso | Docente | Entregable |
|---|---|---|
| Programación en Móviles | BPAREJA | Laboratorios semanales + app (S16: sustentación) |
| Investigación e Innovación Tecnológica | O. Medina | Talleres de Design Thinking + **Pitch Final** (Taller 14, guía en S15) |

El perfil de proyecto ya está presentado (`PC-02-S8`): *"AllinEdu: gestión docente y simulacros
pedagógicos para fortalecer aprendizajes"*, tipo **innovación tecnológica — prototipo funcional
web y móvil**, con equipo de dos personas.

### El kit del taller, y qué ya tienes cubierto

El curso trabaja Design Thinking con herramientas nombradas. Esta es la correspondencia con lo
que ya existe en el repositorio:

| Herramienta del taller | Qué la cubre hoy | Estado |
|---|---|---|
| Entrevistas y encuestas | 17 audios, 3 h 37 min + 2 encuestas | **Hecho**, `EVIDENCIA.md` §2 |
| Insight | I-1 a I-9, literalmente rotulados como insights | **Hecho** |
| Árbol de problemas | Cadena planificación → fichas → DUA → evaluación de memoria | Está disperso; **falta dibujarlo** |
| Buyer personas | Perfiles de la ronda, incluidos los casos negativos (`R11`, `R12`) | Material listo, **falta redactarlos** |
| Mapa de empatías | Transcripciones | Material listo, **falta armarlo** |
| ODS | ODS 4 y 8 argumentados | **Hecho**, `ALCANCE.md` §1 |
| Mapa de stakeholders | Docente, dirección, apoderado, UGEL, SIAGIE, estudiante | **Falta** |
| Matriz de motivaciones | I-4 (responde por temor), reconocimiento institucional (`R14 [02:58]`) | Material listo, **falta la matriz** |
| **Lluvia de ideas, filtro y multivoting, SCAMPER** | — | **Falta. Ver abajo** |
| Prototipado | Web y Android funcionando (asistencia) | En curso |
| Power Pitch / Pitch Final | — | **Falta** |
| **Modelo de negocio** | — | **Falta por completo** |

### Tu ventaja, y cómo no desperdiciarla en la lluvia de ideas

La mayoría de equipos idea primero y busca evidencia después, para justificar lo que ya
decidieron. Tú tienes el orden invertido: **3 h 37 min de campo antes de idear.** Eso hace que la
lluvia de ideas valga mucho más, con una condición: **córrela contra la evidencia, no en el aire.**

Concretamente, en el laboratorio de ideación:

- **Lluvia de ideas** sobre los dolores priorizados de `EVIDENCIA.md` §6, uno por uno. Incluye
  los casos negativos: el docente sin dolor declarado (`R11`) y el perfil de educación física
  (`R12`) obligan a ideas que hoy no están en el alcance.
- **Filtro de ideas y multivoting** es la herramienta que te falta para **justificar el recorte
  de §3 de este plan.** Hoy la decisión de dejar E0b, E4 y E5 fuera está argumentada en prosa;
  el multivoting la convierte en el artefacto que el curso pide. **Es el mismo trabajo, hecho
  una vez.**
- **SCAMPER** aplicado al flujo actual del docente da ideas defendibles, porque parten de algo
  observado: *Eliminar* la triple transcripción (I-7), *Combinar* sesión e instrumento en un solo
  artefacto, *Invertir* el orden —que el sistema planifique y el docente enseñe, que es
  exactamente lo que ellas pidieron (I-9).

### Las dos piezas que no existen en ningún documento

**1. Modelo de negocio.** El Pitch Final lo pide y no hay una línea sobre él en todo el
repositorio. `ALCANCE.md` cubre qué se construye, no cómo se sostiene. Hay que decidir al menos:
quién paga (¿la IE? ¿la UGEL? ¿el docente?), qué pasa con el costo por generación de LLM, y por
qué un colegio público podría adoptarlo. **No se improvisa en S15.**

**2. Factor diferencial, con cinco diferencias.** Para la demo del pitch, el momento más
llamativo ya existe: pulsar «Preparar las sesiones» y ver la faja de la unidad tejerse en vivo,
y después revisar una sesión respondiendo dos preguntas. Tienes una diferencia fuerte y sin escribir: I-1 dice que
el dolor no es la falta de IA, es que la IA genérica resuelve el texto y deja sola a la docente
con el pegado, la corrección, las imágenes, el instrumento y la impresión. **Tu diferencial es el
pipeline integrado y anclado al currículo, no "usamos IA".** Las otras cuatro salen de I-7
(registro único en lugar de triple), I-8 (diferenciación por DUA), E3 (evidencia en el momento,
no de memoria) y la generación por unidad en lote.

### Advertencia sobre la diapositiva del problema

La guía del pitch pide *"Dato de tu problema"* con porcentajes grandes y *"Fuentes"*. Ahí está la
tentación de poner **"los docentes peruanos dedican X horas a Y"**, que es exactamente lo que
`EVIDENCIA.md` §1 prohíbe: la muestra son ~12 docentes de dos instituciones, no una línea base
poblacional.

Lo honesto y además más potente: una cita textual verificada con su `n` declarado. *"Me siento a
las 4 para hacer material y terminó a las 12"* (`R03 [02:26]`) dice más en un pitch que un
porcentaje inventado, y resiste una pregunta del jurado. Un dato sin respaldo no la resiste.

---

## 5. Plan semana por semana

Cada semana cierra con algo que se puede mostrar. Si una semana no cierra, se recorta alcance,
no se arrastra deuda.

### S10 — Currículo cargado y migración de procedencia

- Migración `0500`: `procedencia` (`GENERADA_IA` / `EDITADA_PERSONA` / `ESCRITA_PERSONA`),
  `aprobado_por`, `aprobado_at` en los artefactos generables. Sin esto la Regla 0a de
  `AGENTS.md` no tiene dónde guardarse.
- Cargador de currículo para **un grado y un área**, desde el Programa Curricular oficial, con
  `fuente_url` por fila.
- **Cierre:** consulta que devuelve la competencia con sus capacidades, estándar del ciclo y
  desempeños del grado. Es el contrato de entrada del generador.

> Esta semana es la de mayor riesgo de subestimación. Es transcripción cuidadosa de texto
> normativo, no código. Si se alarga, se recorta a **una sola competencia**.

### S11 — Dominio de planificación, sin IA todavía

- Modelo de dominio puro y servicios para unidad y sesión, según Reglas 1–3 de `AGENTS.md`.
- Endpoints de lectura y de aprobación. Estado `BORRADOR` → aprobación explícita.
- **Cierre:** una unidad y sus sesiones creadas a mano por API, aprobables, con tests pasando.

Hacerlo sin IA primero es deliberado: si el dominio depende del generador, no se puede probar.

### S12 — El generador en lote ✅ (el telar, 2026-10-25)

- Ensamblado de contexto: competencia + capacidades + desempeños + estándar + situación
  significativa de la unidad + **sesiones previas** + **«Mi aula»** del docente.
- Salida **estructurada y validada**: actividades con su `momento` y su **paso didáctico**
  (`paso_proceso_id`), evidencia, DUA y materiales ligados al desempeño. No prosa.
- Se dispara con **«Preparar las sesiones»** en la unidad (todavía no existe aprobar la unidad en
  la app), en segundo plano y nunca durante la revisión (`EPICAS.md` §3). La faja de la unidad se
  teje en pantalla mientras llegan.
- Reglas fijas revisan cada borrador y el generador tiene un segundo intento con sus correcciones.
- **Cierre logrado:** en la base local, las 10 sesiones vacías de la unidad de 4.º B se generaron
  en menos de un minuto, todas sumando la duración de la clase y sin aprobar. Cada generación
  queda registrada con modelo, versión del prompt, intentos y duración.

### S13 — Ficha diferenciada, instrumento e imprimible

- Ficha con ejercicios agrupados por nivel; diferenciada por defecto (regla de UC-02).
- Instrumento con criterios ligados a la competencia, para que E3 pueda valorarlos.
- Exportable imprimible. Solo material aprobado (UC-05).
- **Cierre:** el paquete completo de un día sale en PDF listo para fotocopiar.

### S14 — Revisión por excepción y validaciones (adelantada en parte)

- ✅ Validaciones **determinísticas** como nudos: minutos contra la duración, pasos de la
  institución y su orden, criterios comunicados al inicio, material concreto que pide el desempeño.
- ✅ Revisión que muestra **solo lo señalado**: cada nudo se responde con «Está bien» o
  «Cámbialo», y «Rehacer con tus respuestas» cambia solo lo pedido.
- Pendiente: ¿la sesión cubre una competencia de la unidad? ¿los criterios derivan de una
  capacidad? ¿la duración cabe en el bloque del horario? Y **aprobar en bloque** las sesiones del
  día que ya no tienen nudos.
- **Cierre:** revisar y aprobar el paquete de un día en ≤ 20 min, cronometrado.

### S15 — Captura de evidencia y medición

- E2: registrar evidencia por criterio durante la clase, sobre la asistencia existente.
- E3: consultar evidencia acumulada por competencia.
- **Prueba de usabilidad con ≥ 3 docentes**: tiempo por tarea, número de pasos, SUS.
- **Cierre:** los números de la sustentación, incluida la **tasa de edición por tipo de
  artefacto**, que es la métrica que dice si la IA ayuda o estorba.

### S16 — Sustentación

- Congelar código. Solo correcciones.
- Demo guionada sobre una unidad real y datos sintéticos.
- Actualizar `ALCANCE.md` §8 con los cambios de alcance de este plan.

---

## 6. Lo que corre en paralelo y no es código

Son tuyas, no se pueden programar, y dos bloquean la sustentación.

| Qué | Cuándo | Bloquea |
|---|---|---|
| **Medir la línea base** — registro de tiempos de una semana con 2–3 docentes, separando sesión, ficha, instrumento e impresión | **Empezar ya, en S10** | Los 20 min no tienen contra qué compararse. Sin esto no hay resultado que presentar |
| **Conseguir una unidad de aprendizaje y un registro auxiliar reales** | S10 | Es el contrato de entrada y salida de E1. Ya se pidieron y no se capturaron (`R06 [03:25]`) |
| Confirmar el mapeo de audios a personas | S10 | Citar cualquier cifra agregada de la ronda del 16-10 |
| Verificar el cambio curricular contra fuentes del Minedu | antes de S12 | Nada, pero cambia qué se afirma en la sustentación |
| Autorización y consentimiento si hay piloto con estudiantes | antes de S15 | D-3 de `ALCANCE.md` |

**La medición de la línea base es la tarea más urgente del proyecto y no es técnica.** Si llegas
a S15 sin ella, tienes un producto que funciona y ningún número que demuestre que sirve.

---

## 7. Riesgos y qué hacer

| Riesgo | Señal temprana | Plan B |
|---|---|---|
| La carga del currículo se come S10 y S11 | No está lista al cierre de S10 | Recortar a **una competencia**. Alcanza para la demo |
| La generación produce sesiones que el docente reescribe entera | Tasa de edición alta en S14 | Es un **resultado**, no un fracaso. Se presenta como hallazgo: la IA genérica no basta, y se explica por qué |
| No se consigue una unidad real | S11 sin documento | Reconstruir una a partir de `R16 [18:40]`–`[20:13]` y marcarla explícitamente como reconstrucción |
| El curso de Kotlin consume las semanas | S12 sin avance de backend | Aplicar §4: hacer los temas del curso sobre las pantallas del producto |
| No se logra medir la línea base | S12 sin datos | Usar las estimaciones de `EVIDENCIA.md` §5 y **decir que son autorreporte**, no medición |

El segundo riesgo merece énfasis: **que el pipeline no funcione es un resultado publicable.** El
proyecto es descriptivo y `ALCANCE.md` §6 dice que no se fija de antemano un porcentaje de
mejora. Un hallazgo negativo bien medido vale más que una demo que nadie cronometró.

---

## 8. Definición de terminado, para la Semana 16

- [ ] Currículo cargado para al menos una competencia, con `fuente_url` por fila
- [ ] Una unidad aprobada genera sus sesiones en lote, en estado no aprobado
- [ ] Ficha diferenciada por niveles e instrumento con criterios ligados a la competencia
- [ ] Paquete de un día exportable a imprimible, solo si está aprobado
- [ ] Revisión por excepción cronometrada en ≤ 20 min, con ≥ 3 docentes
- [ ] Evidencia por criterio capturada en clase y consultable por competencia
- [ ] `procedencia` y aprobación persistidas en todo artefacto generado
- [ ] `./gradlew test` en verde y lint de web limpio (Regla 8 de `AGENTS.md`)
- [ ] Línea base medida, o declarada explícitamente como autorreporte
- [ ] `ALCANCE.md` §8 actualizado con los recortes de este plan

---

## 9. Historial

| Fecha | Cambio |
|---|---|
| 2026-10-17 | Documento creado. E0 dividida en E0a (currículo, obligatoria) y E0b (banco de ítems, fuera). E4, E5 y la replanificación automática salen del alcance de la sustentación. Plan S10–S16 fijado |
