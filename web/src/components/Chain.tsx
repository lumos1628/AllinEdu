import type { CSSProperties } from 'react'
import Tocapu from './Tocapu'
import type { Dye, Motif, Weave } from './tejido'

// La cadena de AllinEdu (ALCANCE.md §2) como una faja: cada eslabón es un tocapu y
// su textura dice cuánto está construido. Tejido = se puede usar hoy; dibujado =
// una parte existe; telar vacío = todavía no.
const LINKS: { label: string; hint: string; weave: Weave; motif: Motif; dye: Dye }[] = [
  { label: 'Planificar', hint: 'La sesión y su instrumento', weave: 'woven', motif: 'escalera', dye: 'maiz' },
  { label: 'Aplicar en clase', hint: 'La asistencia', weave: 'woven', motif: 'rombo', dye: 'cochinilla' },
  { label: 'Evaluar', hint: 'El cierre del bimestre', weave: 'drawn', motif: 'greca', dye: 'anil' },
  { label: 'Reforzar', hint: 'Práctica y repaso', weave: 'bare', motif: 'cruz', dye: 'chilca' },
]

const WEAVE_LABEL: Record<Weave, string> = { woven: 'Disponible', drawn: 'En camino', bare: 'Próximamente' }

export default function Chain({ size = 36, hints = false }: { size?: number; hints?: boolean }) {
  return (
    <ol className={hints ? 'chain has-hints' : 'chain'} style={{ '--knot': `${size}px` } as CSSProperties}>
      {LINKS.map((link, i) => (
        <li key={link.label} className={`chain-link is-${link.weave}`} style={{ '--i': i } as CSSProperties}>
          <Tocapu motif={link.motif} dye={link.dye} weave={link.weave} size={size} className="chain-knot" />
          <span className="chain-label">{link.label}</span>
          <span className="chain-state">
            {WEAVE_LABEL[link.weave]}
            {hints && link.weave !== 'bare' && `: ${link.hint.charAt(0).toLowerCase()}${link.hint.slice(1)}`}
          </span>
        </li>
      ))}
    </ol>
  )
}
