import { useState } from 'react'
import { login, NetworkError } from '../services/api'
import Wordmark from './Wordmark'
import Chain from './Chain'

interface Props {
  onLogin: (name: string) => void
}

const prefersReducedMotion = () =>
  window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false

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
      <div className="login-intro">
        <Wordmark as="h1" size={58} />
        <p className="subtitle">Prepara, aplica y evalúa tus clases con menos carga.</p>
        <Chain size={44} />
      </div>

      <div key={errorCount} className={errorCount > 0 ? 'login-card shake' : 'login-card'}>
        <h2 className="login-title">Entra a tu escritorio</h2>

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
