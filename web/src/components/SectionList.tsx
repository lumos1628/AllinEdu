import { useEffect, useState } from 'react'
import { getMySections } from '../services/api'
import type { Section } from '../services/api'

interface Props {
  onSelect: (section: Section) => void
}

// Retorna color por grado (usado para border-left y CSS variable)
function getGradeColor(grado: string): string {
  const g = parseInt(grado)
  if (g <= 3) return '#3B82F6' // Azul - Primaria inicial
  if (g <= 6) return '#10B981' // Verde - Primaria superior
  if (g <= 9) return '#F59E0B' // Ámbar - Secundaria
  return '#8B5CF6' // Púrpura - Bachillerato
}

// Retorna emoji representativo por grado
function getGradeEmoji(grado: string): string {
  const g = parseInt(grado)
  if (g <= 3) return '📚'
  if (g <= 6) return '📖'
  if (g <= 9) return '✏️'
  return '🎓'
}

interface SectionCardProps {
  section: Section
  onClick: () => void
}

function SectionCard({ section, onClick }: SectionCardProps) {
  const color = getGradeColor(section.grado)
  const emoji = getGradeEmoji(section.grado)

  return (
    <button
      className="section-card"
      onClick={onClick}
      style={{ '--card-color': color } as React.CSSProperties}
      aria-label={`${section.nombre}, Grado ${section.grado}. ${section.descripcion}`}
    >
      <div className="card-header">
        <span className="card-emoji">{emoji}</span>
        <div>
          <h3>{section.nombre}</h3>
          <p className="card-grade">Grado {section.grado}</p>
        </div>
      </div>
      <p className="card-desc">{section.descripcion}</p>
    </button>
  )
}

export default function SectionList({ onSelect }: Props) {
  const [sections, setSections] = useState<Section[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    getMySections().then(setSections).catch(() => setError('No se pudieron cargar tus secciones'))
  }, [])

  if (error) return <p className="error">{error}</p>

  return (
    <div>
      <h2>Mis secciones</h2>
      <div className="section-grid">
        {sections.map((s) => (
          <SectionCard key={s.id} section={s} onClick={() => onSelect(s)} />
        ))}
      </div>
    </div>
  )
}