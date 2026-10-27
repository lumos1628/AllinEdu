import { useEffect, useState } from 'react'
import Attendance from './components/Attendance'
import Home from './components/Home'
import UnitView from './components/UnitView'
import SessionView from './components/SessionView'
import ClosureView from './components/ClosureView'
import SlotView from './components/SlotView'
import ClassView from './components/ClassView'
import type { ClassInfo } from './components/ClassView'
import InstrumentView from './components/InstrumentView'
import Login from './components/Login'
import Wordmark from './components/Wordmark'
import ThemeToggle from './components/ThemeToggle'
import { Chumpi } from './components/Tocapu'
import { SESSION_EXPIRED_EVENT, loggedName, logout } from './services/api'
import type { Section } from './services/api'

// Pantallas de la app. Sin router: la navegación es corta y lineal. La sesión
// recuerda desde dónde se abrió (inicio o unidad) para volver ahí.
export type View =
  | { kind: 'home' }
  | { kind: 'attendance'; section: Section }
  | { kind: 'unit'; unitId: number }
  | { kind: 'session'; sessionId: number; back: View; backLabel: string }
  | { kind: 'slot'; section: Section; slotId: number; date: string }
  | { kind: 'class'; section: Section; info: ClassInfo; sessionId: number }
  | { kind: 'instrument'; section: Section; sessionId: number; back: View }
  | { kind: 'closure'; section: Section }

const HOME: View = { kind: 'home' }

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('allinedu_token'))
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
        <Chumpi className="login-chumpi" cell={3} />
        <Login
          onLogin={(name) => {
            setToken(localStorage.getItem('allinedu_token'))
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
          <span className="header-user">{userName}</span>
          <button className="link" onClick={handleLogout}>Salir</button>
          <ThemeToggle />
        </span>
      </header>
      <Chumpi className="header-chumpi" />

      {view.kind === 'attendance' && <Attendance section={view.section} onBack={() => setView(HOME)} />}
      {view.kind === 'unit' && (
        <UnitView
          unitId={view.unitId}
          onBack={() => setView(HOME)}
          onOpenSession={(sessionId) => setView({ kind: 'session', sessionId, back: view, backLabel: 'La unidad' })}
        />
      )}
      {view.kind === 'session' && (
        <SessionView sessionId={view.sessionId} backLabel={view.backLabel} onBack={() => setView(view.back)} />
      )}
      {view.kind === 'slot' && (
        <SlotView
          section={view.section}
          slotId={view.slotId}
          date={view.date}
          onBack={() => setView(HOME)}
          onReady={(sessionId, info) => setView({ kind: 'class', section: view.section, info, sessionId })}
        />
      )}
      {view.kind === 'class' && (
        <ClassView
          section={view.section}
          info={view.info}
          sessionId={view.sessionId}
          onBack={() => setView(HOME)}
          onOpenSession={() => setView({ kind: 'session', sessionId: view.sessionId, back: view, backLabel: 'La clase' })}
          onOpenInstrument={() => setView({ kind: 'instrument', section: view.section, sessionId: view.sessionId, back: view })}
          onTakeAttendance={() => setView({ kind: 'attendance', section: view.section })}
        />
      )}
      {view.kind === 'instrument' && (
        <InstrumentView section={view.section} sessionId={view.sessionId} onBack={() => setView(view.back)} />
      )}
      {view.kind === 'closure' && <ClosureView section={view.section} onBack={() => setView(HOME)} />}
      {view.kind === 'home' && (
        <Home
          userName={userName}
          onTakeAttendance={(section) => setView({ kind: 'attendance', section })}
          onOpenUnit={(unitId) => setView({ kind: 'unit', unitId })}
          onOpenClosure={(section) => setView({ kind: 'closure', section })}
          onOpenClass={(section, info, sessionId) => setView({ kind: 'class', section, info, sessionId })}
          onPrepareClass={(section, slotId, date) => setView({ kind: 'slot', section, slotId, date })}
        />
      )}
    </div>
  )
}
