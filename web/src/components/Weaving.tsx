import type { CSSProperties } from 'react'
import Tocapu from './Tocapu'
import type { Dye, Motif } from './tejido'

const LOOM: { motif: Motif; dye: Dye }[] = [
  { motif: 'rombo', dye: 'cochinilla' },
  { motif: 'aspa', dye: 'maiz' },
  { motif: 'greca', dye: 'anil' },
]

// Mientras el generador trabaja: tres tocapus que se tejen fila por fila, uno tras otro.
export default function Weaving({ label, sub }: { label: string; sub?: string }) {
  return (
    <div className="weaving" role="status" aria-live="polite">
      <span className="weaving-loom" aria-hidden="true">
        {LOOM.map((t, i) => (
          <span key={t.motif} className="weaving-knot" style={{ '--i': i } as CSSProperties}>
            <Tocapu motif={t.motif} dye={t.dye} weave="woven" size={26} />
          </span>
        ))}
      </span>
      <span className="weaving-text">
        <strong>{label}</strong>
        {sub && <span className="weaving-sub">{sub}</span>}
      </span>
    </div>
  )
}
