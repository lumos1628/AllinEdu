import Tocapu from './Tocapu'

// Logotipo: un tocapu de rombo en cochinilla, "Allin" en el color del texto y "Edu" en maíz.
// Android todavía usa la versión anterior (barra dorada); ver IDENTIDAD.md.
export default function Wordmark({ size = 28, as: Tag = 'span' }: { size?: number; as?: 'span' | 'h1' }) {
  return (
    <Tag className="wordmark" style={{ fontSize: size }} aria-label="AllinEdu">
      <Tocapu motif="rombo" dye="cochinilla" weave="woven" size={Math.round(size * 0.82)} className="wordmark-mark" />
      <span>
        <span className="wordmark-allin">Allin</span>
        <span className="wordmark-edu">Edu</span>
      </span>
    </Tag>
  )
}
