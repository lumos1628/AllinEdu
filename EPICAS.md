# Épicas e historias de usuario — AllinEdu

> Capa intermedia entre [`ALCANCE.md`](ALCANCE.md) (qué entra y por qué) y
> [`USE_CASES.md`](USE_CASES.md) (cómo funciona cada interacción). Una épica agrupa casos de uso
> que entregan **un resultado medible para el docente**, no un módulo técnico.
>
> Regla: toda historia declara el dolor que ataca, su respaldo en [`EVIDENCIA.md`](EVIDENCIA.md)
> y cómo se mide que se cumplió. Una historia sin medida es un deseo.

---

## 1. Las épicas

Las épicas corresponden a los grupos ya definidos en `USE_CASES.md`. El orden es el de
`ALCANCE.md` §5: horas recuperadas por hora de desarrollo.

| # | Épica | Casos de uso | Resultado para el docente | Estado |
|---|---|---|---|---|
| **E0a** | **Currículo normativo cargado** | habilitador | Sin esto la IA inventa competencias. Bloquea E1 y E3 | **Obligatorio**, ver [`PLAN.md`](PLAN.md) |
| **E0b** | **Banco de ítems de práctica** | habilitador | Bloquea solo E4 | **Fuera del alcance de la sustentación** |
| **E1** | **Planificar sin trabajar de noche** | UC-01 … UC-05 | Llega a clase con el material listo sin haber pasado la noche haciéndolo | Por construir |
| **E1b** | **Replanificar cuando el día se rompe** | nuevo | El plan sobrevive a interrupciones sin rehacerlo a mano | Por construir |
| **E2** | **Aplicar en clase sin doble registro** | UC-06 … UC-09 | El dato se captura una vez, en el aula | Asistencia **ya existe** |
| **E3** | **Evaluar con evidencia, no de memoria** | UC-10, UC-11 | Puede sustentar cada valoración por competencia | Por construir |
| **E4** | **Detectar la brecha y sostener el aprendizaje** | UC-12 … UC-15 | Sabe quién necesita refuerzo y en qué | Por construir |
| **E5** | **Responder a familias con constancia** | UC-16 … UC-21 | Queda registro de que respondió, con horario visible | Por construir |

**Corrección (2026-10-17): E0 estaba mal planteada y se dividió.** La versión inicial juntaba
el currículo normativo con el banco de ítems de práctica y afirmaba que ambos bloqueaban E1, E3
y E4. Son cosas distintas:

- **E0a — currículo normativo.** Cargar `competencias`, `capacidades`, `estandares_aprendizaje`,
  `desempenos_grado`, `areas_curriculares` y `planes_estudio`. Hoy esas tablas están **vacías**:
  `supabase/seed.sql` no inserta una sola fila en ninguna. Sin esto el generador inventa
  competencias, que son texto normativo. **Bloquea E1 y E3, y es obligatorio.**
- **E0b — banco de ítems de práctica.** Ítems por nivel de dificultad, intentos y repaso
  espaciado. `ALCANCE.md` §5 tiene razón en que es el cuello de botella real y que no es un
  problema de código. Pero **bloquea solo E4**, no E1. Para el piloto serían una competencia, un
  tema y 40–60 ítems revisados uno por uno, que no caben en el tiempo disponible.

Ninguna de las dos es épica de código. Llevan número para que aparezcan en la planificación y no
se descubra tarde que hay épicas dependiendo de contenido que nadie produjo.

**E1b es nueva.** No estaba en `USE_CASES.md` y la evidencia la respalda. Ver §4.

---

## 2. La historia principal de E1

### Versión propuesta

> **Como** docente de aula de primaria,
> **quiero** revisar y aprobar en **20 minutos o menos** todo el material que aplicaré mañana
> —sesiones, fichas diferenciadas e instrumentos, listos para imprimir—
> **para** dejar de trabajar después de las 10 de la noche.

Esta es la historia que sostiene el proyecto. Si se cumple, el dolor nº1 de `EVIDENCIA.md` §6
queda atacado; si no, el resto del producto no compensa.

### Qué le corregí a la formulación original y por qué

La versión inicial era *"no demorarme más de 20 minutos en revisar mis sesiones de aprendizaje
del día siguiente"*. El verbo es el acierto —**revisar**, no crear— y por eso la conservo. Tres
arreglos:

**a) "Mis sesiones" es ambiguo y la ambigüedad cuesta horas.** Son 2 sesiones por día
(`R01 [08:17]`, `R04 [01:13]`) o 3 temas (`R14 [11:07]`). ¿20 minutos por sesión o por el día?
Si es por sesión, el total es una hora y la historia no resuelve el problema. Queda fijado:
**20 minutos para el paquete completo del día siguiente.**

**b) Revisar solo la sesión no mueve la aguja.** La línea base de 2.5 a 8 horas no es redactar
la sesión: es sesión **más** ficha diferenciada **más** instrumento **más** búsqueda de imágenes
**más** impresión (I-1). Quien declara 8 horas lo dice explícito: *"me siento a las 4 para hacer
material y terminó a las 12"*, con fichas (`R03 [02:26]`). Se puede llegar a 20 minutos de
revisión de sesiones y dejarle intactas tres horas de fichas. La historia cubre el paquete, no
el documento.

**c) Los 20 minutos son una meta de diseño, no un dato validado.** No hay línea base medida:
las cuatro fuentes de `EVIDENCIA.md` §5 son estimaciones de autorreporte dichas al paso. El
número sirve para dirigir el diseño y para construir la prueba. **No se presenta como hallazgo
ni como resultado esperado.** Medir primero es la prioridad 2 de `EVIDENCIA.md` §8.

### Criterios de aceptación

| # | Criterio | Cómo se mide |
|---|---|---|
| 1 | El paquete del día siguiente existe **antes** de que el docente se siente a revisar | El sistema no genera durante la sesión de revisión (ver §3) |
| 2 | Revisar y aprobar el paquete completo toma ≤ 20 min | Cronómetro en prueba de usabilidad, ≥ 3 docentes, tarea real |
| 3 | Nada llega a imprimirse sin aprobación explícita | Regla transversal 1 de `USE_CASES.md` |
| 4 | Las fichas salen diferenciadas por nivel por defecto | Regla de UC-02 |
| 5 | Los criterios del instrumento quedan ligados a la competencia | Para que E3 pueda valorarlos |
| 6 | Se conserva qué fue generado y qué editó una persona | Regla de UC-04; es el dato que prueba si la generación sirve |

El criterio 6 es el más valioso del proyecto y el más fácil de omitir. **La tasa de edición por
tipo de artefacto es la métrica que dice si la IA ayuda o estorba.** Si los docentes reescriben
el 80% de las fichas, el pipeline no funciona aunque el cronómetro dé 20 minutos.

### La consecuencia de diseño que nadie espera

20 minutos para 2 sesiones + 2 fichas + 2 instrumentos son **unos 3 minutos por artefacto**.
En 3 minutos no se lee un documento completo.

Por lo tanto la revisión **no puede ser "leer y corregir"**. Tiene que ser aprobación por
excepción: el sistema señala lo que requiere atención —lo que no pudo contextualizar, lo que
contradice la unidad, lo que quedó fuera del nivel de la sección— y el resto se aprueba en bloque.
Un editor de texto con el borrador completo garantiza fallar el criterio 2.

---

## 3. Dónde entra el LLM, y dónde no

### Dónde sí

| Uso | Caso de uso | Cuándo se ejecuta |
|---|---|---|
| Borrador de sesión | UC-01 | Al aprobar la unidad, en lote para el mes |
| Ejercicios de ficha, agrupados por nivel | UC-02 | Igual, en lote |
| Criterios de evaluación e instrumento | UC-03 | Igual, en lote |
| Ítems candidatos del banco (E0) | habilitador | Fuera de línea, una vez, revisión masiva |
| Conclusiones descriptivas | roadmap | No ahora (`ALCANCE.md` §3) |

### La decisión arquitectónica importante: generar por unidad, revisar por día

**El LLM no va en el camino crítico de la revisión.** Si el docente abre la app a las 10 de la
noche y ahí se dispara la generación, los 20 minutos se consumen esperando y corrigiendo, y con
mala conectividad ni eso. La generación ocurre **cuando se aprueba la unidad** —una vez al mes— y
produce el mes completo. La revisión diaria es revisión de material que ya existe.

Esto no es una optimización técnica, es lo que dice la evidencia:

- La unidad, no la sesión, es donde los docentes ubican el cuello de botella: *"en la unidad es
  donde nos demoramos más"* (`R01 [06:45]`), y se elabora de forma colegiada una vez al mes,
  fuera de horario (`R06 [01:03]`: una semana de trabajo conjunto).
- Lo pidieron así: *"la IA al mes les da las sesiones"* (`R16 [32:38]`).
- La expectativa declarada es planificación **entregada**, no un asistente que ayuda a planificar
  (I-9). *"Ese es el sueño"*.

Consecuencia: la unidad de trabajo del producto es el **mes**, y el artefacto de entrada es la
unidad de aprendizaje con su situación significativa. La sesión es la salida, no la entrada.

### Dónde no

| Prohibido | Razón |
|---|---|
| Calificar | Regla transversal 2. El sistema entrega evidencia, decide el docente |
| Asignar un nivel o una etiqueta diagnóstica a un estudiante | Un presunto autismo sin diagnóstico oficial ya se maneja "según lo que nosotros creemos" (`R16 [25:03]`). El sistema no puede consolidar esa sospecha en un dato |
| Que algo generado llegue al estudiante o a un registro oficial sin aprobación | Regla transversal 1 |
| Generar video | `ALCANCE.md` §4: la revisión de un video cuesta más que la del texto y rompe el objetivo |

El segundo merece énfasis porque es tentador y la evidencia lo desaconseja directamente. El
sistema puede pedirle al docente que marque el nivel de un estudiante; no puede inferirlo y menos
nombrarlo.

### Riesgo que condiciona el diseño de E1

`EVIDENCIA.md` §8 prioridad 1: existe un rumor de una sola fuente de que el currículo nacional
pasa a un enfoque **por contenidos** el próximo año, y sobre las sesiones, *"adiós sesiones"*
(`R14 [18:23]`). Sin verificar.

Mitigación barata: la plantilla del artefacto (qué secciones tiene una sesión, una unidad, un
instrumento) debe ser **dato configurable, no estructura fija en el código**. Si el formato
cambia, se cambia una plantilla y no el pipeline. Esto cuesta poco ahora y mucho después.

---

## 4. E1b — Replanificar cuando el día se rompe

Esto es lo que pediste con "que el sistema se ajuste a los cambios e interrupciones", y tiene
respaldo suficiente para ser épica propia.

| Evidencia | Fuente |
|---|---|
| Las sesiones no se alcanzan y hay que reprogramarlas: *"hay veces, no, no acabo… tengo que reprogramar"* | `R01 [08:25]` |
| La sesión de 90 minutos no alcanza | `EVIDENCIA.md` §3; `R13 [56:31]` |
| Charlas y actividades imprevistas de UGEL/Minedu: 2 h por evento | `EVIDENCIA.md` §3 |
| Fallas de proyector y PC: 1 h por eventualidad | `EVIDENCIA.md` §3 |
| Actividades de UGEL y municipio que colisionan, ~2 al mes, avisadas con días de anticipación; se resuelven fusionando actividades a mano | `R12 [01:26]`, `R12 [05:12]` |

### La distinción que evita contradecir el alcance

`ALCANCE.md` §4 pone **fuera del alcance** "recuperar tiempo lectivo perdido por charlas de UGEL
o fallas de equipos", por ser organizacional e infraestructura. Eso sigue siendo cierto y no se
toca.

Lo que E1b hace es distinto:

- **Fuera del alcance (sigue igual):** devolver las 2 horas de clase perdidas.
- **Dentro de E1b:** que el docente no tenga que **rehacer a mano** la planificación del mes
  porque se perdieron esas 2 horas.

Son dos cosas que se confunden fácil. El software no recupera el tiempo lectivo; evita que la
pérdida genere, encima, trabajo nocturno de reprogramación.

### Historias

> **Como** docente, **quiero** marcar que una sesión no se completó y que el sistema re-acomode
> lo que falta en los días restantes de la unidad, **para** no rehacer la programación del mes.

> **Como** docente, **quiero** registrar una interrupción institucional de un día y ver el plan
> ajustado, **para** no perder una tarde recalculando qué entra y qué sale.

> **Como** docente de educación física, **quiero** fusionar una actividad que exige la UGEL con
> una unidad ya programada, **para** cumplir ambas con una sola actividad.
> *(Ya lo hace a mano: `R12 [05:26]`.)*

La tercera cubre el único perfil de la ronda cuyo dolor principal no es la planificación. Es
barata si la replanificación ya existe, y conviene tenerla registrada para no asumir que el
docente de educación física es un usuario secundario.

---

## 5. Qué bloquea qué

```
E0 banco de ítems ──┬──► E1 planificar ──► E1b replanificar
                    │         │
                    │         └──► E3 evaluar ──► E4 refuerzo
                    └──────────────────────────────┘
                              E2 aplicar en clase
                              (asistencia ya existe)

E5 familias — independiente, se puede hacer en cualquier momento
```

- **E0 bloquea casi todo.** Es contenido, no código, y por eso se subestima.
- **E3 depende de E1**, porque los criterios que se valoran nacen en el instrumento de la sesión.
- **E4 depende de E3**, porque sin evidencia por competencia no hay a quién focalizar el refuerzo.
- **E5 no depende de nada** y es la de mayor alivio percibido por unidad de esfuerzo. Buena
  candidata si E0 se atrasa.

### Lo primero que hay que hacer, y no es código

1. **Medir la línea base.** Registro de tiempos de una semana con 2 o 3 docentes, separando
   sesión, ficha, instrumento e impresión. Sin esto, los 20 minutos no tienen contra qué
   compararse y el indicador principal del piloto queda sin sustento.
2. **Conseguir una unidad de aprendizaje y un registro auxiliar reales.** Ya se pidieron
   (`R06 [03:25]`) y se mostraron parcialmente (`R15`, `R16`) sin capturarse. Son el contrato de
   entrada y salida de E1; diseñar el pipeline sin tenerlos delante es adivinar.
3. **Verificar el cambio curricular** contra fuentes oficiales del Minedu.

---

## 6. Historial

| Fecha | Cambio |
|---|---|
| 2026-10-16 | Documento creado. Épicas E0–E5 formalizadas sobre los grupos de `USE_CASES.md`. E1b (replanificación) añadida con respaldo de la ronda del 16-10-2026. Historia principal de E1 fijada en 20 min para el paquete diario completo |
