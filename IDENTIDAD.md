# Identidad visual de AllinEdu

> Fuente única de colores y piezas de marca para web y Android.
> En código viven en `web/src/App.css` (`:root` y `:root[data-theme='light']`) y en
> `mobile/.../presentation/theme/AllinTheme.kt` (`DarkPalette`, `LightPalette`). Si cambias un
> valor, cámbialo en ambos.

## Idea

**Negro, dorado y azul.** El modo oscuro es el predeterminado: sobre negro el dorado brilla y
se vuelve el protagonista. El azul es su complementario (opuesto en el círculo cromático), por eso
lo acompaña sin competir. *Allin* es "bueno" en quechua; el dorado evoca el oro andino.

- **Dorado** = acción: botones, paso disponible de la cadena, logotipo, sellos.
- **Azul** = navegación y foco: enlaces, borde del campo activo, línea de la cadena.
- Se descartó el verde como complemento porque se confunde con "asistió".

El **modo claro** es opcional (botón ☀/☾) para aulas con mucha luz o el teléfono al sol. Se guarda
por dispositivo (`localStorage` en web, `SharedPreferences` en Android).

## Paleta

| Token (web) | `AllinPalette` | Oscuro | Claro | Uso |
|---|---|---|---|---|
| `--bg` | `background` | `#0E0F12` | `#F7F5F0` | Fondo de todas las pantallas |
| `--card` | `card` | `#17191E` | `#FFFFFF` | Tarjetas |
| `--input-bg` | `inputBg` | `#121418` | `#FFFFFF` | Fondo de campos |
| `--text` | `text` | `#F2EFE8` | `#1B1D22` | Texto principal, títulos, "Allin" |
| `--muted` | `muted` | `#A3A7B1` | `#5F6470` | Texto secundario |
| `--border` | `border` | `#2A2E37` | `#E4E1D8` | Bordes decorativos |
| `--input-border` | `inputBorder` | `#5F6676` | `#8C8F98` | Borde de campos (contraste 3:1) |
| `--rail` | `rail` | `#3A3F4B` | `#D3CFC4` | Línea del pase de lista |
| `--primary` | `primary` | `#E3B341` | `#E3B341` | Dorado de acción (relleno) |
| `--on-primary` | `onPrimary` | `#16130A` | `#16130A` | Texto sobre dorado |
| `--gold-text` | `goldText` | `#E3B341` | `#85650C` | Dorado como texto ("Edu", etiquetas) |
| `--accent` | `accent` | `#7AA2FF` | `#2F5BD3` | Azul complementario |

### Estados de asistencia (no son colores de marca)

| Estado | Oscuro (fondo / borde / texto) | Claro (fondo / borde / texto) |
|---|---|---|
| Asistió | `#10291C` / `#2F9E5E` / `#6EE7A0` | `#DCFCE7` / `#16A34A` / `#15803D` |
| Tardanza | `#2A2210` / `#B8890F` / `#F2C14E` | `#FEF9C3` / `#CA8A04` / `#854D0E` |
| Faltó | `#2E1416` / `#C93B3B` / `#F58A8A` | `#FEE2E2` / `#DC2626` / `#B91C1C` |

El estado **siempre se escribe** (sello "ASISTIÓ", "FALTÓ", "TARDANZA"), nunca se comunica solo con
color. El dorado de marca no se usa dentro de las tarjetas de asistencia para no competir con
"tardanza".

## Piezas de marca

- **Logotipo:** "Allin" en el color del texto + "Edu" en dorado, con una barra dorada debajo.
  Web: `components/Wordmark.tsx`. Android: `Wordmark()` en `AllinTheme.kt`.
- **La cadena:** pasos unidos por una línea azul; el paso disponible se enciende en dorado.
  Aparece en el login, en el menú y, como riel, en la lista de asistencia.
- **El sello:** etiqueta con borde, girada −6° y con rebote al aparecer. Estados y bienvenida.
- **Ícono:** `web/public/favicon.svg` ("A" dorada sobre negro, punto azul).
- **Cambio de modo:** `components/ThemeToggle.tsx` (web) y `ThemeToggleButton()` (Android).

## Reglas

1. No escribas colores hexadecimales en componentes: usa los tokens / `AllinColors`.
2. Contraste mínimo 4.5:1 para texto y 3:1 para bordes de controles, **en ambos modos**.
   En modo claro el dorado puro (1.9:1) solo rellena; como texto se usa `--gold-text`.
3. Toda animación respeta "reducir movimiento" del sistema.
