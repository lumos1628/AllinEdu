# Evidencia de investigación — AllinEdu

> Base empírica del proyecto. Todo lo que este repositorio afirma sobre el problema docente
> debe poder rastrearse hasta aquí. Si una decisión de producto no tiene respaldo en este
> documento, es una hipótesis del equipo y debe decirse así.

---

## 1. Advertencia metodológica (leer antes de citar cualquier cifra)

| Fuente | Tamaño | Qué permite afirmar |
|---|---|---|
| Entrevistas en profundidad | **1 docente** de primaria pública (D01) | Describir un caso con detalle. **No** generalizar. |
| Ronda de entrevistas del 16-10-2026 | hasta **12 docentes de aula**, 1 cargo directivo, 1 docente de educación física — al menos 2 instituciones | Convergencia de un dolor, con el grueso en **una** institución. **No** una muestra nacional. |
| Encuesta A | 8 respuestas | Tendencia exploratoria. |
| Encuesta B (primaria pública) | 4 respuestas | Tendencia exploratoria. |

**Esto sigue sin ser una línea base poblacional.** Los tiempos de la sección 3 provienen de una
sola docente. Sirven para dimensionar el problema y diseñar el prototipo; no sirven para
afirmar que "los docentes peruanos dedican X horas a Y".

Advertencias específicas de la ronda del 16-10-2026, detalladas en
`investigacion/HALLAZGOS-2026-10-16.md`:

- **El `n` exacto está pendiente de confirmar.** Son 17 audios, no 17 personas: uno no tiene
  contenido y tres son sesiones de asesoría, no entrevistas a sujetos. Hasta que el
  entrevistador confirme el mapeo, no se cita ninguna cifra agregada de esta ronda.
- **El bloque de la mañana (`R01`–`R12`) parece ser una sola institución**, un colegio público
  de primaria de mujeres: el alumnado se menciona siempre en femenino y se repiten la directora,
  la auxiliar que pasa por las aulas y el formato de unidad de la UGEL. La convergencia entre
  esos docentes es fuerte dentro de ese centro y, por sí sola, no dice nada sobre otros.
- **`R13` y `R14` parecen ser instituciones distintas**, lo cual amplía el alcance: `R13` habla
  de alumnado mixto con 24–25 estudiantes, y en `R14` se tratan casos de **secundaria** e
  **inicial**, niveles que una primaria de mujeres no tiene. Confirmar con el entrevistador.
- **Parte de la ronda está contaminada por preguntas que anuncian la conclusión.** Lo que se
  obtuvo así no se cuenta como hallazgo. Ver §10.

Reglas al escribir documentos del proyecto:

- Toda cifra va acompañada de su fuente y su `n`.
- Las citas se anonimizan (`Docente 1`, `Apoderado 1`). Nunca nombres propios.
- Las transcripciones automáticas se verifican contra el audio antes de citarse textualmente.
- Lo que el equipo infiere se marca como inferencia, no como hallazgo.

---

## 2. Fuentes

- Entrevistas grabadas y transcritas (NotebookLM) a docente de primaria pública (D01).
- **Ronda del 16-10-2026**: 17 audios, 3 h 37 min. El grueso en un colegio público de primaria
  de mujeres, más al menos una institución distinta con secundaria e inicial (§1).
  Transcritas con faster-whisper `large-v3` en local (`investigacion/transcribir.py`);
  transcripciones automáticas sin verificar en `investigacion/crudo/`, no versionado. Síntesis
  en `investigacion/HALLAZGOS-2026-10-16.md`.
- Dos encuestas en línea a docentes (8 y 4 respuestas), incluyendo primaria pública en
  Arequipa y Cajamarca. Respuestas en las hojas de cálculo del proyecto.
- Cubierto en la ronda del 16-10-2026: una entrevista a un cargo directivo (directora
  encargada). Sigue pendiente la perspectiva de coordinación pedagógica de otra institución.

---

## 3. Línea base de tiempos (Docente 1)

Solo tareas con dato temporal explícito declarado por la docente.

### Trabajo en casa, no remunerado

| Tarea | Tiempo | Unidad |
|---|---|---|
| Redacción de sesión de aprendizaje con IA | 30–40 min | Por sesión (×2 al día) |
| Diseño de ficha de trabajo diferenciada por niveles | 40–60 min | Por sesión (×2 al día) |
| **Planificación diaria completa** (2 sesiones + 2 fichas + impresión) | **2.5–4 h** | **Por día** |
| Evaluación de criterios por competencia sobre fichas físicas | 10–15 min | Por día |
| Volcado de la asistencia al registro auxiliar | 5 min | Por día |

**Agregado: ≈14 a 22 horas semanales de trabajo no remunerado en casa.**

Horario declarado: se acuesta entre las **2:00 y 3:30 a.m.** y se levanta a las **5:30 a.m.**
Entre 2 y 3.5 horas de sueño en días de preparación.

> Nota de consistencia: la docente declara 1.5–2 h por "sesión completa" y a la vez "son dos
> sesiones, son 4 horas". El rango 2.5–4 h recoge ambas versiones. Conviene precisarlo en una
> siguiente entrevista.

### Tiempo lectivo perdido

| Causa | Tiempo | Unidad |
|---|---|---|
| Charlas y actividades imprevistas (UGEL / Minedu) | 2 h | Por evento |
| Fallas de proyector y PC del aula | 1 h | Por eventualidad |
| Sesión de aprendizaje: declara que **no alcanza** | 90 min | Por sesión |

### Jornada en la institución

| Tarea | Tiempo | Unidad |
|---|---|---|
| Registro de asistencia en el aula (cartel físico + sellado de agendas) | 5 min | Por día |
| Jornada lectiva | 4.5–5 h | Por día |
| Trabajo colegiado posterior a clase | 30 min | Por día |
| Espera de estudiantes no recogidos | hasta 1:15–1:20 p.m. | Por día |

> Nota de consistencia: una fuente declara llegada a las 7:40 a.m. y otra inicio de jornada a
> las 8:00 a.m. Verificar.

### Comunicación y refuerzo

| Tarea | Dato | Unidad |
|---|---|---|
| Atención a llamadas y mensajes de apoderados | **8:00 a.m. – 11:00 p.m.**, incluidos domingos | Por día |
| Grabación y envío de videos explicativos por WhatsApp | 3–5 min por pieza | Por eventualidad |

### Contexto curricular y de materiales

| Dato | Valor |
|---|---|
| Estudiantes en el aula | 26 |
| Duración de la unidad de fracciones | 2 semanas |
| Evaluaciones integradoras acumulativas | Cada trimestre |
| Cuadernos de trabajo del Estado | 80 páginas, reutilizados **2–3 años** |

---

## 4. Insights (lo que explica el comportamiento, no solo lo que cuesta)

### I-1 — La docente ya usa IA; el cuello de botella es la integración

Flujo declarado: revisar horario, currículo y unidad → ejecutar prompt en Claude → corregir
errores → extraer mapa conceptual de Gemini o ChatGPT → elaborar el instrumento de evaluación →
buscar imágenes → imprimir.

**El dolor no es la falta de IA; es que la IA genérica resuelve el texto y la deja sola con el
pegado, la corrección, las imágenes, el instrumento y la impresión.** Cuatro herramientas y
hasta cuatro horas para producir dos sesiones y dos fichas.

Implicación: la propuesta de valor no es "darle IA", es darle el pipeline completo y conectado.

### I-2 — La asistencia se registra dos veces, con una fotografía como puente

Marca el cartel físico del aula → le toma una foto con el celular porque no puede transcribir
en clase → en casa vuelca la foto al registro auxiliar de papel.

Implicación: el problema no son los 5 minutos del aula; es que el dato existe dos veces y
ninguna de las dos es digital.

### I-3 — Evalúa de memoria

Revisa las fichas físicas en la noche y evoca quién participó y quién expuso: *"hago un
recuerdo"*.

Implicación: es simultáneamente un problema de carga y de calidad de la evidencia. Es el
argumento más fuerte para capturar la evidencia en el momento de la clase.

### I-4 — Responde a las 11 de la noche por temor, no por disponibilidad

El motivo declarado para contestar fuera de horario es evitar reportes negativos de desempeño.

Implicación: un canal más rápido no resuelve nada. Lo que necesita es un canal que **deje
constancia de que respondió**, con horario de atención visible. El valor es la trazabilidad que
la protege, no la mensajería.

### I-5 — Ya diferencia por niveles de dificultad, a mano

Adapta cada ficha de trabajo filtrando ejercicios por nivel.

Implicación: la detección de nivel y la práctica progresiva no son un módulo aparte de
"calidad educativa". Son la misma función que ya realiza manualmente, y sirve a los dos ODS.

### I-6 — Cuando no se llevó el registro de evaluación, la nota se fabrica

Ronda 16-10-2026. Describiendo el llenado de criterios de evaluación por sesión: *"ese es el
trabajo que demoran en hacer, que normalmente no lo hacen"*, y al momento de calificar, *"se
pueden inventar si es que no lo han hecho… al ojo les ponen la nota"* (`R16 [06:01]`). Desde el
cargo directivo, la contraparte: *"los profesores no nos preparamos, honestamente"* (`R14 [10:04]`).

Esto es más fuerte que I-3. En I-3, D01 evaluaba de memoria; aquí se afirma que a veces el dato
**no existe** y se construye al cierre del bimestre.

Implicación: es el argumento más fuerte a favor de capturar la evidencia durante la clase. Sin
captura en el momento, la trazabilidad por competencia es nominal.

> **Manejo de este hallazgo.** Es material profesionalmente sensible. Se encuadra como falla del
> sistema de registro —el mismo dato se transcribe tres veces a mano, ver I-7— y no como
> negligencia individual. No se usa en presentaciones públicas ni frente a la institución.

### I-7 — El mismo dato se transcribe tres veces a mano

Ronda 16-10-2026. La cadena declarada es: criterios de la **sesión** → **registro auxiliar** →
**SIAGIE**, *"uno por uno"*. *"Es una vaina, pues como doble triple trabajo de lo mismo"*
(`R16 [07:36]`).

Generaliza I-2, que había detectado el doble registro solo en asistencia. El patrón es el mismo:
el dato existe varias veces y ninguna copia intermedia es digital.

### I-8 — El DUA convierte una sesión en varias

Ronda 16-10-2026. El Diseño Universal de Aprendizaje exige que la sesión responda a cada
estudiante. Con 25 estudiantes y sin grupos homogéneos, *"en una sesión hacer como 8 sesiones"*
(`R14 [12:17]`).

Esto le da nombre normativo a lo que I-5 observaba como práctica manual de D01, y explica por
qué los dolores 1 y 3 de §6 son el mismo problema: una exigencia regulatoria de diferenciación
sin herramienta que la soporte.

### I-9 — Los propios docentes formulan la propuesta de valor

Ronda 16-10-2026, sin que se les ofreciera la idea: separar **planificar / enseñar / evaluar**, y
que el sistema se encargue de planificar para que el docente solo enseñe. *"Ese es el sueño"*
(`R16 [22:21]`, `R17 [02:19]`). Un cargo directivo recuerda que el Estado antes entregaba las
programaciones ya elaboradas (`R14 [11:27]`).

Implicación: no es solo validación de la hipótesis, es la hipótesis enunciada por el usuario.
Conviene citarla literalmente, y conviene notar que fija la expectativa en "planificación
entregada", no en "asistente que ayuda a planificar".

---

## 5. Contradicción entre fuentes — resuelta el 16-10-2026

| Fuente | Tiempo de planificación |
|---|---|
| Encuestas (n=12) | 2–5 horas **por semana** |
| Entrevista D01 (n=1) | 2.5–4 horas **por día** |

La brecha era de ~5×. La ronda del 16-10-2026 la resuelve del lado de la entrevista: **cuatro
fuentes independientes describen carga diaria de varias horas**, y una es más extrema que D01.

| Fuente | Declaración | Verificar en |
|---|---|---|
| Docente, fichas diferenciadas | "dos horas" al día, en las tardes. "Demasiado" | `R02 [03:22]` |
| Docente, programación y material | "Me siento a las 4 para hacer material y terminó a las 12" (8 h) | `R03 [02:26]` |
| Docente, sesiones y fichas | "Casi siempre terminamos a las 10, 11 de la noche… es horrible" | `R04 [01:19]` |
| Directora encargada (observa al colectivo) | Salen 1 p.m.; desde las 6 p.m. preparan **tres temas** más materiales, a diario | `R14 [10:46]` |

**Conclusión:** la explicación 1 queda respaldada y la 2 descartada. D01 no era un caso extremo
aislado. Las encuestas subdeclaran, con la mejor explicación disponible siendo que "planificar"
en una encuesta no incluye fichas diferenciadas, instrumentos de evaluación, material concreto
ni impresión, que es donde se va la mayor parte del tiempo.

A favor de la conclusión: las cuatro fuentes no están todas en el mismo centro. `R02` y `R04`
son del colegio de primaria de mujeres; `R14` describe una institución con secundaria e inicial.
El patrón cruza al menos dos instituciones, pendiente de confirmar (§1).

**Lo que esto todavía no autoriza.** Las cuatro son estimaciones de autorreporte, dichas al
paso ("será pues dos horas"). Sirven para afirmar que el problema es diario y de varias horas.
No sirven como línea base cuantitativa. Para eso hace falta medir: registro de tiempos de una
semana con dos o tres docentes. **Seguir entrevistando no mejora este dato.**

---

## 6. Dolores priorizados por impacto

Orden por tiempo recuperable, no por intensidad declarada.

| # | Dolor | Costo | ¿Lo reduce software? |
|---|---|---|---|
| 1 | Preparar sesiones, fichas diferenciadas e instrumentos | 2.5–4 h/día | **Sí** — pipeline integrado |
| 2 | Atención a apoderados sin límite de horario | 8 a.m.–11 p.m. | Sí — canal con constancia y horario |
| 3 | Evaluar criterios por competencia de memoria, en casa | 10–15 min/día | Sí — captura en clase |
| 4 | Doble registro de asistencia vía fotografía | 10 min/día | **Ya construido** en parte |
| 5 | Alumnos olvidan los temas | — | Sí — repaso espaciado |
| 6 | Alumnos no llegan con el nivel del grado | — | Solo **detectar** la brecha |
| 7 | Material insuficiente para concursos | — | Parcial — banco de ítems |
| 8 | Registro de notas manual | 20–40 min/actividad (encuestas) | Depende de SIAGIE |
| 9 | Conclusiones descriptivas | 2–10 h/bimestre (encuestas) | Sí, si existe evidencia por competencia |

**Corroboración del dolor nº1 (16-10-2026).** Salió de una pregunta abierta —*"¿cuál es el
proceso que más se le dificulta?"*— en al menos tres entrevistas independientes, con tres
formulaciones distintas del mismo problema: fichas diferenciadas (`R02`), programación y
unidades (`R03`), sesiones y fichas (`R04`). Una cuarta lo ubica en la unidad, no en la sesión:
*"en la unidad es donde nos demoramos más"* (`R01 [06:45]`). Es el hallazgo metodológicamente
más limpio del proyecto, porque nadie sugirió la respuesta.

Matiz que hay que conservar: la unidad de aprendizaje, no la sesión, es donde varios docentes
ubican el cuello de botella, y se elabora **de forma colegiada** fuera de horario (`R01 [05:13]`,
`R06 [01:03]`: una semana de trabajo conjunto, una vez al mes). Un producto que solo genere
sesiones individuales ataca la mitad visible del problema.

**Casos negativos — registrar también a quien no tiene este dolor.**

| Fuente | Qué declara |
|---|---|
| `R11 [02:25]` | Ningún proceso le molesta, ni pedagógico ni administrativo. Docente desde 2018 |
| `R12` | Docente de educación física: su dolor son las actividades de UGEL y municipio avisadas con días de anticipación, ~2 por mes. No la planificación |
| `R07 [00:00]` | Ubica el problema en la falta de formación didáctica desde Minedu/PerúEduca, no en la carga |

El dolor nº1 es dominante, no universal. El perfil de educación física y los docentes sin carga
declarada son el límite natural del alcance.

---

## 7. Lo que software NO resuelve (decirlo explícitamente)

- **Calidad del material del Estado.** Cuadernos de 80 páginas reutilizados 2–3 años es un
  problema de dotación, no de aplicación.
- **Pérdida de tiempo lectivo por charlas imprevistas de UGEL/Minedu** (2 h por evento) y por
  **fallas de equipos** (1 h por eventualidad). Son organizacionales y de infraestructura.
- **Cerrar la brecha de nivel.** El sistema puede medirla y organizar el refuerzo; cerrarla
  depende del docente y del tiempo de clase disponible.
- **Que la jornada de 90 minutos alcance.** Es una restricción curricular.
- **Crear horas de planificación dentro de la jornada.** Es la condición estructural del
  problema (ver §11) y es una decisión normativa. El sistema no crea horas: reduce el trabajo
  que debe caber en las que no existen.

---

## 8. Vacíos — qué falta (actualizado 16-10-2026)

### Prioridad alta

1. ~~**¿Cambia el currículo nacional a un enfoque por contenidos?**~~ **VERIFICADO Y DESCARTADO
   (18-10-2026).** La **Norma Técnica para el Año Escolar 2026** (RM N.º 501-2025-MINEDU, del
   7-11-2025) organiza el año escolar pero **no reemplaza el CNEB ni implanta un enfoque por
   contenidos**. El marco vigente sigue siendo por competencias, con estándares, desempeños y
   evidencias. El rumor de `R14 [18:23]` queda registrado como lo que era: una expectativa de una
   sola fuente, sin respaldo documental. **El riesgo sobre la arquitectura queda cerrado.**
2. **Medir, no preguntar, el tiempo de planificación.** Registro de una semana con dos o tres
   docentes. Otra ronda de entrevistas no mejora este dato (§5).
3. ¿Se puede exportar al formato que SIAGIE importa? Pedir que muestren el flujo real. De esta
   respuesta depende si el registro de notas entra al alcance. **Sigue abierto**, aunque
   `R12 [08:14]` ya describe la cadena de tres registros (borrador, auxiliar, oficial).
4. **Replicar fuera del colegio del bloque de la mañana.** Doce de las diecisiete grabaciones
   del 16-10-2026 vienen de un solo centro. La convergencia entre ellas puede ser cultura
   institucional y no un patrón del sector. `R13` y `R14` ayudan, pero son una entrevista cada
   una. Confirmar primero cuántas instituciones hay realmente en la ronda (§1).

### Prioridad media

5. ¿Cómo se elabora la **unidad** de forma colegiada, paso a paso? Es donde varios docentes
   ubican el cuello de botella y es el proceso que menos se ha documentado.
6. ¿Qué pasa cuando la docente no responde fuera de horario? Confirmar el mecanismo real de
   reportes de desempeño que sustenta I-4. **No se tocó el 16-10-2026.**
7. ¿Quién puede autorizar un piloto con estudiantes y bajo qué consentimiento?
8. Pedir una **carpeta pedagógica y un registro auxiliar físicos** para ver el formato real.
   Solicitado en `R06 [03:25]` y parcialmente mostrado en `R15`/`R16`, sin capturar.

### Resueltos

- ~~Contradicción de 5× en el tiempo de planificación~~ → §5.
- ~~Perspectiva de dirección~~ → cubierta parcialmente por `R14`, un cargo directivo de una
  institución con secundaria e inicial, distinta del colegio del bloque de la mañana.

---

## 9. Respaldo bibliográfico

Sostiene el componente de práctica y repaso, no los resultados del prototipo.

- Agarwal, P. K., Nunes, L. D., & Blunt, J. R. (2021). Retrieval practice consistently benefits
  student learning. *Educational Psychology Review, 33*, 1409–1453.
- Latimier, A., Peyre, H., & Ramus, F. (2021). A meta-analytic review of the benefit of spacing
  out retrieval practice episodes on retention. *Educational Psychology Review, 33*, 959–987.
- Brooke, J. (1996). SUS: A quick and dirty usability scale. — instrumento de usabilidad.
- Roy, P., et al. (2024). *ChatGPT in lesson preparation: A Teacher Choices trial.* NFER.
- Selwyn, N., Ljungqvist, M., & Sonesson, A. (2025). When the prompting stops. *Learning, Media
  and Technology, 50*(3), 310–323. — documenta el trabajo adicional de revisar salidas de IA.
- Ministerio de Educación del Perú (2016). *Currículo Nacional de la Educación Básica.*
- Ministerio de Educación del Perú (2012). *Marco de Buen Desempeño Docente.*

> Ninguna de estas fuentes acredita un resultado de AllinEdu. Respaldan que las estrategias
> elegidas son razonables de probar.

---

## 10. Nota de método sobre la ronda del 16-10-2026

Varias entrevistas de esta ronda abren enunciando la conclusión y pidiendo confirmación:
*"se ha concluido que el principal problema es el desinterés de los padres… ¿es verdad eso?
Usted puede confirmar"* (`R09 [00:17]`; también `R08 [00:00]`, `R11 [00:09]`, `R12 [03:17]`).

Eso induce la respuesta, y tiene dos consecuencias que este documento debe respetar:

**El desinterés de los padres no se registra como hallazgo de esta ronda.** Apareció porque se
preguntó por él en esos términos. Puede ser un problema real —hay un dato propio y verificable:
de unos 50 apoderados esperados en la escuela de padres, *"a la hora que deben estar, exagerados
serán unos 20"* (`R12 [19:41]`)— pero el número de docentes que "lo confirman" no vale como
evidencia, y no se usará para priorizar.

**El dolor nº1 sí es limpio**, porque salió de una pregunta abierta. La diferencia entre ambos
casos es la mejor ilustración de por qué el orden de las preguntas importa.

Dos docentes resistieron la premisa, lo que confirma que el sesgo era visible en el momento:
`R08 [00:34]` responde *"eso es falso"* sobre la orientación de las sesiones a concursos, y
`R11 [02:25]` no encuentra ninguna queja.

**Regla para la próxima ronda:** preguntar antes de contar. Abrir con la pregunta abierta, dejar
que la persona nombre su propio dolor, y solo al final —si hace falta— confrontar con lo que
dijeron otros. Lo que se obtenga en esa fase final se anota como reacción, no como hallazgo.

### La ronda cayó en una semana de gestión

Según la Tabla 1 de la Norma Técnica para el Año Escolar 2026 (RM N.º 501-2025-MINEDU), del
**12 al 16 de octubre de 2026** corrió el cuarto bloque de **semanas de gestión**: el tercer
bloque lectivo cerró el 9 de octubre y el cuarto empezó el 19.

Las 17 grabaciones de esta ronda se hicieron el **16 de octubre**, o sea el último día de esa
semana de gestión. Las docentes **no estaban dictando clase**. Eso explica la disponibilidad
para entrevistas relámpago y probablemente también por qué el cierre de periodo y el papeleo
aparecen tan presentes: era justo lo que estaban haciendo esa semana.

**Consecuencia para este documento:** las respuestas de la ronda del 16-10 describen bien la
carga de una semana de gestión, no la de una semana lectiva típica. Los tiempos de la tarde
que sostienen el dolor nº1 vienen de las rondas del 20-09 y del 25/26-09, que sí cayeron en
bloque lectivo. Antes de citar cifras agregadas de las 26 grabaciones hay que separar por
tipo de semana.

### Datos personales en el material

El audio y las transcripciones automáticas de la ronda contienen un nombre y un teléfono
dictado (`R05 [05:49]`), nombres de pila de docentes (`R14`, `R15`, `R16`) y nombres de
instituciones (`R13`). Viven en `investigacion/crudo/`, que no se versiona. Nada de eso pasa a
`investigacion/transcripciones/` ni a este documento: la regla sigue siendo `D<n>` y perfil.

---

## 11. La condición estructural: la jornada no reparte las horas

Verificado leyendo los PDF oficiales. **Corregido el 19-10-2026**: la versión anterior de
esta sección decía 30 horas de jornada y «no sobra nada». Era falso, por dos errores de
cita que se arrastraron desde una búsqueda de terceros. Lo que sigue está verificado
contra los archivos de `investigacion/normativa/FUENTES.md`.

| Dato | Valor | Fuente |
|---|---|---|
| Clase de los estudiantes de primaria | **30 horas pedagógicas** por semana, todos los grados | CNEB, Tabla N.º 6, p. 165 |
| Hora pedagógica | **45 minutos** | CNEB, p. 165 |
| Jornada del **profesor con aula a cargo**, primaria | **35 horas pedagógicas**, y «puede ser lectiva o no lectiva» | D.S. N.º 004-2013-ED, art. 139, **modificado por el D.S. N.º 005-2017-MINEDU** |
| Qué comprende esa remuneración | docencia en aula, **preparación de clases y evaluación**, actividades extracurriculares, trabajo con familias y apoyo a la institución | Ley N.º 29944, art. 65 |
| ¿Alguna norma reparte las 35 horas entre lectivas y no lectivas? | **No.** El reglamento ni define los términos | verificado en sus 134 pp. |
| Cuando la norma sí quiere reservar tiempo, lo hace | personal jerárquico: 40 horas con **12 de dictado obligatorio** | D.S. N.º 004-2013-ED, art. 141 |

### Dos errores corregidos

1. **El reglamento es el D.S. N.º 004-2013-ED**, no el 004-2014-ED. El número equivocado
   venía de una búsqueda de terceros y se citó sin abrir el archivo.
2. **El artículo 139 fue modificado** por el D.S. N.º 005-2017-MINEDU, que subió la jornada
   de primaria a **35 horas** y agregó que «puede ser lectiva o no lectiva». No hay
   modificación posterior.

### La aritmética correcta

La jornada son 35 horas pedagógicas y el plan de estudios asigna 30 horas de clase a los
estudiantes. Cuánto le queda a la docente para preparar y evaluar depende de si la IE tiene
docentes especializados:

| Escenario | Dicta | No lectivas | Sesiones semanales | **Minutos por sesión** |
|---|---|---|---|---|
| Con especialista de Inglés y Educación Física | 22 h | 13 h | 11 | **53 min** |
| Sin especialistas, la tutora dicta todo | 28 h | 7 h | 14 | **22 min** |

Contra los **90 a 120 minutos por sesión** que midió el trabajo de campo (§5). El hueco es
de **dos a cinco veces**, en los dos escenarios.

Estas cifras no están afirmadas en prosa: las calcula la vista `v_carga_no_programada`
de la base de datos a partir de la tabla `jornadas_normativas`, que guarda las horas con
su norma y su artículo.

### Por qué importa para el planteamiento

Decir «carga de trabajo **no remunerada**» es rebatible en una línea con el artículo 65.
Decir «no hay tiempo asignado» también era rebatible, porque sí hay margen. Lo que no se
puede rebatir es que **el margen es de dos a cinco veces menor que el trabajo**. El PC1 y
el laboratorio 04 quedaron corregidos en ese sentido el 19-10-2026.

### Lo que decide cuál escenario aplica — **PENDIENTE**

**¿La IE tiene profesor de Inglés? ¿De Educación Física?** Si los tiene son 53 minutos;
si no, 22. Es la pregunta de mayor rendimiento que queda, y está modelada en la semilla
como dos banderas de configuración (`tiene_docente_ingles`, `tiene_docente_ed_fisica`).

### Corrección sobre «material concreto»

La cifra de **7 ocurrencias** que se venía citando sale de la maqueta de 212 pp. del día de
aprobación del Programa Curricular. En la **edición publicada de 396 pp.**, que es la que
MINEDU sirve hoy, son **28 ocurrencias repartidas en 13 páginas**. La conclusión no cambia
—la exigencia es por desempeño y no general— pero la cifra correcta es 28 en 13 páginas.

### Las 8 semanas de gestión

La Norma Técnica 2026 asigna **36 semanas lectivas y 8 de gestión** (mínimo 160 días
lectivos y 30 de gestión). Las semanas de gestión se definen como «período destinado al
trabajo colegiado o individual […] para garantizar acciones de planificación, seguimiento
y evaluación» y las regula la **RM N.º 239-2025-MINEDU**, cuyo articulado completo aún no
se pudo descargar.

Es el único tiempo explícito para planificar, y es institucional: sirve para el trabajo
colegiado de toda la IE, no para que cada docente prepare sus ~500 sesiones del año.
