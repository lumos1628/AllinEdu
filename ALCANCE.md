# Alcance de AllinEdu

> Qué entra al prototipo, qué no, y por qué. Las decisiones de este documento se sustentan en
> [`EVIDENCIA.md`](EVIDENCIA.md); si un punto no tiene respaldo ahí, está marcado como hipótesis.

---

## 1. El producto en una frase

AllinEdu reduce la carga operativa del docente en la preparación, aplicación y evaluación de
sus clases, y convierte la evidencia que genera ese ciclo en apoyo para reforzar aprendizajes.

**ODS 4** — Educación de calidad: detectar brechas por competencia y sostener la retención.
**ODS 8** — Trabajo decente: recuperar horas de trabajo no remunerado y devolver un límite
entre la jornada y el descanso.

El encuadre del ODS 8 se argumenta como reducción de sobrecarga administrativa y trabajo fuera
de horario, con anclaje en el Marco de Buen Desempeño Docente, no como "eficiencia".

---

## 2. La cadena

AllinEdu no son dos módulos pegados. Es un ciclo donde cada paso alimenta al siguiente.

```
  PLANIFICAR              APLICAR EN CLASE           EVALUAR
  currículo / unidad      asistencia                 criterios por
  sesión             ──►  evidencia por         ──►  competencia
  ficha diferenciada      criterio
  instrumento                   │                         │
                                │                         ▼
                                ▼                   EVIDENCIA POR
                         COMUNICACIÓN                COMPETENCIA
                         CON FAMILIAS                      │
                         (constancia                       ├──► refuerzo focalizado
                          y horario)                       │     y repaso espaciado
                                                           │
                                                           └──► insumo para conclusiones
                                                                 descriptivas
```

Por qué importa que sea una cadena y no dos mitades: el dolor más costoso en horas
(conclusiones descriptivas, 2–10 h por bimestre según encuestas) solo se puede atacar si existe
evidencia por competencia, y esa evidencia la produce el ciclo de clase. Sin la parte
pedagógica no hay insumo para la parte administrativa.

---

## 3. Catálogo de funcionalidades y decisiones

### Núcleo del prototipo

| Funcionalidad | Sustento | Estado |
|---|---|---|
| Pipeline de preparación: unidad → sesión → ficha diferenciada → instrumento → imprimible | 2.5–4 h/día (I-1) | **Parcial (web):** escritorio con horario semanal, unidad con sus sesiones y sesión como documento con aprobación docente. Falta elaborar/editar, ficha diferenciada e imprimible |
| Asistencia digital en el aula, sin fotografía ni volcado posterior | I-2 | **Parcial: ya existe** |
| Captura de evidencia por criterio durante la clase | I-3 | Por construir |
| Banco de ítems por competencia, grado y nivel de dificultad | Sustrato de todo lo demás | Por construir |
| Diagnóstico breve por competencia | Dolor 6 | Por construir |
| Práctica progresiva por nivel de dificultad | I-5 | Por construir |
| Repaso espaciado | Agarwal 2021, Latimier 2021 | Por construir |
| Canal con familias: constancia de respuesta y horario de atención visible | I-4 | Por construir |
| Generación asistida de ítems y borradores, con revisión docente obligatoria | I-1, NFER 2024 | Por construir |

**Sobre la generación con IA:** no es un extra, es lo que evita que el proyecto se contradiga.
Si el docente tuviera que redactar a mano los ítems y las fichas, le habríamos *agregado* carga
operativa y roto el ODS 8. Regla permanente: **la IA propone borradores, el docente aprueba.**

### Si alcanza el tiempo

| Funcionalidad | Por qué es barata |
|---|---|
| Modo concurso (práctica cronometrada con formato de certamen) | Sale del banco de ítems ya construido |
| Reportes y libreta exportables | Es exportar datos que ya están en el sistema |

### Roadmap documentado, no construido

| Funcionalidad | Por qué no entra ahora |
|---|---|
| Conclusiones descriptivas asistidas | Alto valor (2–10 h/bimestre), pero requiere que el ciclo de evidencia ya funcione. Desde 2026-10-19 la web permite **escribirlas a mano** en el cierre de bimestre (sin borrador generado); la parte asistida sigue aquí |
| Registro de notas | Depende de poder exportar al formato de SIAGIE. Ver decisión pendiente D-1 |
| Repositorio de material digital y videos **enlazados** | Bajo costo técnico, alto costo de contenido |

---

## 4. Fuera del alcance, con razón explícita

| Fuera | Razón |
|---|---|
| Generación de **videos** con IA | Sin base pedagógica, costo alto y carga de revisión peor que en texto: un ítem se revisa en segundos, un video hay que verlo completo. Contradice el objetivo de reducir carga. Alternativa: enlazar video existente. |
| Mejorar la calidad del material del Estado | No es un problema de software (EVIDENCIA §7) |
| Integración automática con SIAGIE | Sistema cerrado, sin API confirmada |
| Portal familiar completo / mensajería libre | Se convierte en WhatsApp. El canal queda acotado: un hilo por estudiante, iniciado por el docente, asincrónico, sin tiempo real ni grupos |
| Calificación automática o decisiones pedagógicas automáticas | El sistema entrega evidencia; decide el docente |
| Vigilancia de estudiantes | Los simulacros son práctica formativa de bajo riesgo, no evaluación de alto impacto |
| Recuperar tiempo lectivo perdido por charlas de UGEL o fallas de equipos | Organizacional e infraestructura, no software |

---

## 5. Orden de implementación

Priorizado por horas recuperadas por hora de desarrollo.

1. **Pipeline de preparación.** Es el 90% del tiempo recuperable. Empezar por sesión + ficha
   diferenciada; el instrumento de evaluación después.
2. **Captura de evidencia en clase.** Habilita la evaluación por criterios y elimina el
   "hago un recuerdo". Se apoya en la asistencia ya construida.
3. **Banco de ítems + diagnóstico + práctica progresiva + repaso espaciado.** Una competencia y
   un tema para el piloto: 40–60 ítems.
4. **Canal con familias.** Barato y de alto alivio percibido.

**El banco de ítems es el cuello de botella real y no es un problema de código.** Sin ítems no
funciona el diagnóstico, ni la práctica, ni el repaso, ni el modo concurso. Para el piloto se
delimita a una competencia y un tema. Cubrir el currículo completo no es alcance de este
proyecto y debe decirse en la delimitación.

---

## 6. Delimitación del piloto

| Dimensión | Delimitación |
|---|---|
| Alcance de investigación | Descriptivo |
| Usuarios | Docentes de primaria pública; coordinación/dirección por confirmar |
| Competencia y tema | Uno, definido con docentes |
| Ítems | 40–60 para el tema piloto |
| Indicadores | Tiempo y número de pasos por tarea; usabilidad (SUS); resultados de diagnóstico, práctica posterior y prueba diferida |
| Con estudiantes | Solo con acceso y consentimiento autorizados |

No se fija de antemano un porcentaje de mejora.

---

## 7. Decisiones pendientes

| ID | Decisión | Bloqueada por |
|---|---|---|
| D-1 | ¿Entra el registro de notas? | Saber si se puede exportar al formato que SIAGIE importa. Si no se puede, queda fuera: una app que registra notas sin alimentar SIAGIE agrega un **tercer** lugar donde tipear y empeora el problema |
| D-2 | ¿Se renombra el código de `EduFast` a `AllinEdu`? | Decisión del equipo. Afecta paquetes Java/Kotlin, nombre de BD, clases, claves de `localStorage` y cuentas de prueba. Desde 2026-10-14 el nombre **visible** en web y Android ya es AllinEdu; el renombrado interno sigue pendiente |
| D-3 | ¿Hay piloto con estudiantes? | Autorización institucional y consentimiento |
| D-4 | ¿Coordinación/dirección entra como rol? | Entrevistas de semana 9 |

---

## 8. Historial de cambios de alcance

| Fecha | Cambio | Motivo |
|---|---|---|
| 2026-10-12 | La planificación de clases pasa de **fuera del alcance** a **núcleo y prioridad 1** | La línea base mostró 2.5–4 h/día. El argumento previo (Selwyn 2025 sobre carga de revisión de IA) no aplica: la docente ya usa IA y ya paga ese costo |
| 2026-10-12 | La asistencia pasa de **fuera del alcance** a **eslabón del núcleo** | Es un dolor real y confirmado (I-2), y habilita la captura de evidencia |
| 2026-10-12 | "Centralizar actividades que coordinación asigna a docentes" deja de ser el eje central | No apareció como dolor prioritario en la investigación |
| 2026-10-12 | Los simulacros dejan de ser co-protagonistas y pasan a ser una función del ciclo | Son un módulo con cuatro modos de uso, no cuatro módulos |
| 2026-10-19 | La web incorpora el escritorio semanal, la unidad, la sesión (lectura y aprobación del borrador) y el cierre de bimestre, siguiendo el prototipo | Llevar a la app real el flujo validado en el prototipo con los datos que ya existen en la base. La aprobación cumple la Regla 0a; el nivel de logro lo elige siempre el docente (Regla 0b) |
| 2026-10-19 | El cierre de bimestre registra nivel de logro, conclusión y recomendación por estudiante. **No resuelve D-1**: queda como captura para el cierre, no como registro oficial de notas | Lo pide el prototipo y la RVM 048-2024 obliga a conclusión en B y C. Si D-1 se decide en contra, la pantalla se limita a redactar las conclusiones para copiarlas a SIAGIE |

> Re-delimitar después de empatizar es Design Thinking funcionando, no un retroceso. Cada
> cambio queda registrado aquí con su motivo.
