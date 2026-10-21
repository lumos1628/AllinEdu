import { useEffect, useState } from 'react'
import { getSectionStudents, getSession } from '../services/api'
import type { Section, SessionDetail, Student } from '../services/api'

interface Props {
  section: Section
  sessionId: number
  onBack: () => void
}

const INSTRUMENT_TYPE: Record<string, string> = {
  ESCALA_VALORACION: 'Escala valorativa',
  LISTA_COTEJO: 'Lista de cotejo',
  RUBRICA: 'Rúbrica',
}

// Hoja para imprimir y marcar en el aula. Por ahora no guarda marcas: registrar
// en clase es el siguiente eslabón ("Anotar en clase").
export default function InstrumentView({ section, sessionId, onBack }: Props) {
  const [session, setSession] = useState<SessionDetail | null>(null)
  const [students, setStudents] = useState<Student[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([getSession(sessionId), getSectionStudents(section.id)])
      .then(([s, list]) => {
        setSession(s)
        setStudents(list)
      })
      .catch(() => setError('No se pudo cargar el instrumento.'))
  }, [sessionId, section.id])

  const instrument = session?.instrument
  const levels = instrument?.criteria[0]?.levels ?? []

  return (
    <div className="home">
      <div className="no-print instrument-bar">
        <button className="link back-link" onClick={onBack}>← La clase</button>
        {instrument && (
          <button className="editor-cancel" onClick={() => window.print()}>
            Imprimir
          </button>
        )}
      </div>

      {error && <p className="error">{error}</p>}
      {!session && !error && <p className="muted">Cargando el instrumento…</p>}
      {session && !instrument && <p className="home-empty">Esta sesión todavía no tiene instrumento.</p>}

      {session && instrument && (
        <article className="hoja instrument-sheet">
          <p className="doc-field">{INSTRUMENT_TYPE[instrument.type] ?? instrument.name}</p>
          <h1 className="instrument-title">{session.title}</h1>
          <p className="doc-text doc-soft">
            {section.grado} {section.nombre} · {students.length} estudiantes · {session.competencies.join(' · ')}
          </p>

          <h2 className="doc-block-title instrument-sub">Lo que vas a observar</h2>
          <ol className="doc-list">
            {instrument.criteria.map((c) => (
              <li key={c.description}>{c.description}</li>
            ))}
          </ol>

          <h2 className="doc-block-title instrument-sub">Registro</h2>
          <p className="doc-text doc-soft">
            Marca un nivel por criterio:{' '}
            {levels.map((l, i) => (
              <span key={l.code}>
                <strong>{l.code}</strong> {l.description.replace(/\.$/, '').toLowerCase()}
                {i < levels.length - 1 ? ' · ' : '.'}
              </span>
            ))}
          </p>
          <div className="doc-table-wrap">
            <table className="doc-table instrument-table">
              <thead>
                <tr>
                  <th scope="col">N.º</th>
                  <th scope="col">Estudiante</th>
                  {instrument.criteria.map((_, i) => (
                    <th key={i} scope="col">Criterio {i + 1}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {students.map((s, i) => (
                  <tr key={s.id}>
                    <td>{String(i + 1).padStart(2, '0')}</td>
                    <th scope="row">{s.name}</th>
                    {instrument.criteria.map((_, j) => (
                      <td key={j} className="instrument-mark" />
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>
      )}
    </div>
  )
}
