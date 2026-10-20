import { useEffect, useState } from 'react'
import { getUnit } from '../services/api'
import type { UnitDetail } from '../services/api'

interface Props {
  unitId: number
  onBack: () => void
  onOpenSession: (sessionId: number) => void
}

function shortDate(iso: string | null): string {
  if (!iso) return ''
  const [y, m, d] = iso.split('-').map(Number)
  return new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'long' }).format(new Date(y, m - 1, d))
}

export default function UnitView({ unitId, onBack, onOpenSession }: Props) {
  const [unit, setUnit] = useState<UnitDetail | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getUnit(unitId)
      .then(setUnit)
      .catch(() => setError('No se pudo cargar la unidad.'))
  }, [unitId])

  const approved = unit?.sessions.filter((s) => s.approved).length ?? 0

  return (
    <div className="home">
      <button className="link back-link" onClick={onBack}>← Inicio</button>

      {error && <p className="error">{error}</p>}
      {!unit && !error && <p className="muted">Cargando la unidad…</p>}

      {unit && (
        <>
          <div className="doc-hero">
            <p className="home-greet">
              Del {shortDate(unit.startDate)} al {shortDate(unit.endDate)} · {unit.sessions.length} sesiones
            </p>
            <h1 className="doc-title">{unit.title}</h1>
          </div>

          <div className="desk-top">
            <section aria-labelledby="unit-sessions-title">
              <h2 id="unit-sessions-title" className="home-label">
                Sesiones · {approved} de {unit.sessions.length} aprobadas por ti
              </h2>
              <ol className="unit-sessions">
                {unit.sessions.map((s) => (
                  <li key={s.id}>
                    <button className="unit-session" onClick={() => onOpenSession(s.id)}>
                      <span className="unit-session-num">{s.number}</span>
                      <span className="unit-session-text">
                        <span className="unit-session-title">{s.title}</span>
                        <span className="unit-session-sub">
                          {s.competencies.join(' · ')}
                          {s.activities === 0 && ' · sin secuencia todavía'}
                        </span>
                      </span>
                      <span className={s.approved ? 'status-dot is-ok' : 'status-dot is-draft'}>
                        {s.approved ? 'Aprobada' : 'Borrador'}
                      </span>
                    </button>
                  </li>
                ))}
              </ol>
            </section>

            <aside className="unit-side">
              {unit.situation && (
                <section className="unit-situation" aria-labelledby="unit-situation-title">
                  <h2 id="unit-situation-title" className="home-label">La situación de donde sale todo</h2>
                  <p>{unit.situation}</p>
                </section>
              )}

              <section className="desk-unit" aria-labelledby="unit-comp-title">
                <h2 id="unit-comp-title" className="home-label">Competencias de la unidad</h2>
                <ul className="unit-comps">
                  {unit.competencies.map((c) => (
                    <li key={c.id}>
                      <span className="unit-comp-name">{c.name}</span>
                      <span className={c.sessions === 0 ? 'unit-comp-count is-zero' : 'unit-comp-count'}>
                        {c.sessions === 0
                          ? 'Ninguna sesión la trabaja'
                          : `${c.sessions} ${c.sessions === 1 ? 'sesión' : 'sesiones'}`}
                      </span>
                    </li>
                  ))}
                </ul>
              </section>
            </aside>
          </div>
        </>
      )}
    </div>
  )
}
