import { useEffect, useState } from 'react'
import { getClassroom, getUnit, getUnitJob, prepareUnit, saveClassroom } from '../services/api'
import type { DraftJob, UnitDetail } from '../services/api'
import Tocapu from './Tocapu'
import Weaving from './Weaving'
import { BAND_MOTIFS } from './tejido'

interface Props {
  unitId: number
  onBack: () => void
  onOpenSession: (sessionId: number) => void
}

const POLL_MS = 2500

function shortDate(iso: string | null): string {
  if (!iso) return ''
  const [y, m, d] = iso.split('-').map(Number)
  return new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'long' }).format(new Date(y, m - 1, d))
}

const plural = (n: number, one: string, many: string) => `${n} ${n === 1 ? one : many}`

// El telar (USE_CASES.md UC-01): todas las sesiones que faltan se preparan de una vez, en
// segundo plano, y la faja de la unidad se va tejiendo mientras llegan.
function Loom({ job, busy, error, onPrepare }: { job: DraftJob | null; busy: boolean; error: string; onPrepare: () => void }) {
  if (!job) return null
  if (job.running) {
    const at = Math.min(job.done + job.failed + 1, job.total)
    return (
      <section className="loom is-running" aria-labelledby="loom-title">
        <h2 id="loom-title" className="sr-only">Preparando la unidad</h2>
        <Weaving
          label={`Tejiendo la sesión ${at} de ${job.total}…`}
          sub="Puedes cerrar esta pantalla: siguen llegando como borradores para que las revises."
        />
      </section>
    )
  }
  const finished = job.total > 0
  // Sin nada que preparar ni nada recién preparado, la franja no ocupa lugar.
  if (job.pending === 0 && !finished) return null
  return (
    <section className="loom" aria-labelledby="loom-title">
      <div className="loom-text">
        <h2 id="loom-title" className="loom-title">
          {job.pending > 0 ? plural(job.pending, 'sesión sin preparar', 'sesiones sin preparar') : 'La unidad está tejida'}
        </h2>
        {finished && (
          <p className="loom-done">
            {job.done > 0 && `Listo: ${plural(job.done, 'borrador nuevo', 'borradores nuevos')} para revisar.`}
            {job.failed > 0 && ` ${plural(job.failed, 'no se pudo', 'no se pudieron')} preparar; vuelve a intentarlo.`}
          </p>
        )}
        {job.pending > 0 && (
          <p className="loom-lead">
            Las preparo todas de una vez, en orden y siguiendo el hilo de la unidad. Llegan como borradores con unas
            pocas preguntas para ti; nada se aprueba sin ti.
          </p>
        )}
        {error && <p className="error" role="alert">{error}</p>}
      </div>
      {job.pending > 0 && (
        <>
          <button className="home-take" onClick={onPrepare} disabled={busy}>
            {busy ? 'Empezando…' : job.pending === 1 ? 'Preparar la sesión' : `Preparar las ${job.pending} sesiones`}
          </button>
        </>
      )}
    </section>
  )
}

// "Mi aula" (UC-22): lo que el docente cuenta una vez y usan todos sus borradores.
function Classroom() {
  const [text, setText] = useState<string | null>(null)
  const [saved, setSaved] = useState('')
  const [status, setStatus] = useState('')

  useEffect(() => {
    getClassroom()
      .then((t) => {
        setText(t)
        setSaved(t)
      })
      .catch(() => setText(''))
  }, [])

  async function handleSave() {
    if (text === null) return
    setStatus('Guardando…')
    try {
      const t = await saveClassroom(text)
      setSaved(t)
      setStatus('Guardado. Lo usan tus próximos borradores.')
    } catch (e) {
      setStatus(e instanceof Error ? e.message : 'No se pudo guardar')
    }
  }

  return (
    <section className="desk-unit classroom" aria-labelledby="classroom-title">
      <h2 id="classroom-title" className="home-label">Mi aula</h2>
      <p className="classroom-lead">Lo que los borradores saben de tu aula y tu comunidad.</p>
      <label className="closure-field">
        <span className="sr-only">Cómo es tu aula y tu comunidad</span>
        <span className="closure-field-hint">
          Cuéntalo como a una colega: qué hay cerca de la escuela, qué materiales tienen, cómo trabajan. No escribas
          nombres de estudiantes.
        </span>
        <textarea
          value={text ?? ''}
          onChange={(e) => {
            setText(e.target.value)
            setStatus('')
          }}
          rows={5}
          maxLength={2000}
          disabled={text === null}
          aria-label="Cómo es tu aula y tu comunidad"
          placeholder="Ej.: Estamos junto al río; los domingos hay feria. Tenemos regletas, chapitas y la balanza del aula."
        />
      </label>
      <div className="classroom-actions">
        <button className="editor-cancel" onClick={handleSave} disabled={text === null || text === saved}>
          Guardar
        </button>
        {status && <span className="muted" role="status">{status}</span>}
      </div>
    </section>
  )
}

export default function UnitView({ unitId, onBack, onOpenSession }: Props) {
  const [unit, setUnit] = useState<UnitDetail | null>(null)
  const [error, setError] = useState('')
  const [job, setJob] = useState<DraftJob | null>(null)
  const [starting, setStarting] = useState(false)
  const [jobError, setJobError] = useState('')

  useEffect(() => {
    getUnit(unitId)
      .then(setUnit)
      .catch(() => setError('No se pudo cargar la unidad.'))
    getUnitJob(unitId)
      .then(setJob)
      .catch(() => setJob(null))
  }, [unitId])

  // Mientras se teje, se pregunta cada pocos segundos y la lista se actualiza con lo que llegó.
  const running = job?.running ?? false
  useEffect(() => {
    if (!running) return
    let alive = true
    const timer = window.setInterval(async () => {
      try {
        const next = await getUnitJob(unitId)
        if (!alive) return
        setJob(next)
        setUnit(await getUnit(unitId))
      } catch {
        // Un fallo de red no corta el telar: se vuelve a preguntar en el siguiente turno.
      }
    }, POLL_MS)
    return () => {
      alive = false
      window.clearInterval(timer)
    }
  }, [running, unitId])

  async function handlePrepare() {
    setStarting(true)
    setJobError('')
    try {
      setJob(await prepareUnit(unitId))
    } catch (e) {
      setJobError(e instanceof Error ? e.message : 'No se pudo empezar')
    } finally {
      setStarting(false)
    }
  }

  const approved = unit?.sessions.filter((s) => s.approved).length ?? 0
  const questions = unit?.sessions.reduce((n, s) => n + s.openKnots, 0) ?? 0

  return (
    <div className="home">
      <button className="link back-link" onClick={onBack}>← Inicio</button>

      {error && <p className="error">{error}</p>}
      {!unit && !error && <p className="muted">Cargando la unidad…</p>}

      {unit && (
        <>
          <div className="doc-hero">
            <p className="home-greet">
              Del {shortDate(unit.startDate)} al {shortDate(unit.endDate)}, en {unit.sessions.length} sesiones
            </p>
            <h1 className="doc-title">{unit.title}</h1>
          </div>

          <Loom job={job} busy={starting} error={jobError} onPrepare={handlePrepare} />

          <div className="desk-top">
            <section aria-labelledby="unit-sessions-title">
              <h2 id="unit-sessions-title" className="home-label">
                Sesiones: {approved} de {unit.sessions.length} aprobadas por ti
                {questions > 0 && `, ${plural(questions, 'pregunta', 'preguntas')} por responder`}
              </h2>
              <ol className="unit-sessions">
                {unit.sessions.map((s, i) => {
                  const weaving = job?.running && job.currentSessionId === s.id
                  return (
                    <li key={s.id}>
                      <button className={weaving ? 'unit-session is-weaving' : 'unit-session'} onClick={() => onOpenSession(s.id)}>
                        <Tocapu
                          motif={BAND_MOTIFS[i % BAND_MOTIFS.length]}
                          dye="maiz"
                          weave={s.approved || weaving ? 'woven' : s.activities > 0 ? 'drawn' : 'bare'}
                          size={28}
                        />
                        <span className="unit-session-num">{s.number}</span>
                        <span className="unit-session-text">
                          <span className="unit-session-title">{s.title}</span>
                          <span className="unit-session-sub">
                            {s.competencies.join(' · ')}
                            {weaving ? ' · tejiendo ahora…' : s.activities === 0 && ' · sin secuencia todavía'}
                          </span>
                        </span>
                        <span className="unit-session-side">
                          <span className={s.approved ? 'status-dot is-ok' : 'status-dot is-draft'}>
                            {s.approved ? 'Aprobada' : 'Borrador'}
                          </span>
                          {s.openKnots > 0 && !s.approved && (
                            <span className="unit-session-knots">{plural(s.openKnots, 'pregunta', 'preguntas')}</span>
                          )}
                        </span>
                      </button>
                    </li>
                  )
                })}
              </ol>
            </section>

            <aside className="unit-side">
              {unit.situation && (
                <section className="unit-situation" aria-labelledby="unit-situation-title">
                  <h2 id="unit-situation-title" className="home-label">La situación de donde sale todo</h2>
                  <p>{unit.situation}</p>
                </section>
              )}

              <Classroom />

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
