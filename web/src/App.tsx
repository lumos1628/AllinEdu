import { useEffect, useState } from 'react'
import Attendance from './components/Attendance'
import Home from './components/Home'
import UnitView from './components/UnitView'
import SessionView from './components/SessionView'
import ClosureView from './components/ClosureView'
import Login from './components/Login'
import Wordmark from './components/Wordmark'
import ThemeToggle from './components/ThemeToggle'
import { SESSION_EXPIRED_EVENT, loggedName, logout } from './services/api'
import type { Section } from './services/api'

// Pantallas de la app. Sin router: la navegación es corta y lineal
// (inicio → unidad → sesión, inicio → asistencia, inicio → cierre).
export type View =
  | { kind: 'home' }
  | { kind: 'attendance'; section: Section }
  | { kind: 'unit'; unitId: number }
  | { kind: 'session'; sessionId: number; unitId: number }
  | { kind: 'closure'; section: Section }

const HOME: View = { kind: 'home' }

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('edufast_token'))
  const [userName, setUserName] = useState(loggedName())
  const [view, setView] = useState<View>(HOME)

  // Si el backend invalida la sesión (401), la capa de servicios emite este
  // evento y aquí se vuelve al login sin recargar toda la página.
  useEffect(() => {
    function handleSessionExpired() {
      setToken(null)
      setUserName('')
      setView(HOME)
    }
    window.addEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired)
  }, [])

  // Cada pantalla nueva empieza arriba, no donde quedó el scroll de la anterior.
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [view])

  if (!token) {
    return (
      <div className="page">
        <div className="login-topbar">
          <ThemeToggle />
        </div>
        <Login
          onLogin={(name) => {
            setToken(localStorage.getItem('edufast_token'))
            setUserName(name)
          }}
        />
      </div>
    )
  }

  function handleLogout() {
    logout()
    setToken(null)
    setUserName('')
    setView(HOME)
  }

  return (
    <div className="page">
      <header className="header">
        <button className="wordmark-home" onClick={() => setView(HOME)} aria-label="Ir al inicio">
          <Wordmark size={20} />
        </button>
        <span className="header-actions">
          <ThemeToggle />
          {userName} · <button className="link" onClick={handleLogout}>Salir</button>
        </span>
      </header>

      {view.kind === 'attendance' && <Attendance section={view.section} onBack={() => setView(HOME)} />}
      {view.kind === 'unit' && (
        <UnitView
          unitId={view.unitId}
          onBack={() => setView(HOME)}
          onOpenSession={(sessionId) => setView({ kind: 'session', sessionId, unitId: view.unitId })}
        />
      )}
      {view.kind === 'session' && (
        <SessionView sessionId={view.sessionId} onBack={() => setView({ kind: 'unit', unitId: view.unitId })} />
      )}
      {view.kind === 'closure' && <ClosureView section={view.section} onBack={() => setView(HOME)} />}
      {view.kind === 'home' && (
        <Home
          userName={userName}
          onTakeAttendance={(section) => setView({ kind: 'attendance', section })}
          onOpenUnit={(unitId) => setView({ kind: 'unit', unitId })}
          onOpenClosure={(section) => setView({ kind: 'closure', section })}
        />
      )}
    </div>
  )
}
