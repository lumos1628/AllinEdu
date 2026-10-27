import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { getClosure, getMySections, getWeek } from '../services/api'
import type { Closure, Section, Week, WeekBlock } from '../services/api'
import type { ClassInfo } from './ClassView'
import Chain from './Chain'
import Tocapu from './Tocapu'
import { BAND_MOTIFS, threadOf } from './tejido'
import type { Weave } from './tejido'

interface Props {
  userName: string
  onTakeAttendance: (section: Section) => void
  onOpenUnit: (unitId: number) => void
  onOpenClosure: (section: Section) => void
  onOpenClass: (section: Section, info: ClassInfo, sessionId: number) => void
  onPrepareClass: (section: Section, slotId: number, date: string) => void
}

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

function isoDate(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function addDays(iso: string, days: number): string {
  const d = parseDate(iso)
  d.setDate(d.getDate() + days)
  return isoDate(d)
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

type Prep = { label: string; tone: 'ready' | 'draft' | 'empty' }

// Lo aprobado está tejido, el borrador solo dibujado y lo que falta es telar vacío.
const WEAVE: Record<Prep['tone'], Weave> = { ready: 'woven', draft: 'drawn', empty: 'bare' }

// Qué tan lista está la sesión de esa clase. Dice lo que existe, no lo inventa.
function prepOf(b: WeekBlock): Prep {
  if (!b.session) return { label: 'Sin preparar', tone: 'empty' }
  if (b.session.activities === 0) return { label: 'Sin secuencia', tone: 'empty' }
  if (!b.session.approved) return { label: 'Borrador por revisar', tone: 'draft' }
  return { label: 'Lista', tone: 'ready' }
}

// El hilo del área con la textura de su preparación. El texto del estado va al lado.
function AreaKnot({ block, size }: { block: WeekBlock; size: number }) {
  const thread = threadOf(block.area)
  return <Tocapu motif={thread.motif} dye={thread.dye} weave={WEAVE[prepOf(block).tone]} size={size} />
}

interface ClassCardProps {
  block: WeekBlock
  timeState?: BlockState
  onOpen: (block: WeekBlock) => void
}

function ClassCard({ block, timeState, onOpen }: ClassCardProps) {
  const prep = prepOf(block)
  const time = timeState && STATE_LABEL[timeState]
  const body = (
    <>
      <span className="class-card-time">{block.start}</span>
      <span className="class-card-knot">{block.mine && <AreaKnot block={block} size={26} />}</span>
      <span className="class-card-text">
        <span className="class-card-area">{block.area}</span>
        <span className="class-card-sub">
          {block.mine ? (block.session ? block.session.title : 'Todavía no tiene sesión') : 'La dicta otro docente'}
        </span>
      </span>
      <span className="class-card-side">
        {time && <span className={`class-time is-${timeState}`}>{time}</span>}
        {block.mine && <span className={`class-state is-${prep.tone}`}>{prep.label}</span>}
      </span>
    </>
  )
  if (!block.mine) return <div className="class-card is-other">{body}</div>
  return (
    <button
      className={`class-card${timeState === 'now' ? ' is-now' : ''}`}
      onClick={() => onOpen(block)}
      aria-label={`${block.area}, ${block.start} a ${block.end}: ${prep.label}. ${block.session ? 'Abrir la clase' : 'Preparar la clase'}`}
    >
      {body}
    </button>
  )
}

interface DayProps {
  title: string
  blocks: WeekBlock[]
  isToday: boolean
  empty: string
  onOpen: (block: WeekBlock) => void
  action?: ReactNode
}

function DayClasses({ title, blocks, isToday, empty, onOpen, action }: DayProps) {
  const now = new Date()
  const nowMin = now.getHours() * 60 + now.getMinutes()
  const upcoming = blocks.find((b) => minutes(b.start) > nowMin)
  const id = `day-${title.replace(/\W+/g, '-')}`
  const pending = blocks.filter((b) => b.mine && prepOf(b).tone !== 'ready').length
  return (
    <section className="desk-day-block" aria-labelledby={id}>
      <div className="desk-head">
        <h2 id={id} className="home-label">{title}</h2>
        {!isToday && blocks.length > 0 && (
          <span className={pending ? 'class-state is-draft' : 'class-state is-ready'}>
            {pending ? `${pending} por preparar` : 'Todo listo'}
          </span>
        )}
      </div>
      {action}
      {blocks.length === 0 ? (
        <p className="home-empty">{empty}</p>
      ) : (
        <ol className="class-list">
          {blocks.map((b) => (
            <li key={`${b.date}-${b.start}`}>
              <ClassCard block={b} timeState={isToday ? stateOf(b, nowMin, upcoming?.start ?? null) : undefined} onOpen={onOpen} />
            </li>
          ))}
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
        className="unit-band"
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={unit.totalSessions}
        aria-valuenow={unit.approvedSessions}
        aria-label="Sesiones aprobadas"
      >
        {Array.from({ length: unit.totalSessions }, (_, i) => (
          <Tocapu
            key={i}
            motif={BAND_MOTIFS[i % BAND_MOTIFS.length]}
            dye="maiz"
            weave={i < unit.approvedSessions ? 'woven' : 'drawn'}
            size={22}
          />
        ))}
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

function WeekGrid({ week, onOpen }: { week: Week; onOpen: (block: WeekBlock) => void }) {
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
                      {b && !b.mine && (
                        <>
                          <span className="desk-cell-area">{b.area}</span>
                          <span className="desk-cell-sub">otro docente</span>
                        </>
                      )}
                      {b && b.mine && (
                        <button className="desk-cell-btn" onClick={() => onOpen(b)}>
                          <AreaKnot block={b} size={16} />
                          <span className="desk-cell-area">{b.area}</span>
                          <span className={`desk-cell-sub is-${prepOf(b).tone}`}>{prepOf(b).label}</span>
                        </button>
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
      <Chain size={32} hints />
    </section>
  )
}

export default function Home({ userName, onTakeAttendance, onOpenUnit, onOpenClosure, onOpenClass, onPrepareClass }: Props) {
  const [sections, setSections] = useState<Section[]>([])
  const [current, setCurrent] = useState<Section | null>(null)
  const [week, setWeek] = useState<Week | null>(null)
  // La semana siguiente solo se pide si hoy ya no quedan días con clases (p. ej. viernes).
  const [nextWeek, setNextWeek] = useState<Week | null>(null)
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
      .then((w) => {
        if (!alive) return
        setWeek(w)
        const laterThisWeek = w.blocks.some((b) => b.date > w.today)
        if (!laterThisWeek) {
          getWeek(current.id, addDays(w.weekStart, 7))
            .then((n) => alive && setNextWeek(n))
            .catch(() => alive && setNextWeek(null))
        }
      })
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

  const todayBlocks = week ? week.blocks.filter((b) => b.date === week.today) : []
  const nextDate = week
    ? [...week.blocks, ...(nextWeek?.blocks ?? [])].map((b) => b.date).filter((d) => d > week.today).sort()[0]
    : undefined
  const nextBlocks = nextDate ? [...week!.blocks, ...(nextWeek?.blocks ?? [])].filter((b) => b.date === nextDate) : []

  function openClass(block: WeekBlock) {
    if (!current) return
    const info = { slotId: block.slotId, date: block.date, start: block.start, end: block.end, area: block.area }
    if (block.session) onOpenClass(current, info, block.session.id)
    else onPrepareClass(current, block.slotId, block.date)
  }

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
                setNextWeek(null)
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
            <div className="desk-days">
              <DayClasses
                title={`Hoy en ${sectionLabel(current)}`}
                blocks={todayBlocks}
                isToday
                empty="Hoy no hay clases en el horario de esta sección."
                onOpen={openClass}
                action={
                  <button className="home-take desk-take" onClick={() => onTakeAttendance(current)}>
                    Tomar asistencia
                  </button>
                }
              />
              <DayClasses
                title={nextDate ? `Para preparar: ${longDate(nextDate)}` : 'Próximas clases'}
                blocks={nextBlocks}
                isToday={false}
                empty="No hay más clases en el horario cercano."
                onOpen={openClass}
              />
            </div>
            <div className="unit-side">
              <UnitCard week={week} onOpen={onOpenUnit} />
              {closure && <ClosureCard closure={closure} onOpen={() => onOpenClosure(current)} />}
            </div>
          </div>
          <WeekGrid week={week} onOpen={openClass} />
        </>
      )}

      <ChainTrack />
    </div>
  )
}
