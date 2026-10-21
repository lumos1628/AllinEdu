import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { getSession } from '../services/api'
import type { Section, SessionDetail } from '../services/api'

export interface ClassInfo {
  slotId: number
  date: string
  start: string
  end: string
  area: string
}

interface Props {
  section: Section
  info: ClassInfo
  sessionId: number
  onBack: () => void
  onOpenSession: () => void
  onOpenInstrument: () => void
  onTakeAttendance: () => void
}

const INSTRUMENT_TYPE: Record<string, string> = {
  ESCALA_VALORACION: 'La escala valorativa',
  LISTA_COTEJO: 'La lista de cotejo',
  RUBRICA: 'La rúbrica',
}

function longDate(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number)
  return new Intl.DateTimeFormat('es-PE', { weekday: 'long', day: 'numeric', month: 'long' }).format(new Date(y, m - 1, d))
}

interface OptionProps {
  icon: ReactNode
  title: string
  sub: string
  tone?: 'gold' | 'blue' | 'green' | 'purple'
  onClick?: () => void
  soon?: boolean
}

function Option({ icon, title, sub, tone = 'blue', onClick, soon }: OptionProps) {
  const body = (
    <>
      <span className={`class-opt-icon is-${tone}`} aria-hidden="true">{icon}</span>
      <span className="class-card-text">
        <span className="class-card-area">{title}</span>
        <span className="class-card-sub">{sub}</span>
      </span>
      {soon ? <span className="class-soon">Próximamente</span> : <span className="class-opt-arrow" aria-hidden="true">›</span>}
    </>
  )
  if (soon) return <div className="class-card class-opt is-soon">{body}</div>
  return (
    <button className="class-card class-opt" onClick={onClick}>
      {body}
    </button>
  )
}

const stroke = { fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round', strokeLinejoin: 'round' } as const

export default function ClassView({
  section,
  info,
  sessionId,
  onBack,
  onOpenSession,
  onOpenInstrument,
  onTakeAttendance,
}: Props) {
  const [session, setSession] = useState<SessionDetail | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    getSession(sessionId)
      .then(setSession)
      .catch(() => setError('No se pudo cargar la clase.'))
  }, [sessionId])

  const status = !session
    ? null
    : session.activities.length === 0
      ? { label: 'Sin secuencia', tone: 'empty' }
      : session.approved
        ? { label: 'Lista', tone: 'ready' }
        : { label: 'Borrador por revisar', tone: 'draft' }

  return (
    <div className="home">
      <button className="link back-link" onClick={onBack}>← Inicio</button>

      <div className="doc-hero">
        <p className="home-greet">
          {longDate(info.date)} · {info.start} a {info.end} · {section.grado.split(' ')[0]} {section.nombre}
        </p>
        <h1 className="doc-title">{info.area}</h1>
        {session && (
          <p className="class-hero-sub">
            {session.title}
            {status && <span className={`class-state is-${status.tone}`}>{status.label}</span>}
          </p>
        )}
      </div>

      {error && <p className="error">{error}</p>}
      {!session && !error && <p className="muted">Cargando la clase…</p>}

      {session && (
        <div className="desk-top">
          <ul className="class-list" aria-label="Lo que tienes para esta clase">
            <li>
              <Option
                tone="blue"
                title="Ver la sesión"
                sub={
                  session.activities.length
                    ? `El documento completo · ${session.activities.length} actividades`
                    : 'Todavía sin secuencia: escríbela o pide un borrador'
                }
                onClick={onOpenSession}
                icon={
                  <svg width="22" height="22" viewBox="0 0 24 24" {...stroke}>
                    <path d="M5 4h14v16H5z" />
                    <path d="M9 9h6M9 13h6M9 17h3" />
                  </svg>
                }
              />
            </li>
            <li>
              {session.instrument ? (
                <Option
                  tone="purple"
                  title={INSTRUMENT_TYPE[session.instrument.type] ?? 'El instrumento'}
                  sub={`Con qué vas a mirar · ${session.instrument.criteria.length} criterios · para imprimir`}
                  onClick={onOpenInstrument}
                  icon={
                    <svg width="22" height="22" viewBox="0 0 24 24" {...stroke}>
                      <path d="M4 6h4v4H4zM4 14h4v4H4z" />
                      <path d="M12 8h8M12 16h8" />
                    </svg>
                  }
                />
              ) : (
                <Option
                  soon
                  tone="purple"
                  title="El instrumento"
                  sub="Esta sesión todavía no tiene criterios ni instrumento"
                  icon={
                    <svg width="22" height="22" viewBox="0 0 24 24" {...stroke}>
                      <path d="M4 6h4v4H4zM4 14h4v4H4z" />
                      <path d="M12 8h8M12 16h8" />
                    </svg>
                  }
                />
              )}
            </li>
            <li>
              <Option
                soon
                tone="gold"
                title="La ficha de los niños"
                sub="Lo que reciben los estudiantes, con su versión diferenciada"
                icon={
                  <svg width="22" height="22" viewBox="0 0 24 24" {...stroke}>
                    <path d="M7 3h10l3 3v15H7z" />
                    <path d="M11 10h5M11 14h5" />
                  </svg>
                }
              />
            </li>
            <li>
              <Option
                soon
                tone="green"
                title="Anotar en clase"
                sub="Quién logró cada criterio, para que el cierre de bimestre se llene solo"
                icon={
                  <svg width="22" height="22" viewBox="0 0 24 24" {...stroke}>
                    <path d="M20 6L9 17l-5-5" />
                  </svg>
                }
              />
            </li>
          </ul>

          <aside className="unit-side">
            <section className="desk-unit">
              <h2 className="home-label">Al empezar la clase</h2>
              <button className="home-take slot-new" onClick={onTakeAttendance}>
                Tomar asistencia
              </button>
              {session.criteria.length > 0 && (
                <>
                  <p className="doc-field-dark">Para decirles al empezar</p>
                  <p className="class-say">
                    «Hoy vamos a {session.purpose ? session.purpose.charAt(0).toLowerCase() + session.purpose.slice(1).replace(/\.$/, '') : 'trabajar juntos'}.
                    Voy a mirar {session.criteria.length === 1 ? 'una cosa' : `${session.criteria.length} cosas`}:{' '}
                    {session.criteria.map((c) => c.replace(/\.$/, '').toLowerCase()).join('; ')}.»
                  </p>
                  <p className="desk-unit-meta">
                    El currículo pide comunicar los criterios antes de empezar (CNEB, §VII). Dilo a tu manera si prefieres.
                  </p>
                </>
              )}
            </section>
          </aside>
        </div>
      )}
    </div>
  )
}
