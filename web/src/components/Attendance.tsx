import { useEffect, useMemo, useRef, useState } from 'react'
import { getAttendance, getSectionStudents, saveAttendance } from '../services/api'
import type {
  AttendanceEntry,
  AttendanceRecord,
  AttendanceStatus,
  Section,
  Student,
} from '../services/api'

interface Props {
  section: Section
  onBack: () => void
}

// Cada clic en la tarjeta avanza al siguiente estado. Tras PRESENTE va AUSENTE
// porque marcar faltas es lo más frecuente cuando todos parten como presentes.
const NEXT_STATUS: Record<AttendanceStatus, AttendanceStatus> = {
  PRESENTE: 'AUSENTE',
  AUSENTE: 'TARDANZA',
  TARDANZA: 'PRESENTE',
}

const STATUS_LABEL: Record<AttendanceStatus, string> = {
  PRESENTE: 'Asistió',
  AUSENTE: 'Faltó',
  TARDANZA: 'Tardanza',
}

const prefersReducedMotion = () =>
  window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false

interface CardProps {
  student: Student
  status: AttendanceStatus
  index: number
  disabled: boolean
  onCycle: () => void
}

// Tarjeta de alumno. Al cambiar de estado, el nuevo color se expande como tinta
// desde el punto donde se hizo clic y el sello del estado aparece con un rebote.
function StudentCard({ student, status, index, disabled, onCycle }: CardProps) {
  const [shown, setShown] = useState(status)
  const [from, setFrom] = useState(status)
  const [inkKey, setInkKey] = useState(0)
  const [origin, setOrigin] = useState('0px 50%')

  // Patrón "estado derivado de props": detecta el cambio durante el render.
  if (status !== shown) {
    setFrom(shown)
    setShown(status)
    setInkKey((k) => k + 1)
  }

  const statusClass = status.toLowerCase()

  return (
    <li className={`roll-item status-${statusClass}`} style={{ '--i': Math.min(index, 10) } as React.CSSProperties}>
      <span className="roll-dot" aria-hidden="true" />
      <button
        type="button"
        className={`student-card status-${statusClass} from-${from.toLowerCase()}`}
        disabled={disabled}
        onClick={(e) => {
          const rect = e.currentTarget.getBoundingClientRect()
          // Con teclado no hay coordenadas: la tinta sale del borde izquierdo.
          if (e.clientX || e.clientY) setOrigin(`${e.clientX - rect.left}px ${e.clientY - rect.top}px`)
          onCycle()
        }}
        aria-label={`${student.name}: ${STATUS_LABEL[status]}. Activa para cambiar.`}
      >
        {inkKey > 0 && (
          <span
            key={inkKey}
            className="ink"
            style={{ '--origin': origin } as React.CSSProperties}
            onAnimationEnd={() => setOrigin('0px 50%')}
            aria-hidden="true"
          />
        )}
        <span className="student-info">
          <span className="student-name">{student.name}</span>
          <small className="student-code">{student.code}</small>
        </span>
        <span key={`stamp-${inkKey}`} className="stamp">
          {STATUS_LABEL[status]}
        </span>
      </button>
    </li>
  )
}

// Botones flotantes para ir al primer o al último alumno. Cada uno aparece
// solo si hay hacia dónde desplazarse.
function ScrollButtons({ target }: { target: React.RefObject<HTMLOListElement | null> }) {
  const [canUp, setCanUp] = useState(false)
  const [canDown, setCanDown] = useState(false)

  useEffect(() => {
    function update() {
      const doc = document.documentElement
      setCanUp(window.scrollY > 120)
      setCanDown(window.scrollY + window.innerHeight < doc.scrollHeight - 120)
    }
    update()
    window.addEventListener('scroll', update, { passive: true })
    window.addEventListener('resize', update)
    return () => {
      window.removeEventListener('scroll', update)
      window.removeEventListener('resize', update)
    }
  }, [])

  function go(edge: 'first' | 'last') {
    const list = target.current
    const item = edge === 'first' ? list?.firstElementChild : list?.lastElementChild
    item?.scrollIntoView({
      behavior: prefersReducedMotion() ? 'auto' : 'smooth',
      block: edge === 'first' ? 'center' : 'end',
    })
  }

  return (
    <div className="scroll-buttons">
      <button type="button" className={canUp ? 'fab visible' : 'fab'} onClick={() => go('first')}
        aria-label="Ir al primer alumno" tabIndex={canUp ? 0 : -1}>
        ↑
      </button>
      <button type="button" className={canDown ? 'fab visible' : 'fab'} onClick={() => go('last')}
        aria-label="Ir al último alumno" tabIndex={canDown ? 0 : -1}>
        ↓
      </button>
    </div>
  )
}

// Fecha de hoy en hora LOCAL (no UTC): evita que de noche aparezca el día equivocado
function today(): string {
  const d = new Date()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${month}-${day}`
}

export default function Attendance({ section, onBack }: Props) {
  const [students, setStudents] = useState<Student[]>([])
  const [date, setDate] = useState(today())
  const [statuses, setStatuses] = useState<Record<number, AttendanceStatus>>({})
  const [saved, setSaved] = useState<AttendanceRecord[]>([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const listRef = useRef<HTMLOListElement>(null)

  // Carga los alumnos de la sección una sola vez
  useEffect(() => {
    let cancelled = false
    getSectionStudents(section.id)
      .then((list) => {
        if (!cancelled) setStudents(list)
      })
      .catch(() => {
        if (!cancelled) setError('No se pudieron cargar los alumnos')
      })
    return () => {
      cancelled = true
    }
  }, [section.id])

  // Cada vez que cambian los alumnos o la fecha, carga la asistencia de ese día.
  // El flag "cancelled" evita que una respuesta lenta pise los cambios del usuario.
  useEffect(() => {
    if (students.length === 0) return
    let cancelled = false

    const allPresent: Record<number, AttendanceStatus> = Object.fromEntries(
      students.map((s) => [s.id, 'PRESENTE']),
    )

    getAttendance(section.id, date)
      .then((records) => {
        if (cancelled) return
        setSaved(records)
        setStatuses(
          records.length > 0
            ? Object.fromEntries(records.map((r) => [r.studentId, r.status]))
            : allPresent,
        )
      })
      .catch(() => {
        if (cancelled) return
        setSaved([])
        setStatuses(allPresent)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [section.id, date, students])

  const statusOf = (id: number): AttendanceStatus => statuses[id] ?? 'PRESENTE'

  // Conteo en vivo de lo marcado en pantalla (aún no guardado)
  const counts = useMemo(() => {
    const c: Record<AttendanceStatus, number> = { PRESENTE: 0, AUSENTE: 0, TARDANZA: 0 }
    for (const s of students) c[statuses[s.id] ?? 'PRESENTE']++
    return c
  }, [students, statuses])

  function setAll(value: AttendanceStatus) {
    setStatuses(Object.fromEntries(students.map((s) => [s.id, value])))
  }

  function cycle(id: number) {
    setStatuses((prev) => ({ ...prev, [id]: NEXT_STATUS[prev[id] ?? 'PRESENTE'] }))
  }

  async function handleSave() {
    setSaving(true)
    setError('')
    setMessage('')
    const attendance: AttendanceEntry[] = students.map((s) => ({
      studentId: s.id,
      status: statusOf(s.id),
    }))
    try {
      const records = await saveAttendance(section.id, date, attendance)
      setSaved(records)
      setMessage(`Asistencia del ${date} guardada`)
    } catch {
      setError('No se pudo guardar la asistencia')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="topbar">
        <button className="link" onClick={onBack}>
          ← Inicio
        </button>
        <h2>{section.descripcion}</h2>
      </div>

      <div className="controls">
        <label>
          Fecha:
          <input
            type="date"
            value={date}
            onChange={(e) => {
              setDate(e.target.value)
              setLoading(true)
            }}
          />
        </label>
        <button onClick={() => setAll('PRESENTE')} disabled={loading}>
          Todos asistieron
        </button>
      </div>

      <p className="muted">Toca una tarjeta para cambiar su estado: asistió → faltó → tardanza.</p>

      <ul className="attendance-counts" aria-live="polite">
        <li className="count-present">Asistieron: <strong>{counts.PRESENTE}</strong></li>
        <li className="count-late">Tardanzas: <strong>{counts.TARDANZA}</strong></li>
        <li className="count-absent">Faltaron: <strong>{counts.AUSENTE}</strong></li>
      </ul>

      <ol className="roll-list" ref={listRef} aria-label="Lista de alumnos">
        {students.map((s, i) => (
          <StudentCard
            key={s.id}
            student={s}
            status={statusOf(s.id)}
            index={i}
            disabled={loading}
            onCycle={() => cycle(s.id)}
          />
        ))}
      </ol>

      <ScrollButtons target={listRef} />

      {message && <p className="ok">{message}</p>}
      {error && <p className="error">{error}</p>}

      <div className="actions">
        <button className="primary" onClick={handleSave} disabled={loading || saving}>
          {saving ? 'Guardando...' : 'Guardar asistencia'}
        </button>
      </div>

      {saved.length > 0 && (
        <div className="summary">
          <h3>Resumen del {date}</h3>
          <p>
            Asistieron: <strong>{saved.filter((r) => r.status === 'PRESENTE').length}</strong> · Tardanzas:{' '}
            <strong>{saved.filter((r) => r.status === 'TARDANZA').length}</strong> · Faltaron:{' '}
            <strong>{saved.filter((r) => r.status === 'AUSENTE').length}</strong>
          </p>
        </div>
      )}
    </div>
  )
}