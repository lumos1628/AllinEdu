import { useState } from 'react'
import { answerKnot } from '../services/api'
import type { Knot, SessionDetail } from '../services/api'
import Tocapu from './Tocapu'

interface Props {
  session: SessionDetail
  onChange: (session: SessionDetail) => void
}

// Lo que se le sugiere escribir al pedir un cambio, según de dónde sale el nudo.
const CHANGE_HINT: Record<Knot['type'], string> = {
  SUPUESTO: '¿Qué hay en tu comunidad en su lugar? Por ejemplo: una feria los domingos',
  MINUTOS: 'Opcional: qué acortar o alargar',
  PASOS: 'Opcional: cómo lo quieres',
  CRITERIOS: 'Opcional: cómo lo quieres',
  MATERIAL: 'Opcional: qué material tienes',
}

// Revisión por excepción (USE_CASES.md UC-04): el docente contesta unas pocas preguntas
// en vez de leer y corregir el documento completo. El nudo es un aspa: dos hilos cruzados.
export default function KnotList({ session, onChange }: Props) {
  if (session.knots.length === 0) return null
  const open = session.knots.filter((k) => k.state === 'PENDIENTE').length
  return (
    <section className="knots" aria-labelledby="knots-title">
      <h2 id="knots-title" className="knots-title">
        {open === 0
          ? 'Respondiste todas las preguntas'
          : open === 1
            ? 'Antes de aprobar, una pregunta'
            : `Antes de aprobar, ${open} preguntas`}
      </h2>
      <p className="knots-sub">
        Es lo único que necesita tu mirada. Lo demás ya pasó las reglas de tu institución.
      </p>
      <ol className="knot-list">
        {session.knots.map((k) => (
          <KnotItem key={k.id} sessionId={session.id} knot={k} onChange={onChange} />
        ))}
      </ol>
    </section>
  )
}

function KnotItem({ sessionId, knot, onChange }: { sessionId: number; knot: Knot; onChange: (s: SessionDetail) => void }) {
  const answered = knot.state !== 'PENDIENTE'
  const [editing, setEditing] = useState(!answered)
  const [changing, setChanging] = useState(false)
  const [text, setText] = useState(knot.answer ?? '')
  const [remember, setRemember] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function send(answer: 'ACEPTADO' | 'CAMBIO') {
    setBusy(true)
    setError('')
    try {
      onChange(await answerKnot(sessionId, knot.id, answer, answer === 'CAMBIO' ? text : '', remember))
      setEditing(false)
      setChanging(false)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo guardar tu respuesta')
    } finally {
      setBusy(false)
    }
  }

  return (
    <li className={answered && !editing ? 'knot is-answered' : 'knot'}>
      <Tocapu motif="aspa" dye="maiz" weave={answered ? 'woven' : 'drawn'} size={22} className="knot-glyph" />
      <div className="knot-body">
        <p className="knot-question">{knot.question}</p>
        {knot.fragment && <p className="knot-fragment">«{knot.fragment}»</p>}

        {answered && !editing && (
          <p className="knot-answer">
            {knot.state === 'ACEPTADO' ? 'Dijiste que está bien.' : `Pediste cambiarlo${knot.answer ? `: ${knot.answer}` : '.'}`}{' '}
            <button type="button" className="link" onClick={() => setEditing(true)}>
              Cambiar respuesta
            </button>
          </p>
        )}

        {editing && !changing && (
          <div className="knot-actions">
            <button type="button" className="knot-ok" onClick={() => send('ACEPTADO')} disabled={busy}>
              Está bien
            </button>
            <button type="button" className="knot-change" onClick={() => setChanging(true)} disabled={busy}>
              Cámbialo
            </button>
          </div>
        )}

        {editing && changing && (
          <div className="knot-form">
            <label className="knot-field">
              <span className="sr-only">Qué quieres en su lugar</span>
              <textarea
                value={text}
                onChange={(e) => setText(e.target.value)}
                rows={2}
                maxLength={500}
                placeholder={CHANGE_HINT[knot.type]}
              />
            </label>
            {knot.type === 'SUPUESTO' && (
              <label className="knot-remember">
                <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
                Recordarlo en «Mi aula» para las próximas sesiones
              </label>
            )}
            <div className="knot-actions">
              <button
                type="button"
                className="knot-ok"
                onClick={() => send('CAMBIO')}
                disabled={busy || (knot.type === 'SUPUESTO' && !text.trim())}
              >
                {busy ? 'Guardando…' : 'Guardar'}
              </button>
              <button type="button" className="link" onClick={() => setChanging(false)} disabled={busy}>
                Volver
              </button>
            </div>
          </div>
        )}
        {error && <p className="error" role="alert">{error}</p>}
      </div>
    </li>
  )
}
