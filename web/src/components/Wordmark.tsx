// Logotipo: "Allin" en el color del texto, "Edu" en dorado y una barra dorada debajo.
// Es el mismo que Wordmark() en Android (presentation/theme/AllinTheme.kt).
export default function Wordmark({ size = 28, as: Tag = 'span' }: { size?: number; as?: 'span' | 'h1' }) {
  return (
    <Tag className="wordmark" style={{ fontSize: size }} aria-label="AllinEdu">
      <span className="wordmark-allin">Allin</span>
      <span className="wordmark-edu">Edu</span>
    </Tag>
  )
}
