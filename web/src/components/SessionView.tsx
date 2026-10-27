import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { approveSession, generateDraft, getSession } from '../services/api'
import type { SessionDetail } from '../services/api'
import SessionEditor from './SessionEditor'
import KnotList from './KnotList'
import Weaving from './Weaving'

interface Props {
  sessionId: number
  backLabel: string
  onBack: () => void
}

const MOMENTS: { key: string; label: string }[] = [
  { key: 'INICIO', label: 'Inicio' },
  { key: 'DESARROLLO', label: 'Desarrollo' },
  { key: 'CIERRE', label: 'Cierre' },
  { key: 'OTRO', label: 'Otros momentos' },
]

const INSTRUMENT_TYPE: Record<string, string> = {
  ESCALA_VALORACION: 'Escala valorativa',
  LISTA_COTEJO: 'Lista de cotejo',
  RUBRICA: 'Rúbrica',
  REGISTRO: 'Registro',
  PORTAFOLIO: 'Portafolio',
}

const EVIDENCE_FORMAT: Record<string, string> = {
  ORAL: 'oral',
  ESCRITA: 'escrita',
  OBSERVACION: 'por observación',
  ARCHIVO: 'archivo',
  PRODUCTO: 'producto',
}

function editedLabel(iso: string): string {
  return new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'long', hour: 'numeric', minute: '2-digit' }).format(
    new Date(iso),
  )
}

function Empty({ children }: { children: string }) {
  return <p className="doc-empty">{children}</p>
}

// Señala dentro del texto la frase que causa un nudo todavía sin responder.
function marked(text: string, marks: string[]): ReactNode {
  const mark = marks.find((m) => text.includes(m))
  if (!mark) return text
  const at = text.indexOf(mark)
  return (
    <>
      {text.slice(0, at)}
      <mark className="knot-mark">{mark}</mark>
      {marked(text.slice(at + mark.length), marks)}
    </>
  )
}

function Sequence({ session }: { session: SessionDetail }) {
  const marks = session.knots.filter((k) => k.state === 'PENDIENTE' && k.fragment).map((k) => k.fragment!)
  if (session.activities.length === 0) return <Empty>Esta sesión todavía no tiene secuencia didáctica.</Empty>
  const total = session.activities.reduce((sum, a) => sum + (a.minutes ?? 0), 0)
  return (
    <>
      {MOMENTS.map(({ key, label }) => {
        const acts = session.activities.filter((a) => (a.moment ?? 'OTRO') === key)
        if (acts.length === 0) return null
        const minutes = acts.reduce((sum, a) => sum + (a.minutes ?? 0), 0)
        return (
          <div key={key} className="doc-moment">
            <h4 className="doc-moment-title">
              {label}
              {minutes > 0 && <span> · {minutes} min</span>}
            </h4>
            <ol className="doc-steps">
              {acts.map((a) => (
                <li key={a.id}>
                  {a.generated && <span className="doc-tag is-draft">Propuesta, sin revisar</span>}
                  {a.title && <strong>{a.title}. </strong>}
                  {marked(a.instructions, marks)}
                </li>
              ))}
            </ol>
          </div>
        )
      })}
      {session.durationMinutes != null && total > 0 && total !== session.durationMinutes && (
        <p className="doc-warn">
          Las actividades suman <strong>{total} minutos</strong> y la sesión dura {session.durationMinutes}.
        </p>
      )}
    </>
  )
}

export default function SessionView({ sessionId, backLabel, onBack }: Props) {
  const [session, setSession] = useState<SessionDetail | null>(null)
  const [error, setError] = useState('')
  const [approving, setApproving] = useState(false)
  const [editing, setEditing] = useState(false)
  const [savedNote, setSavedNote] = useState('')
  const [generating, setGenerating] = useState(false)
  const [draftError, setDraftError] = useState('')
  const [asking, setAsking] = useState(false)
  const [indications, setIndications] = useState('')

  useEffect(() => {
    getSession(sessionId)
      .then(setSession)
      .catch(() => setError('No se pudo cargar la sesión.'))
  }, [sessionId])

  async function handleGenerate(extra?: string) {
    setGenerating(true)
    setDraftError('')
    try {
      setSession(await generateDraft(sessionId, extra))
      setSavedNote('')
      setAsking(false)
      setIndications('')
    } catch (e) {
      setDraftError(e instanceof Error ? e.message : 'No se pudo preparar el borrador')
    } finally {
      setGenerating(false)
    }
  }

  async function handleApprove() {
    setApproving(true)
    setError('')
    try {
      setSession(await approveSession(sessionId))
    } catch {
      setError('No se pudo aprobar la sesión. Inténtalo de nuevo.')
    } finally {
      setApproving(false)
    }
  }

  const pendingReview = session?.activities.filter((a) => a.generated).length ?? 0
  const openKnots = session?.knots.filter((k) => k.state === 'PENDIENTE').length ?? 0
  const changes = session?.knots.filter((k) => k.state === 'CAMBIO').length ?? 0

  return (
    <div className="home">
      <button className="link back-link" onClick={onBack}>← {backLabel}</button>

      {error && <p className="error">{error}</p>}
      {!session && !error && <p className="muted">Cargando la sesión…</p>}

      {session && (
        <>
          <div className="doc-hero">
            <p className="home-greet">
              Sesión {session.number} de «{session.unitTitle}»
              {session.durationMinutes ? `, ${session.durationMinutes} minutos` : ''}
            </p>
            <h1 className="doc-title">{session.title}</h1>
          </div>

          {editing ? (
            <SessionEditor
              session={session}
              onCancel={() => setEditing(false)}
              onSaved={(saved) => {
                setSession(saved)
                setEditing(false)
                setSavedNote('Cambios guardados. Quedan registrados como tuyos.')
              }}
            />
          ) : (
          <>
          <div className="doc-toolbar">
            <span className="doc-meta">
              {session.approved && <span className="seal">Aprobada por ti</span>}
              {session.editedAt && <span>Editada el {editedLabel(session.editedAt)}</span>}
              {savedNote && <span className="ok" role="status">{savedNote}</span>}
            </span>
            <span className="doc-toolbar-actions">
              {session.regenerable && session.activities.length > 0 && !generating && (
                <button className="editor-cancel" onClick={() => setAsking((v) => !v)} aria-expanded={asking}>
                  Pedir otro borrador
                </button>
              )}
              <button className="editor-cancel" onClick={() => { setSavedNote(''); setEditing(true) }}>
                Editar la sesión
              </button>
            </span>
          </div>

          {generating && (
            <Weaving
              label={session.activities.length ? 'Tejiendo otro borrador…' : 'Tejiendo el borrador…'}
              sub="Lo reviso con las reglas de tu institución antes de mostrártelo. Tarda unos segundos."
            />
          )}

          {!generating && session.activities.length === 0 && (
            <div className="doc-generate">
              <div>
                <p className="doc-draft-title">Esta sesión todavía no tiene secuencia</p>
                <p className="doc-draft-text">
                  Te propongo la sesión completa: secuencia, DUA y materiales, a partir de la unidad, los desempeños
                  del grado, los pasos de tu institución y lo que me contaste de tu aula. Llega como borrador, con unas
                  pocas preguntas para ti.
                </p>
                {draftError && <p className="error" role="alert">{draftError}</p>}
              </div>
              <button className="home-take" onClick={() => handleGenerate()}>
                Proponer un borrador
              </button>
            </div>
          )}

          {!generating && asking && (
            <div className="doc-ask">
              <label className="closure-field">
                <span>¿Qué quieres distinto?</span>
                <span className="closure-field-hint">
                  Con tus palabras: «más corta», «sin salir del aula», «con chapitas en vez de tiras».
                </span>
                <textarea value={indications} onChange={(e) => setIndications(e.target.value)} rows={2} maxLength={1000} />
              </label>
              {draftError && <p className="error" role="alert">{draftError}</p>}
              <button className="home-take" onClick={() => handleGenerate(indications)} disabled={!indications.trim()}>
                Tejer otro borrador
              </button>
            </div>
          )}

          {!generating && !session.approved && session.activities.length > 0 && (
            <div className="doc-draft" role="status">
              <div>
                <p className="doc-draft-title">
                  {session.generated && !session.editedAt ? 'Es un borrador que propuse yo' : 'Es un borrador'}
                </p>
                <p className="doc-draft-text">
                  No llega a tus estudiantes ni se imprime hasta que lo apruebes.
                  {openKnots > 0
                    ? ` Responde ${openKnots === 1 ? 'la pregunta de abajo' : `las ${openKnots} preguntas de abajo`} y apruébalo cuando te convenza.`
                    : pendingReview > 0
                      ? ' No quedan preguntas: pasó las reglas de tu institución. Dale una mirada y apruébalo si te convence.'
                      : ''}
                </p>
              </div>
              <button className="home-take" onClick={handleApprove} disabled={approving || generating}>
                {approving ? 'Aprobando…' : 'Aprobar la sesión'}
              </button>
            </div>
          )}

          {!generating && <KnotList session={session} onChange={setSession} />}

          {!generating && changes > 0 && session.regenerable && (
            <div className="knot-redo">
              <p>
                Pediste {changes === 1 ? 'un cambio' : `${changes} cambios`}. Rehago el borrador con tus respuestas;
                lo que ya estaba bien se mantiene.
              </p>
              {draftError && <p className="error" role="alert">{draftError}</p>}
              <button className="home-take" onClick={() => handleGenerate()}>
                Rehacer con tus respuestas
              </button>
            </div>
          )}

          <article className="hoja" aria-label="Documento de la sesión">
            <section className="doc-block">
              <h3 className="doc-block-title">I. Propósitos de aprendizaje y evidencias</h3>
              <div className="doc-grid">
                <div>
                  <p className="doc-field">Competencia</p>
                  {session.competencies.length ? (
                    session.competencies.map((c) => <p key={c} className="doc-text"><strong>{c}</strong></p>)
                  ) : (
                    <Empty>Sin competencia asignada.</Empty>
                  )}
                  {session.purpose && (
                    <>
                      <p className="doc-field">Propósito</p>
                      <p className="doc-text">{session.purpose}</p>
                    </>
                  )}
                </div>
                <div>
                  <p className="doc-field">Criterios de evaluación</p>
                  {session.criteria.length ? (
                    <ol className="doc-list">
                      {session.criteria.map((c) => <li key={c}>{c}</li>)}
                    </ol>
                  ) : (
                    <Empty>Todavía no tiene criterios.</Empty>
                  )}
                </div>
              </div>
              <div className="doc-grid">
                <div>
                  <p className="doc-field">Evidencia de aprendizaje</p>
                  {session.evidences.length ? (
                    session.evidences.map((e) => (
                      <p key={e.description} className="doc-text">
                        {e.description}
                        {e.format && EVIDENCE_FORMAT[e.format] && <span className="doc-soft"> ({EVIDENCE_FORMAT[e.format]})</span>}
                      </p>
                    ))
                  ) : (
                    <Empty>Todavía no tiene evidencia prevista.</Empty>
                  )}
                </div>
                <div>
                  <p className="doc-field">Instrumento de evaluación</p>
                  {session.instrument ? (
                    <p className="doc-text">
                      {INSTRUMENT_TYPE[session.instrument.type] ?? session.instrument.type} · ver bloque IV
                    </p>
                  ) : (
                    <Empty>Todavía no tiene instrumento.</Empty>
                  )}
                </div>
              </div>
            </section>

            <section className="doc-block">
              <h3 className="doc-block-title">II. Preparación de la sesión</h3>
              <p className="doc-field">DUA aplicado</p>
              {session.dua.length ? (
                session.dua.map((n) => (
                  <p key={n.title} className="doc-text">
                    {n.generated && <span className="doc-tag is-draft">Propuesta</span>}
                    <strong>{n.title}.</strong> {n.text}
                  </p>
                ))
              ) : (
                <Empty>Todavía no tiene pautas DUA.</Empty>
              )}
              <div className="doc-grid">
                <div>
                  <p className="doc-field">Materiales y recursos</p>
                  {session.materials.length ? (
                    <ul className="doc-materials">
                      {session.materials.map((m) => (
                        <li key={m.name}>
                          <span className={m.required ? 'doc-tag is-required' : 'doc-tag'}>
                            {m.required ? 'Obligatorio' : 'Sugerido'}
                          </span>
                          {m.generated && <span className="doc-tag is-draft">Propuesta</span>}
                          {m.name}
                          {m.quantity ? <span className="doc-soft"> · {m.quantity}</span> : null}
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <Empty>Todavía no tiene materiales.</Empty>
                  )}
                </div>
                <div>
                  <p className="doc-field">Enfoque transversal</p>
                  {session.approaches.length ? (
                    session.approaches.map((n) => (
                      <p key={n.title} className="doc-text"><strong>{n.title}.</strong> {n.text}</p>
                    ))
                  ) : (
                    <Empty>Todavía no tiene enfoque transversal.</Empty>
                  )}
                </div>
              </div>
            </section>

            <section className="doc-block">
              <h3 className="doc-block-title">III. Secuencia didáctica</h3>
              <Sequence session={session} />
            </section>

            <section className="doc-block">
              <h3 className="doc-block-title">
                IV. {session.instrument ? INSTRUMENT_TYPE[session.instrument.type] ?? 'Instrumento' : 'Instrumento'}
              </h3>
              {session.instrument ? (
                <div className="doc-table-wrap">
                  <table className="doc-table">
                    <thead>
                      <tr>
                        <th scope="col">Criterio</th>
                        {session.instrument.criteria[0]?.levels.map((l) => (
                          <th key={l.code} scope="col">{l.code}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {session.instrument.criteria.map((c) => (
                        <tr key={c.description}>
                          <th scope="row">{c.description}</th>
                          {c.levels.map((l) => (
                            <td key={l.code}>{l.description}</td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <Empty>Todavía no tiene instrumento.</Empty>
              )}
            </section>
          </article>
          </>
          )}
        </>
      )}
    </div>
  )
}
