import { useState } from 'react'
import { updateSession } from '../services/api'
import type { Moment, SessionDetail } from '../services/api'

interface Props {
  session: SessionDetail
  onSaved: (session: SessionDetail) => void
  onCancel: () => void
}

interface DraftActivity {
  key: string
  id: number | null
  moment: Moment
  title: string
  instructions: string
  minutes: string
  generated: boolean
}

const MOMENTS: { key: Moment; label: string; add: string }[] = [
  { key: 'INICIO', label: 'Inicio', add: 'Agregar al inicio' },
  { key: 'DESARROLLO', label: 'Desarrollo', add: 'Agregar al desarrollo' },
  { key: 'CIERRE', label: 'Cierre', add: 'Agregar al cierre' },
]

let nextKey = 0
const newKey = () => `n${nextKey++}`

const toMinutes = (v: string) => (v.trim() === '' ? null : Number(v))

export default function SessionEditor({ session, onSaved, onCancel }: Props) {
  const [title, setTitle] = useState(session.title)
  const [purpose, setPurpose] = useState(session.purpose ?? '')
  const [duration, setDuration] = useState(session.durationMinutes?.toString() ?? '')
  const [evidence, setEvidence] = useState(session.evidences[0]?.description ?? '')
  const [activities, setActivities] = useState<DraftActivity[]>(() =>
    session.activities.map((a) => ({
      key: `e${a.id}`,
      id: a.id,
      moment: a.moment && a.moment !== 'OTRO' ? a.moment : 'DESARROLLO',
      title: a.title ?? '',
      instructions: a.instructions,
      minutes: a.minutes?.toString() ?? '',
      generated: a.generated,
    })),
  )
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const total = activities.reduce((sum, a) => sum + (toMinutes(a.minutes) ?? 0), 0)
  const durationNum = toMinutes(duration)

  function update(key: string, patch: Partial<DraftActivity>) {
    setActivities((list) => list.map((a) => (a.key === key ? { ...a, ...patch, generated: false } : a)))
  }

  function remove(key: string) {
    setActivities((list) => list.filter((a) => a.key !== key))
  }

  // Mueve dentro del mismo momento: el orden entre momentos lo fija el momento.
  function move(key: string, dir: -1 | 1) {
    setActivities((list) => {
      const i = list.findIndex((a) => a.key === key)
      const moment = list[i].moment
      let j = i + dir
      while (j >= 0 && j < list.length && list[j].moment !== moment) j += dir
      if (j < 0 || j >= list.length) return list
      const copy = [...list]
      ;[copy[i], copy[j]] = [copy[j], copy[i]]
      return copy
    })
  }

  function add(moment: Moment) {
    setActivities((list) => [
      ...list,
      { key: newKey(), id: null, moment, title: '', instructions: '', minutes: '', generated: false },
    ])
  }

  async function handleSave() {
    setError('')
    if (!title.trim()) return setError('La sesión necesita un título.')
    if (activities.some((a) => !a.instructions.trim())) return setError('Cada actividad necesita su descripción.')
    setSaving(true)
    try {
      const ordered = MOMENTS.flatMap((m) => activities.filter((a) => a.moment === m.key))
      const saved = await updateSession(session.id, {
        title: title.trim(),
        purpose,
        durationMinutes: durationNum,
        evidence,
        activities: ordered.map((a) => ({
          id: a.id,
          moment: a.moment,
          title: a.title,
          instructions: a.instructions,
          minutes: toMinutes(a.minutes),
        })),
      })
      onSaved(saved)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo guardar la sesión')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="editor">
      <div className="hoja editor-sheet">
        <section className="doc-block">
          <h3 className="doc-block-title">Datos de la sesión</h3>
          <label className="editor-field">
            <span>Título</span>
            <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={240} />
          </label>
          <div className="doc-grid">
            <label className="editor-field">
              <span>Propósito</span>
              <textarea value={purpose} onChange={(e) => setPurpose(e.target.value)} rows={3} />
            </label>
            <label className="editor-field">
              <span>Evidencia de aprendizaje</span>
              <textarea value={evidence} onChange={(e) => setEvidence(e.target.value)} rows={3} />
            </label>
          </div>
          <label className="editor-field editor-short">
            <span>Duración (minutos)</span>
            <input type="number" min={1} inputMode="numeric" value={duration} onChange={(e) => setDuration(e.target.value)} />
          </label>
        </section>

        <section className="doc-block">
          <h3 className="doc-block-title">Secuencia didáctica</h3>
          <p className={durationNum && total !== durationNum ? 'editor-total is-off' : 'editor-total'} aria-live="polite">
            Las actividades suman <strong>{total} min</strong>
            {durationNum ? ` de ${durationNum}` : ''}
            {durationNum && total > durationNum ? ` · sobran ${total - durationNum}` : ''}
            {durationNum && total < durationNum && total > 0 ? ` · faltan ${durationNum - total}` : ''}
          </p>

          {MOMENTS.map((m) => {
            const list = activities.filter((a) => a.moment === m.key)
            return (
              <div key={m.key} className="doc-moment">
                <h4 className="doc-moment-title">{m.label}</h4>
                {list.length === 0 && <p className="doc-empty">Sin actividades.</p>}
                <ol className="editor-acts">
                  {list.map((a, i) => (
                    <li key={a.key} className="editor-act">
                      <div className="editor-act-row">
                        <label className="editor-field editor-grow">
                          <span>Nombre del paso (opcional)</span>
                          <input value={a.title} onChange={(e) => update(a.key, { title: e.target.value })} maxLength={180} />
                        </label>
                        <label className="editor-field editor-min">
                          <span>Min</span>
                          <input
                            type="number"
                            min={1}
                            inputMode="numeric"
                            value={a.minutes}
                            onChange={(e) => update(a.key, { minutes: e.target.value })}
                          />
                        </label>
                      </div>
                      <label className="editor-field">
                        <span>Qué se hace</span>
                        <textarea
                          value={a.instructions}
                          onChange={(e) => update(a.key, { instructions: e.target.value })}
                          rows={2}
                        />
                      </label>
                      <div className="editor-act-tools">
                        {a.generated && <span className="doc-tag is-draft">Propuesta, sin revisar</span>}
                        <button type="button" className="editor-tool" onClick={() => move(a.key, -1)} disabled={i === 0}>
                          Subir
                        </button>
                        <button
                          type="button"
                          className="editor-tool"
                          onClick={() => move(a.key, 1)}
                          disabled={i === list.length - 1}
                        >
                          Bajar
                        </button>
                        <button type="button" className="editor-tool is-danger" onClick={() => remove(a.key)}>
                          Quitar
                        </button>
                      </div>
                    </li>
                  ))}
                </ol>
                <button type="button" className="editor-add" onClick={() => add(m.key)}>
                  + {m.add}
                </button>
              </div>
            )
          })}
        </section>

        <p className="doc-empty">
          Competencias, criterios, DUA, materiales e instrumento vienen de la unidad y todavía no se editan aquí.
        </p>
      </div>

      <div className="editor-bar">
        {error && <p className="error" role="alert">{error}</p>}
        <div className="editor-bar-actions">
          <button type="button" className="home-take" onClick={handleSave} disabled={saving}>
            {saving ? 'Guardando…' : 'Guardar cambios'}
          </button>
          <button type="button" className="editor-cancel" onClick={onCancel} disabled={saving}>
            Cancelar
          </button>
        </div>
      </div>
    </div>
  )
}
