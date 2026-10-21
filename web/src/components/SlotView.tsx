import { useEffect, useRef, useState } from 'react'
import { getSlotOptions, prepareSlot } from '../services/api'
import type { Section, SlotOptions } from '../services/api'
import type { ClassInfo } from './ClassView'

interface Props {
  section: Section
  slotId: number
  date: string
  onBack: () => void
  onReady: (sessionId: number, info: ClassInfo) => void
}

const infoOf = (o: SlotOptions): ClassInfo => ({ slotId: o.slotId, date: o.date, start: o.start, end: o.end, area: o.area })

function longDate(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number)
  return new Intl.DateTimeFormat('es-PE', { weekday: 'long', day: 'numeric', month: 'long' }).format(new Date(y, m - 1, d))
}

export default function SlotView({ section, slotId, date, onBack, onReady }: Props) {
  const [options, setOptions] = useState<SlotOptions | null>(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState<number | 'new' | null>(null)
  const onReadyRef = useRef(onReady)
  useEffect(() => {
    onReadyRef.current = onReady
  })

  useEffect(() => {
    getSlotOptions(section.id, slotId, date)
      .then((o) => {
        // Si otra pestaña ya la preparó, se abre directamente.
        if (o.existingSessionId) onReadyRef.current(o.existingSessionId, infoOf(o))
        else setOptions(o)
      })
      .catch(() => setError('No se pudo cargar la clase.'))
  }, [section.id, slotId, date])

  async function choose(sessionId: number | null) {
    setBusy(sessionId ?? 'new')
    setError('')
    try {
      const session = await prepareSlot(section.id, slotId, date, sessionId)
      onReady(session.id, infoOf(options!))
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo preparar la clase')
      setBusy(null)
    }
  }

  return (
    <div className="home">
      <button className="link back-link" onClick={onBack}>← Inicio</button>

      {error && <p className="error" role="alert">{error}</p>}
      {!options && !error && <p className="muted">Cargando la clase…</p>}

      {options && (
        <>
          <div className="doc-hero">
            <p className="home-greet">
              {longDate(options.date)} · {options.start} a {options.end}
            </p>
            <h1 className="doc-title">Preparar {options.area}</h1>
          </div>

          {!options.unitId ? (
            <p className="home-empty">
              Ninguna unidad cubre esta fecha. Primero hay que crear o extender la unidad para poder preparar la clase.
            </p>
          ) : (
            <div className="desk-top">
              <section aria-labelledby="slot-candidates-title">
                <h2 id="slot-candidates-title" className="home-label">
                  Sesiones de «{options.unitTitle}» que aún no tienen fecha
                </h2>
                {options.candidates.length === 0 ? (
                  <p className="home-empty">No quedan sesiones de {options.area} sin fecha en esta unidad.</p>
                ) : (
                  <ul className="slot-list">
                    {options.candidates.map((s) => (
                      <li key={s.id}>
                        <button className="class-card" onClick={() => choose(s.id)} disabled={busy !== null}>
                          <span className="class-card-time">{s.number}</span>
                          <span className="class-card-text">
                            <span className="class-card-area">{s.title}</span>
                            <span className="class-card-sub">
                              {s.activities > 0 ? `${s.activities} actividades` : 'Sin secuencia todavía'}
                            </span>
                          </span>
                          <span className={s.approved ? 'class-state is-ready' : 'class-state is-draft'}>
                            {busy === s.id ? 'Asignando…' : s.approved ? 'Aprobada' : 'Borrador'}
                          </span>
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </section>

              <aside className="desk-unit">
                <h2 className="home-label">O empieza de cero</h2>
                <p className="desk-unit-situation">
                  Se crea una sesión vacía para esta clase, con las competencias de {options.area} de la unidad. Adentro
                  puedes escribirla o pedir un borrador.
                </p>
                <button className="home-take slot-new" onClick={() => choose(null)} disabled={busy !== null}>
                  {busy === 'new' ? 'Creando…' : 'Empezar una sesión nueva'}
                </button>
              </aside>
            </div>
          )}
        </>
      )}
    </div>
  )
}
