import { useEffect, useState } from 'react'
import { getMySections } from '../services/api'
import type { Section } from '../services/api'

interface Props {
  onSelect: (section: Section) => void
}

// Eslabones de la cadena de AllinEdu (ver ALCANCE.md §2). Solo "Aplicar en clase"
// está construido; el resto se muestra como próximo para que el docente vea hacia
// dónde va la herramienta sin prometer funciones que todavía no existen.
const CHAIN = [
  { id: 'plan', label: 'Planificar', hint: 'Sesión, ficha e instrumento', ready: false },
  { id: 'apply', label: 'Aplicar en clase', hint: 'Asistencia', ready: true },
  { id: 'evaluate', label: 'Evaluar', hint: 'Criterios por competencia', ready: false },
  { id: 'reinforce', label: 'Reforzar', hint: 'Práctica y repaso', ready: false },
] as const

// El backend envía el grado como texto ("3.º de primaria"); el nivel define el color.
function levelOf(grado: string): 'primaria' | 'secundaria' | 'otro' {
  const g = grado.toLowerCase()
  if (g.includes('primaria')) return 'primaria'
  if (g.includes('secundaria')) return 'secundaria'
  return 'otro'
}

function ChainStrip() {
  return (
    <ol className="chain" aria-label="Cadena de trabajo de AllinEdu">
      {CHAIN.map((step) => (
        <li key={step.id} className={step.ready ? 'chain-step ready' : 'chain-step'}>
          <span className="chain-label">{step.label}</span>
          <span className="chain-hint">{step.ready ? step.hint : 'Próximamente'}</span>
        </li>
      ))}
    </ol>
  )
}

function SectionCard({ section, onClick }: { section: Section; onClick: () => void }) {
  return (
    <button
      className={`section-card level-${levelOf(section.grado)}`}
      onClick={onClick}
      aria-label={`Tomar asistencia de ${section.grado}, sección ${section.nombre}`}
    >
      <span className="card-grade">{section.grado}</span>
      <span className="card-title">Sección {section.nombre}</span>
      <span className="card-action">Tomar asistencia →</span>
    </button>
  )
}

export default function SectionList({ onSelect }: Props) {
  const [sections, setSections] = useState<Section[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMySections()
      .then(setSections)
      .catch(() => setError('No se pudieron cargar tus secciones'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <ChainStrip />

      <h2>Mis secciones</h2>
      <p className="muted">Elige una sección para tomar la asistencia de hoy.</p>

      {loading && <p className="muted">Cargando secciones…</p>}
      {error && <p className="error">{error}</p>}
      {!loading && !error && sections.length === 0 && (
        <p className="muted">Aún no tienes secciones asignadas. Consulta con la dirección de tu institución.</p>
      )}

      <div className="section-grid">
        {sections.map((s) => (
          <SectionCard key={s.id} section={s} onClick={() => onSelect(s)} />
        ))}
      </div>
    </div>
  )
}
