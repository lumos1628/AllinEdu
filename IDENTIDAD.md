# Identidad visual de AllinEdu

> Fuente única de colores y piezas de marca para web y Android.
> En código viven en `web/src/App.css` (`:root` y `:root[data-theme='light']`), en
> `web/src/components/tejido.ts` (motivos y el hilo de cada área) y en
> `mobile/.../presentation/theme/AllinTheme.kt` (`DarkPalette`, `LightPalette`). Si cambias un
> valor, cámbialo en ambos.
>
> **Estado (2026-10-24):** la web ya usa la identidad textil descrita aquí. Android sigue con la
> anterior (negro, dorado y azul; logotipo con barra dorada) hasta que se alinee con estos tokens.

## Idea

**Tintes andinos sobre añil.** *Allin* es "bueno" en quechua. La identidad sale del textil andino:
el **tocapu**, el cuadro tejido con un motivo geométrico, y el **chumpi**, la faja tejida. No se
usa como adorno, sino para decir algo:

- **Color = qué área.** Cada área curricular tiene su tinte y su motivo (cochinilla y rombo para
  Matemática, achiote y greca para Comunicación...). Color y forma juntos, para no depender solo
  del color.
- **Textura = cuánto está listo.** Lo aprobado por el docente está **tejido** (cuadro lleno); el
  borrador está solo **dibujado** (contorno con el motivo); lo que falta es **telar vacío** (marco
  discontinuo). Es la Regla 0a hecha visual: nada queda tejido sin la aprobación del docente.
- **Maíz** = acción: botones, el día de hoy, sellos. Sigue al dorado anterior.
- **Celeste de añil** = navegación y foco: enlaces y anillo de foco.
- El textil aparece solo donde informa (área, estado, avance de la unidad, la cadena) y en una
  franja de chumpi bajo el encabezado. No hay fondos estampados.

El **modo oscuro** (añil noche) es el predeterminado. El **modo claro** (cal) es opcional (botón
☀/☾) para aulas con mucha luz o el teléfono al sol. Se guarda por dispositivo (`localStorage` en
web, `SharedPreferences` en Android).

## Tipografía

| Familia | Uso |
|---|---|
| **Archivo** expandido (`font-stretch: 118%`, 600–800) | Títulos, fechas, horas, números grandes, logotipo. Letras de bloque, como tejido |
| **Atkinson Hyperlegible** (400, 700, cursiva) | Todo el texto de lectura, etiquetas y formularios. Diseñada para baja visión |

Las horas y los números de tablas usan Archivo: el cero con barra de Atkinson se lee como tachado
en tamaños chicos.

## Paleta

| Token (web) | Oscuro | Claro | Uso |
|---|---|---|---|
| `--bg` | `#11162A` | `#EEEDE8` | Fondo: añil noche / cal |
| `--card` | `#181F38` | `#FAF9F6` | Paneles |
| `--hover-bg` | `#212A4A` | `#E5E3DC` | Fila bajo el puntero |
| `--input-bg` | `#0C1022` | `#FFFFFF` | Fondo de campos |
| `--text` | `#F2EDE3` | `#171C30` | Texto principal: alpaca / tinta añil |
| `--muted` | `#A9AFC6` | `#535A70` | Texto secundario |
| `--border` | `#2C3555` | `#D8D5CC` | Bordes e hilos entre filas |
| `--input-border` | `#6B7598` | `#80869A` | Borde de campos (contraste 3:1) |
| `--rail` | `#3B456C` | `#C9C5BA` | Hilo de la cadena y del pase de lista |
| `--primary` | `#EDB53C` | `#EDB53C` | Maíz de acción (relleno) |
| `--on-primary` | `#1B1405` | `#1B1405` | Texto sobre maíz |
| `--gold-text` | `#EDB53C` | `#7E5A05` | Maíz como texto ("Edu", sellos, borradores) |
| `--accent` | `#93B3FF` | `#2848B0` | Celeste de añil: enlaces y foco |

### Tintes: el hilo de cada área

| Token | Área | Motivo | Oscuro | Claro |
|---|---|---|---|---|
| `--dye-cochinilla` | Matemática (y el logotipo) | rombo | `#E8628A` | `#B8325E` |
| `--dye-achiote` | Comunicación | greca | `#F08A4B` | `#B0501B` |
| `--dye-turquesa` | Personal Social | cruz | `#43C2C2` | `#157A7C` |
| `--dye-chilca` | Ciencia y Tecnología | escalera | `#5CC08C` | `#23845A` |
| `--dye-airampo` | Arte y Cultura | ojo | `#C27BE8` | `#8A3FB5` |
| `--dye-anil` | Educación Física | aspa | `#7C9DFF` | `#3354C4` |
| `--dye-nogal` | Educación Religiosa | pirámide | `#C89263` | `#8C5A30` |
| `--dye-qolle` | Inglés | damero | `#C9CF55` | `#727812` |
| `--dye-tara` | Tutoría | bandas | `#A7ADBF` | `#6B7085` |
| `--dye-maiz` | La unidad y sus sesiones | rombo / aspa | `#EDB53C` | `#A87708` |

Un área que no está en la lista recibe un hilo estable según su nombre (`threadOf` en
`tejido.ts`). Todos los tintes tienen contraste ≥ 3:1 sobre el fondo y los paneles, en ambos modos.

### Estados de asistencia y niveles (no son colores de marca)

| Estado | Oscuro (fondo / borde / texto) | Claro (fondo / borde / texto) |
|---|---|---|
| Asistió, AD, A | `#14302A` / `#34A56E` / `#74E3A6` | `#DCFCE7` / `#16A34A` / `#146C35` |
| Tardanza, B | `#302915` / `#BB8D1A` / `#F2C556` | `#FEF9C3` / `#CA8A04` / `#854D0E` |
| Faltó, C | `#361A2B` / `#D04A5A` / `#F79AA6` | `#FEE2E2` / `#DC2626` / `#B91C1C` |

El estado **siempre se escribe** (sello "Asistió", "Faltó", "Tardanza"), nunca se comunica solo con
color. El maíz de marca no se usa dentro de las tarjetas de asistencia para no competir con
"tardanza".

### La hoja

La sesión y el instrumento se muestran como **papel en ambos modos**, porque así se imprimen:
`--paper`, `--ink`, `--ink-soft`, `--rule`, `--paper-gold` y afines en `App.css`.

## Piezas de marca

- **Tocapu:** `components/Tocapu.tsx`. Motivos de 5×5 hilos en `tejido.ts`. Es decorativo
  (`aria-hidden`): el área y el estado siempre van también en texto al lado.
- **Logotipo:** tocapu de rombo tejido en cochinilla + "Allin" en el color del texto + "Edu" en
  maíz, en Archivo expandido. Web: `components/Wordmark.tsx`. Android: `Wordmark()` en
  `AllinTheme.kt` (pendiente de alinear).
- **El chumpi:** franja de rombos en cochinilla, maíz y añil (`Chumpi` en `Tocapu.tsx`). Una sola
  vez por pantalla: bajo el encabezado o arriba del login.
- **La unidad tejida:** un tocapu por sesión, en maíz; tejido si el docente la aprobó. Aparece en
  el escritorio, en la unidad y al elegir qué sesión preparar.
- **La cadena:** `components/Chain.tsx`. Cuatro tocapus unidos por un hilo; su textura dice cuánto
  de cada eslabón existe hoy. En el login los eslabones se tejen fila por fila al abrir: es el
  único movimiento que no responde a una acción del docente.
- **El borrador:** borde discontinuo en maíz con la urdimbre a la vista (líneas verticales). Igual
  en la etiqueta "Propuesta, sin revisar" y en las conclusiones que faltan del cierre.
- **El nudo:** un tocapu de aspa (dos hilos cruzados) en maíz, dibujado mientras la pregunta está
  pendiente y tejido cuando el docente la respondió (`components/KnotList.tsx`). La frase del
  borrador que causa un nudo se subraya con el trazo discontinuo del borrador.
- **Tejiendo:** mientras el generador trabaja, tres tocapus se tejen fila por fila en turnos
  (`components/Weaving.tsx`), y en la unidad se teje el tocapu de la sesión que está llegando.
  Es movimiento que responde a una acción del docente y se apaga con «reducir movimiento».
- **El sello:** etiqueta con borde, girada −5° y con rebote al aparecer. Aprobación, estados de
  asistencia y bienvenida.
- **Ícono:** `web/public/favicon.svg` (el tocapu del logotipo sobre añil).
- **Cambio de modo:** `components/ThemeToggle.tsx` (web) y `ThemeToggleButton()` (Android).

## Reglas

1. No escribas colores hexadecimales en componentes: usa los tokens / `AllinColors`.
2. Contraste mínimo 4.5:1 para texto y 3:1 para tintes, bordes de controles y tocapus, **en ambos
   modos**. En modo claro el maíz puro solo rellena; como texto se usa `--gold-text`.
3. El textil informa o no aparece: un tocapu siempre dice un área o un estado.
4. Toda animación respeta "reducir movimiento" del sistema.
