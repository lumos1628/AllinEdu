import { useEffect, useMemo, useState } from 'react'
import { getClosure, saveMark } from '../services/api'
import type { Closure, ClosureMark, Level, Section } from '../services/api'

interface Props {
  section: Section
  onBack: () => void
}

const LEVELS: { code: Level; label: string }[] = [
  { code: 'AD', label: 'Logro destacado' },
  { code: 'A', label: 'Logro esperado' },
  { code: 'B', label: 'En proceso' },
  { code: 'C', label: 'En inicio' },
]

const needsText = (level: Level | null) => level === 'B' || level === 'C'

function shortDate(iso: string): string {
  const [y, m, d] = iso.split('-').map(Number)
  return new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'short' }).format(new Date(y, m - 1, d))
}

interface Cell {
  enrollmentId: number
  competencyId: number
}

function markOf(closure: Closure, cell: Cell): ClosureMark | undefined {
  return closure.students
    .find((s) => s.enrollmentId === cell.enrollmentId)
    ?.marks.find((m) => m.competencyId === cell.competencyId)
}

function firstPending(closure: Closure, after?: Cell): Cell | null {
  const cells: Cell[] = closure.students.flatMap((s) =>
    s.marks.filter((m) => m.missingConclusion).map((m) => ({ enrollmentId: s.enrollmentId, competencyId: m.competencyId })),
  )
  if (cells.length === 0) return null
  if (!after) return cells[0]
  return cells.find((c) => c.enrollmentId !== after.enrollmentId || c.competencyId !== after.competencyId) ?? null
}

export default function ClosureView({ section, onBack }: Props) {
  const [closure, setClosure] = useState<Closure | null>(null)
  const [loadError, setLoadError] = useState('')
  const [cell, setCell] = useState<Cell | null>(null)
  const [level, setLevel] = useState<Level | null>(null)
  const [conclusion, setConclusion] = useState('')
  const [recommendation, setRecommendation] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const [onlyPending, setOnlyPending] = useState(false)

  function select(c: Closure, next: Cell | null) {
    setCell(next)
    setSaveError('')
    const m = next ? markOf(c, next) : undefined
    setLevel(m?.level ?? null)
    setConclusion(m?.conclusion ?? '')
    setRecommendation(m?.recommendation ?? '')
  }

  useEffect(() => {
    getClosure(section.id)
      .then((c) => {
        setClosure(c)
        const start = firstPending(c) ??
          (c.students[0] && c.competencies[0]
            ? { enrollmentId: c.students[0].enrollmentId, competencyId: c.competencies[0].id }
            : null)
        select(c, start)
      })
      .catch(() => setLoadError('No hay un bimestre abierto o no se pudo cargar el cierre.'))
  }, [section.id])

  const current = useMemo(() => {
    if (!closure || !cell) return null
    const student = closure.students.find((s) => s.enrollmentId === cell.enrollmentId)
    const competency = closure.competencies.find((c) => c.id === cell.competencyId)
    return student && competency ? { student, competency, mark: markOf(closure, cell) } : null
  }, [closure, cell])

  async function handleSave() {
    if (!closure || !cell || !level) return
    setSaving(true)
    setSaveError('')
    try {
      const updated = await saveMark(section.id, {
        enrollmentId: cell.enrollmentId,
        competencyId: cell.competencyId,
        level,
        conclusion,
        recommendation,
      })
      setClosure(updated)
      select(updated, firstPending(updated, cell) ?? cell)
    } catch (e) {
      setSaveError(e instanceof Error ? e.message : 'No se pudo guardar')
    } finally {
      setSaving(false)
    }
  }

  const students = closure?.students.filter((s) => !onlyPending || s.marks.some((m) => m.missingConclusion)) ?? []
  const textMissing = needsText(level) && (!conclusion.trim() || !recommendation.trim())
  const twoInC = level === 'C' && current?.mark?.previousLevel === 'C'

  return (
    <div className="home">
      <button className="link back-link" onClick={onBack}>← Inicio</button>

      {loadError && <p className="error">{loadError}</p>}
      {!closure && !loadError && <p className="muted">Cargando el cierre…</p>}

      {closure && (
        <>
          <div className="doc-hero closure-hero">
            <div>
              <p className="home-greet">
                {section.grado} {section.nombre} · del {shortDate(closure.startDate)} al {shortDate(closure.endDate)}
              </p>
              <h1 className="doc-title">Cierre del {closure.periodName}</h1>
              <p className="closure-rule">
                La conclusión es obligatoria cuando el estudiante queda en <strong>B</strong> o <strong>C</strong>. En A y AD,
                solo si quieres.
              </p>
            </div>
            <div className="closure-count">
              <span className="closure-count-num">{closure.pendingConclusions}</span>
              <span>{closure.pendingConclusions === 1 ? 'conclusión por escribir' : 'conclusiones por escribir'}</span>
            </div>
          </div>

          {closure.competencies.length === 0 ? (
            <p className="home-empty">Todavía no hay competencias para evaluar en este periodo.</p>
          ) : (
            <div className="closure-cols">
              <section aria-labelledby="closure-table-title">
                <div className="closure-table-head">
                  <h2 id="closure-table-title" className="home-label">Tu aula</h2>
                  <label className="closure-filter">
                    <input type="checkbox" checked={onlyPending} onChange={(e) => setOnlyPending(e.target.checked)} />
                    Solo los que faltan
                  </label>
                </div>
                <div className="desk-week-scroll">
                  <table className="closure-table">
                    <thead>
                      <tr>
                        <th scope="col">Estudiante</th>
                        {closure.competencies.map((c) => (
                          <th key={c.id} scope="col">{c.name}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {students.map((s) => (
                        <tr key={s.enrollmentId} className={cell?.enrollmentId === s.enrollmentId ? 'is-selected' : undefined}>
                          <th scope="row">{s.name}</th>
                          {closure.competencies.map((c) => {
                            const m = s.marks.find((x) => x.competencyId === c.id)
                            const selected = cell?.enrollmentId === s.enrollmentId && cell?.competencyId === c.id
                            return (
                              <td key={c.id}>
                                <button
                                  className={`level-chip level-${m?.level ?? 'none'}${m?.missingConclusion ? ' is-missing' : ''}${selected ? ' is-selected' : ''}`}
                                  onClick={() => select(closure, { enrollmentId: s.enrollmentId, competencyId: c.id })}
                                  aria-label={`${s.name}, ${c.name}: ${m ? `nivel ${m.level}` : 'sin nivel'}${m?.missingConclusion ? ', falta la conclusión' : ''}`}
                                >
                                  {m?.level ?? '—'}
                                </button>
                              </td>
                            )
                          })}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <p className="closure-legend">
                  <span className="level-chip is-missing legend-chip" aria-hidden="true">B</span> falta la conclusión ·{' '}
                  sin borde: ya está o no es obligatoria
                </p>
              </section>

              {current && (
                <aside className="closure-editor" aria-labelledby="closure-editor-title">
                  <p className="home-label">Escribiendo ahora</p>
                  <h2 id="closure-editor-title" className="closure-student">{current.student.name}</h2>
                  <p className="closure-comp">{current.competency.name}</p>

                  <fieldset className="closure-levels">
                    <legend>¿En qué nivel lo ves? Esto lo decides tú.</legend>
                    <div className="closure-level-row">
                      {LEVELS.map((l) => (
                        <button
                          key={l.code}
                          type="button"
                          className={`level-pick level-${l.code}${level === l.code ? ' is-on' : ''}`}
                          aria-pressed={level === l.code}
                          title={l.label}
                          onClick={() => setLevel(l.code)}
                        >
                          {l.code}
                        </button>
                      ))}
                    </div>
                    <p className="closure-hint">
                      {level ? LEVELS.find((l) => l.code === level)!.label : 'Elige un nivel para seguir.'}
                      {level && (needsText(level) ? ' · la conclusión es obligatoria' : ' · la conclusión es opcional')}
                    </p>
                  </fieldset>

                  {twoInC && (
                    <p className="closure-alert">
                      El periodo anterior también quedó en C. Explica por qué no hubo avance; no basta repetir lo mismo.
                    </p>
                  )}

                  <label className="closure-field">
                    <span>Conclusión descriptiva</span>
                    <span className="closure-field-hint">Qué logró y qué le cuesta todavía.</span>
                    <textarea
                      aria-label="Conclusión descriptiva"
                      value={conclusion}
                      onChange={(e) => setConclusion(e.target.value)}
                      rows={5}
                      aria-required={needsText(level)}
                    />
                  </label>

                  <label className="closure-field">
                    <span>Recomendación</span>
                    <span className="closure-field-hint">Qué hacer para que avance y los apoyos que le diste.</span>
                    <textarea
                      aria-label="Recomendación"
                      value={recommendation}
                      onChange={(e) => setRecommendation(e.target.value)}
                      rows={3}
                      aria-required={needsText(level)}
                    />
                  </label>

                  {saveError && <p className="error" role="alert">{saveError}</p>}

                  <button className="home-take closure-save" onClick={handleSave} disabled={!level || textMissing || saving}>
                    {saving ? 'Guardando…' : 'Guardar y pasar al siguiente'}
                  </button>
                  <p className="closure-hint closure-center">
                    {!level
                      ? 'Primero elige el nivel.'
                      : textMissing
                        ? 'Con B o C faltan la conclusión y la recomendación.'
                        : 'Lo que guardas queda registrado como tuyo.'}
                  </p>
                </aside>
              )}
            </div>
          )}
        </>
      )}
    </div>
  )
}
