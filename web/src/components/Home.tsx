import { useEffect, useState } from 'react'
import { getClosure, getMySections, getWeek } from '../services/api'
import type { Closure, Section, Week, WeekBlock } from '../services/api'

interface Props {
  userName: string
  onTakeAttendance: (section: Section) => void
  onOpenUnit: (unitId: number) => void
  onOpenClosure: (section: Section) => void
}

// Eslabones de la cadena de AllinEdu (ver ALCANCE.md §2). Solo "Aplicar en clase"
// está construido; el resto se muestra como próximo para no prometer lo que no existe.
const CHAIN = [
  { id: 'plan', label: 'Planificar', hint: 'Sesión, ficha e instrumento', ready: false },
  { id: 'apply', label: 'Aplicar en clase', hint: 'Asistencia', ready: true },
  { id: 'evaluate', label: 'Evaluar', hint: 'Criterios por competencia', ready: false },
  { id: 'reinforce', label: 'Reforzar', hint: 'Práctica y repaso', ready: false },
] as const

const DAY_SHORT = ['', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

// "2026-10-19" se interpreta en hora local, no en UTC (evita correrse un día).
function parseDate(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}

function longDate(iso: string): string {
  return new Intl.DateTimeFormat('es-PE', { weekday: 'long', day: 'numeric', month: 'long' }).format(parseDate(iso))
}

function shortDate(iso: string): string {
  return new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'short' }).format(parseDate(iso))
}

function minutes(hhmm: string): number {
  const [h, m] = hhmm.split(':').map(Number)
  return h * 60 + m
}

// "4.º de primaria" + "B" → "4.º B"
function sectionLabel(s: Section): string {
  return `${s.grado.trim().split(/\s+/)[0]} ${s.nombre}`
}

function greeting(now: Date): string {
  const h = now.getHours()
  if (h < 12) return 'Buenos días'
  if (h < 19) return 'Buenas tardes'
  return 'Buenas noches'
}

type BlockState = 'past' | 'now' | 'next' | 'later'

function stateOf(block: WeekBlock, nowMin: number, nextStart: string | null): BlockState {
  if (nowMin >= minutes(block.end)) return 'past'
  if (nowMin >= minutes(block.start)) return 'now'
  return block.start === nextStart ? 'next' : 'later'
}

const STATE_LABEL: Record<BlockState, string> = { past: 'Terminó', now: 'Ahora', next: 'Sigue', later: '' }

function Today({ week, section, onTakeAttendance }: { week: Week; section: Section; onTakeAttendance: () => void }) {
  const now = new Date()
  const nowMin = now.getHours() * 60 + now.getMinutes()
  const blocks = week.blocks.filter((b) => b.date === week.today)
  const upcoming = blocks.find((b) => minutes(b.start) > nowMin)

  return (
    <section className="desk-today" aria-labelledby="desk-today-title">
      <div className="desk-head">
        <h2 id="desk-today-title" className="home-label">Hoy en {sectionLabel(section)}</h2>
      </div>
      <button className="home-take desk-take" onClick={onTakeAttendance}>
        Tomar asistencia
      </button>
      {blocks.length === 0 ? (
        <p className="home-empty">Hoy no hay clases en el horario de esta sección.</p>
      ) : (
        <ol className="desk-day">
          {blocks.map((b) => {
            const st = stateOf(b, nowMin, upcoming?.start ?? null)
            return (
              <li key={b.start} className={`desk-slot is-${st}${b.mine ? '' : ' is-other'}`}>
                <span className="desk-slot-time">{b.start}</span>
                <span className="desk-slot-text">
                  <span className="desk-slot-area">{b.area}</span>
                  <span className="desk-slot-sub">
                    {b.start} a {b.end}
                    {!b.mine && ' · la dicta otro docente'}
                  </span>
                </span>
                {STATE_LABEL[st] && <span className="desk-slot-state">{STATE_LABEL[st]}</span>}
              </li>
            )
          })}
        </ol>
      )}
    </section>
  )
}

function UnitCard({ week, onOpen }: { week: Week; onOpen: (unitId: number) => void }) {
  const unit = week.unit
  if (!unit) {
    return (
      <section className="desk-unit" aria-labelledby="desk-unit-title">
        <h2 id="desk-unit-title" className="home-label">Unidad</h2>
        <p className="home-empty">Esta sección no tiene una unidad en curso.</p>
      </section>
    )
  }
  const upcoming = unit.startDate > week.today
  const pct = unit.totalSessions ? Math.round((unit.approvedSessions / unit.totalSessions) * 100) : 0
  return (
    <section className="desk-unit" aria-labelledby="desk-unit-title">
      <h2 id="desk-unit-title" className="home-label">
        {upcoming ? `Empieza el ${longDate(unit.startDate)}` : 'Unidad en curso'}
      </h2>
      <p className="desk-unit-title">{unit.title}</p>
      {unit.situation && <p className="desk-unit-situation">{unit.situation}</p>}
      <p className="desk-unit-meta">
        Del {shortDate(unit.startDate)} al {shortDate(unit.endDate)}
      </p>
      <div
        className="desk-bar"
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={unit.totalSessions}
        aria-valuenow={unit.approvedSessions}
        aria-label="Sesiones aprobadas"
      >
        <span style={{ width: `${pct}%` }} />
      </div>
      <p className="desk-unit-meta">
        <strong>{unit.approvedSessions}</strong> de {unit.totalSessions} sesiones aprobadas por ti
      </p>
      <button className="desk-link" onClick={() => onOpen(unit.id)}>
        Ver la unidad y sus sesiones
      </button>
    </section>
  )
}

function ClosureCard({ closure, onOpen }: { closure: Closure; onOpen: () => void }) {
  const total = closure.students.reduce((n, s) => n + s.marks.length, 0)
  const pending = closure.pendingConclusions
  return (
    <section className={pending > 0 ? 'desk-closure is-pending' : 'desk-closure'} aria-labelledby="desk-closure-title">
      <h2 id="desk-closure-title" className="home-label">
        Cierre del {closure.periodName}
      </h2>
      <p className="desk-closure-line">
        <span className="desk-closure-num">{pending}</span>
        {pending === 1 ? 'conclusión por escribir' : 'conclusiones por escribir'}
      </p>
      <p className="desk-unit-meta">
        Periodo del {shortDate(closure.startDate)} al {shortDate(closure.endDate)} · {total} niveles registrados en{' '}
        {closure.students.length} estudiantes
      </p>
      <button className="desk-link" onClick={onOpen}>
        {pending > 0 ? 'Escribirlas ahora' : 'Revisar el cierre'}
      </button>
    </section>
  )
}

function WeekGrid({ week }: { week: Week }) {
  const days = [...new Set(week.blocks.map((b) => b.dayOfWeek))].sort()
  const starts = [...new Set(week.blocks.map((b) => b.start))].sort((a, b) => minutes(a) - minutes(b))
  if (days.length === 0) return null
  const byKey = new Map(week.blocks.map((b) => [`${b.dayOfWeek}-${b.start}`, b]))
  const dateOf = (d: number) => week.blocks.find((b) => b.dayOfWeek === d)!.date

  return (
    <section className="desk-week" aria-labelledby="desk-week-title">
      <h2 id="desk-week-title" className="home-label">Horario de esta semana</h2>
      <div className="desk-week-scroll">
        <table className="desk-grid">
          <thead>
            <tr>
              <th scope="col"><span className="sr-only">Hora</span></th>
              {days.map((d) => {
                const isToday = dateOf(d) === week.today
                return (
                  <th key={d} scope="col" className={isToday ? 'is-today' : undefined}>
                    {isToday ? 'Hoy' : DAY_SHORT[d]} {parseDate(dateOf(d)).getDate()}
                  </th>
                )
              })}
            </tr>
          </thead>
          <tbody>
            {starts.map((s) => (
              <tr key={s}>
                <th scope="row">{s}</th>
                {days.map((d) => {
                  const b = byKey.get(`${d}-${s}`)
                  const isToday = dateOf(d) === week.today
                  const isPast = dateOf(d) < week.today
                  const cls = ['desk-cell', isToday && 'is-today', isPast && 'is-past', b && !b.mine && 'is-other']
                    .filter(Boolean)
                    .join(' ')
                  return (
                    <td key={d} className={cls}>
                      {b && (
                        <>
                          <span className="desk-cell-area">{b.area}</span>
                          {!b.mine && <span className="desk-cell-sub">otro docente</span>}
                        </>
                      )}
                    </td>
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function ChainTrack() {
  return (
    <section className="home-chain" aria-labelledby="home-chain-title">
      <h2 id="home-chain-title" className="home-label">Lo que hace AllinEdu por tu clase</h2>
      <ol className="home-chain-list">
        {CHAIN.map((step) => (
          <li key={step.id} className={step.ready ? 'home-step ready' : 'home-step'}>
            <span className="home-step-dot" aria-hidden="true" />
            <span className="home-step-label">{step.label}</span>
            <span className="home-step-hint">{step.ready ? `${step.hint} · disponible` : 'Próximamente'}</span>
          </li>
        ))}
      </ol>
    </section>
  )
}

export default function Home({ userName, onTakeAttendance, onOpenUnit, onOpenClosure }: Props) {
  const [sections, setSections] = useState<Section[]>([])
  const [current, setCurrent] = useState<Section | null>(null)
  const [week, setWeek] = useState<Week | null>(null)
  const [closure, setClosure] = useState<Closure | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const now = new Date()

  useEffect(() => {
    getMySections()
      .then((list) => {
        setSections(list)
        setCurrent(list[0] ?? null)
        if (list.length === 0) setLoading(false)
      })
      .catch(() => {
        setError('No se pudieron cargar tus secciones. Revisa tu conexión y vuelve a entrar.')
        setLoading(false)
      })
  }, [])

  useEffect(() => {
    if (!current) return
    let alive = true
    getWeek(current.id)
      .then((w) => alive && setWeek(w))
      .catch(() => alive && setError('No se pudo cargar el horario de la sección.'))
      .finally(() => alive && setLoading(false))
    // El cierre es opcional en el inicio: si no hay bimestre abierto, no se muestra.
    getClosure(current.id)
      .then((c) => alive && setClosure(c))
      .catch(() => alive && setClosure(null))
    return () => {
      alive = false
    }
  }, [current])

  const dateLabel = week ? longDate(week.today) : new Intl.DateTimeFormat('es-PE', { weekday: 'long', day: 'numeric', month: 'long' }).format(now)

  return (
    <div className="home">
      <div className="home-hero">
        <p className="home-greet">{greeting(now)}{userName ? `, ${userName}` : ''}</p>
        <h1 className="home-date">{dateLabel}</h1>
      </div>

      {sections.length > 1 && (
        <div className="desk-tabs" role="tablist" aria-label="Tus secciones">
          {sections.map((s) => (
            <button
              key={s.id}
              role="tab"
              aria-selected={current?.id === s.id}
              className="desk-tab"
              onClick={() => {
                if (current?.id === s.id) return
                setWeek(null)
                setClosure(null)
                setLoading(true)
                setCurrent(s)
              }}
            >
              {sectionLabel(s)}
            </button>
          ))}
        </div>
      )}

      {error && <p className="error">{error}</p>}
      {loading && !error && <p className="muted">Cargando tu semana…</p>}
      {!loading && !error && sections.length === 0 && (
        <p className="home-empty">Aún no tienes secciones asignadas. Pide a la dirección de tu institución que te asigne una.</p>
      )}

      {week && current && (
        <>
          <div className="desk-top">
            <Today week={week} section={current} onTakeAttendance={() => onTakeAttendance(current)} />
            <div className="unit-side">
              <UnitCard week={week} onOpen={onOpenUnit} />
              {closure && <ClosureCard closure={closure} onOpen={() => onOpenClosure(current)} />}
            </div>
          </div>
          <WeekGrid week={week} />
        </>
      )}

      <ChainTrack />
    </div>
  )
}
