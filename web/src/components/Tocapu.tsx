import { useId } from 'react'
import { CELLS } from './tejido'
import type { Dye, Motif, Weave } from './tejido'

// Tocapu: el cuadro tejido de los textiles andinos. En AllinEdu cada área curricular
// tiene su tinte y su motivo (color y forma, para no depender solo del color), y el
// estado se lee en la textura: lo aprobado está tejido, el borrador solo dibujado y
// lo que falta es telar vacío (ver IDENTIDAD.md).

interface Props {
  motif: Motif
  dye: Dye
  weave: Weave
  size?: number
  className?: string
}

// Decorativo: el estado y el área siempre van también en texto al lado.
export default function Tocapu({ motif, dye, weave, size = 16, className }: Props) {
  const mask = useId()
  const cells = CELLS[motif]
  const color = `var(--dye-${dye})`
  return (
    <svg
      className={['tocapu', `is-${weave}`, className].filter(Boolean).join(' ')}
      width={size}
      height={size}
      viewBox="0 0 7 7"
      shapeRendering="crispEdges"
      aria-hidden="true"
      focusable="false"
    >
      {weave === 'woven' && (
        <>
          <mask id={mask}>
            <rect width="7" height="7" fill="#fff" />
            {cells.map(([x, y]) => (
              <rect key={`${x}-${y}`} x={x} y={y} width="1" height="1" fill="#000" />
            ))}
          </mask>
          <rect width="7" height="7" fill={color} mask={`url(#${mask})`} />
        </>
      )}
      {weave === 'drawn' && (
        <>
          <rect x="0.25" y="0.25" width="6.5" height="6.5" fill="none" stroke={color} strokeWidth="0.5" />
          {cells.map(([x, y]) => (
            <rect key={`${x}-${y}`} x={x} y={y} width="1" height="1" fill={color} />
          ))}
        </>
      )}
      {weave === 'bare' && (
        <rect
          x="0.25"
          y="0.25"
          width="6.5"
          height="6.5"
          fill="none"
          stroke="var(--muted)"
          strokeWidth="0.5"
          strokeDasharray="1 1"
        />
      )}
    </svg>
  )
}

// Franja de chumpi (faja tejida): rombos en tres tintes que se repiten. Cada hilo mide `cell` px.
export function Chumpi({ className, cell = 2 }: { className?: string; cell?: number }) {
  const id = useId()
  const dyes: Dye[] = ['cochinilla', 'maiz', 'anil']
  return (
    <svg
      className={['chumpi', className].filter(Boolean).join(' ')}
      style={{ height: cell * 5 }}
      aria-hidden="true"
      focusable="false"
      shapeRendering="crispEdges"
    >
      <defs>
        <pattern id={id} width={18 * cell} height={5 * cell} patternUnits="userSpaceOnUse">
          {dyes.map((dye, i) =>
            CELLS.rombo.map(([x, y]) => (
              <rect
                key={`${dye}-${x}-${y}`}
                x={(i * 6 + x - 1) * cell}
                y={(y - 1) * cell}
                width={cell}
                height={cell}
                fill={`var(--dye-${dye})`}
              />
            )),
          )}
        </pattern>
      </defs>
      <rect width="100%" height="100%" fill={`url(#${id})`} />
    </svg>
  )
}
