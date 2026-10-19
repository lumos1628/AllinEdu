# Prototipo AllinEdu

Veintiún pantallas en dos páginas: **móvil** (en el aula) y **web** (preparar en
casa y cerrar el bimestre). Es la fuente editable del prototipo, no una captura.

Cada `.dc.html` es un *artboard* autónomo: HTML con estilos en línea y, al final,
una clase `DCLogic` cuyo `renderVals()` devuelve los valores que rellenan los
`{{huecos}}` de la plantilla. Se navega entre pantallas con enlaces normales
(`<a href="Sesion.dc.html">`). `canvas.json` es el índice: posición de cada
tablero en el lienzo, a qué página pertenece, el orden de lectura y las notas
amarillas con el anclaje normativo.

## Las pantallas

**Móvil — el día de clase**: Inicio · Asistencia · Clases de hoy (acordeón) ·
Anotar en clase · La sesión de aprendizaje · La ficha · La lista de cotejo
**Móvil — preparar mañana**: Preparar mañana · El borrador · Otro camino para
dos niños · Así sale en papel · Ya está · No pude prepararlo
**Móvil — cierre**: Cierre del bimestre · Comentario del bimestre
**Web**: Escritorio · Hoy · La unidad · Elaborar la sesión · Revisar con la
fuente al lado · Cierre del bimestre

## Para trabajar en él

Los archivos se editan con cualquier editor de texto. Para verlos renderizados
hacen falta el `dc-runtime.js` y el `support.js` del tipo de artefacto Design,
que no están aquí porque no son nuestros: se publican junto al artefacto. El
lienzo vivo está en el artefacto de Claude y estos archivos son la copia de
trabajo, así que al cambiar uno hay que volver a publicarlo allí para verlo.

## Nombres

Todas las personas del prototipo son inventadas: docente Zevallos, Ana
Portocarrero, Luz Valdivieso, Sara Arriaga, Rosa Larrea, Pedro Ocampo. Se
eligieron comprobando que **no aparecen** en `investigacion/material docente/`,
que es material real y está en `.gitignore`. Si se añaden nombres nuevos, hay
que hacer la misma comprobación antes de commitear.

## Pendiente

Tres cosas que quedaron a medias y conviene arreglar antes de usar esto como
base firme:

1. **Una cita de página inventada.** Dos pantallas dicen «Programa Curricular,
   p. 139». Las páginas verificadas de la edición publicada son **238** para
   Matemática y **165** para Comunicación. Hay que corregirla: el proyecto
   entero se apoya en que ninguna cifra se cita sin estar en una fuente
   descargada (`investigacion/normativa/FUENTES.md`).
2. **El instrumento está mal nombrado.** `Cotejo.dc.html` se llama «lista de
   cotejo» y registra Sí/No, pero el formato real de la docente usa **escala de
   valoración** con niveles por criterio. Lo mismo en las pantallas web de
   anotar en clase y de cierre. La versión móvil de tres niveles sí era
   correcta.
3. **`WCrear` y `WRevision` se solapan**: elaborar la sesión y revisarla
   muestran casi lo mismo y podrían fundirse en una.
