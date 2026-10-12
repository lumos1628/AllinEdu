# Evidencia de investigación — AllinEdu

> Base empírica del proyecto. Todo lo que este repositorio afirma sobre el problema docente
> debe poder rastrearse hasta aquí. Si una decisión de producto no tiene respaldo en este
> documento, es una hipótesis del equipo y debe decirse así.

---

## 1. Advertencia metodológica (leer antes de citar cualquier cifra)

| Fuente | Tamaño | Qué permite afirmar |
|---|---|---|
| Entrevistas en profundidad | **1 docente** de primaria pública | Describir un caso con detalle. **No** generalizar. |
| Encuesta A | 8 respuestas | Tendencia exploratoria. |
| Encuesta B (primaria pública) | 4 respuestas | Tendencia exploratoria. |

**Esto es un caso, no una línea base poblacional.** Los tiempos de la sección 3 provienen de
una sola docente. Sirven para dimensionar el problema y diseñar el prototipo; no sirven para
afirmar que "los docentes peruanos dedican X horas a Y".

Reglas al escribir documentos del proyecto:

- Toda cifra va acompañada de su fuente y su `n`.
- Las citas se anonimizan (`Docente 1`, `Apoderado 1`). Nunca nombres propios.
- Las transcripciones automáticas se verifican contra el audio antes de citarse textualmente.
- Lo que el equipo infiere se marca como inferencia, no como hallazgo.

---

## 2. Fuentes

- Entrevistas grabadas y transcritas (NotebookLM) a docente de primaria pública.
- Dos encuestas en línea a docentes (8 y 4 respuestas), incluyendo primaria pública en
  Arequipa y Cajamarca. Respuestas en las hojas de cálculo del proyecto.
- Pendiente (semana 9): entrevistas a responsables de coordinación/dirección.

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

---

## 5. Contradicción abierta entre fuentes

| Fuente | Tiempo de planificación |
|---|---|
| Encuestas (n=12) | 2–5 horas **por semana** |
| Entrevista (n=1) | 2.5–4 horas **por día** |

Diferencia de aproximadamente 5×. Explicaciones posibles, aún sin resolver:

1. Las encuestas midieron solo "planificar"; la entrevista incluye fichas, instrumentos,
   búsqueda de imágenes e impresión.
2. La docente entrevistada es un caso extremo.
3. Las encuestas subdeclaran por efecto de autorreporte.

**No usar ambas cifras en el mismo documento sin explicar esta brecha.** Resolverlo es una
pregunta explícita para la semana 9.

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

---

## 7. Lo que software NO resuelve (decirlo explícitamente)

- **Calidad del material del Estado.** Cuadernos de 80 páginas reutilizados 2–3 años es un
  problema de dotación, no de aplicación.
- **Pérdida de tiempo lectivo por charlas imprevistas de UGEL/Minedu** (2 h por evento) y por
  **fallas de equipos** (1 h por eventualidad). Son organizacionales y de infraestructura.
- **Cerrar la brecha de nivel.** El sistema puede medirla y organizar el refuerzo; cerrarla
  depende del docente y del tiempo de clase disponible.
- **Que la jornada de 90 minutos alcance.** Es una restricción curricular.

---

## 8. Vacíos — qué falta preguntar (semana 9)

1. Resolver la contradicción de 5× en el tiempo de planificación.
2. ¿Se puede exportar al formato que SIAGIE importa? Pedir que muestren el flujo real.
   De esta respuesta depende si el registro de notas entra al alcance.
3. ¿Cómo asignan, comunican y revisan actividades los responsables de coordinación/dirección?
4. ¿Qué pasa cuando la docente no responde fuera de horario? Confirmar el mecanismo real de
   reportes de desempeño que sustenta I-4.
5. Validar los tiempos con al menos dos docentes más antes de usarlos como línea base.
6. ¿Quién puede autorizar un piloto con estudiantes y bajo qué consentimiento?

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
