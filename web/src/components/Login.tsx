import { useState } from 'react'
import { login, NetworkError } from '../services/api'
import Wordmark from './Wordmark'

interface Props {
  onLogin: (name: string) => void
}

const prefersReducedMotion = () =>
  window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false

// Eslabones de la cadena (ALCANCE.md §2). El único construido se enciende al final.
const STEPS = [
  { label: 'Planificar', ready: false },
  { label: 'Aplicar', ready: true },
  { label: 'Evaluar', ready: false },
  { label: 'Reforzar', ready: false },
]

// La cadena de AllinEdu se dibuja sola: la línea avanza y cada paso aparece
// cuando la línea lo alcanza. Es decorativa; el texto equivalente va en aria-label.
function ChainHero() {
  const xs = STEPS.map((_, i) => 40 + i * 80)
  return (
    <svg
      className="chain-hero"
      viewBox="0 0 320 84"
      role="img"
      aria-label="La cadena de AllinEdu: planificar, aplicar en clase, evaluar y reforzar"
    >
      <line className="chain-hero-track" x1={xs[0]} y1="28" x2={xs[xs.length - 1]} y2="28" />
      <line
        className="chain-hero-line"
        x1={xs[0]}
        y1="28"
        x2={xs[xs.length - 1]}
        y2="28"
        pathLength={1}
      />
      {STEPS.map((step, i) => (
        <g
          key={step.label}
          className={step.ready ? 'chain-hero-node ready' : 'chain-hero-node'}
          style={{ '--i': i } as React.CSSProperties}
        >
          {step.ready && <circle className="chain-hero-pulse" cx={xs[i]} cy="28" r="10" />}
          <circle className="chain-hero-dot" cx={xs[i]} cy="28" r="10" />
          <text x={xs[i]} y="66" textAnchor="middle">
            {step.label}
          </text>
        </g>
      ))}
    </svg>
  )
}

export default function Login({ onLogin }: Props) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  // Cambia en cada error para que la sacudida se repita aunque el mensaje sea el mismo
  const [errorCount, setErrorCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const [welcome, setWelcome] = useState<string | null>(null)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const data = await login(email, password)
      // Sello de bienvenida un instante antes de pasar al menú
      setWelcome(data.name)
      window.setTimeout(() => onLogin(data.name), prefersReducedMotion() ? 400 : 1100)
    } catch (e) {
      setError(e instanceof NetworkError ? 'Sin conexión con el servidor' : 'Correo o contraseña incorrectos')
      setErrorCount((n) => n + 1)
      setLoading(false)
    }
  }

  return (
    <div className="login-screen">
      <ChainHero />

      <div key={errorCount} className={errorCount > 0 ? 'login-card shake' : 'login-card'}>
        <Wordmark as="h1" size={32} />
        <p className="subtitle">Prepara, aplica y evalúa tus clases con menos carga</p>

        {welcome ? (
          <div className="welcome" role="status">
            <span className="welcome-stamp">¡Bienvenido, profe!</span>
            <span className="welcome-name">{welcome}</span>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <label className="field">
              <span>Correo</span>
              <input
                type="email"
                autoComplete="username"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                aria-invalid={error ? true : undefined}
                required
              />
            </label>
            <label className="field">
              <span>Contraseña</span>
              <span className="password-field">
                <input
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  aria-invalid={error ? true : undefined}
                  required
                />
                <button
                  type="button"
                  className="toggle"
                  aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                  onClick={() => setShowPassword((visible) => !visible)}
                >
                  {showPassword ? 'Ocultar' : 'Mostrar'}
                </button>
              </span>
            </label>
            {error && (
              <p className="error" role="alert">
                {error}
              </p>
            )}
            <button type="submit" className="primary login-submit" disabled={loading}>
              {loading && <span className="spinner" aria-hidden="true" />}
              {loading ? 'Entrando…' : 'Ingresar'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
