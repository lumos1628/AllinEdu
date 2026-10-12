import { useEffect, useState } from 'react'
import Attendance from './components/Attendance'
import SectionList from './components/SectionList'
import Login from './components/Login'
import { SESSION_EXPIRED_EVENT, loggedName, logout } from './services/api'
import type { Section } from './services/api'

export default function App() {
  const [token, setToken] = useState(() => localStorage.getItem('edufast_token'))
  const [userName, setUserName] = useState(loggedName())
  const [section, setSection] = useState<Section | null>(null)

  // Si el backend invalida la sesión (401), la capa de servicios emite este
  // evento y aquí se vuelve al login sin recargar toda la página.
  useEffect(() => {
    function handleSessionExpired() {
      setToken(null)
      setUserName('')
      setSection(null)
    }
    window.addEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired)
  }, [])

  if (!token) {
    return (
      <div className="page">
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
    setSection(null)
  }

  return (
    <div className="page">
      <header className="header">
        <strong>EduFast</strong>
        <span>
          {userName} · <button className="link" onClick={handleLogout}>Salir</button>
        </span>
      </header>

      {section ? (
        <Attendance section={section} onBack={() => setSection(null)} />
      ) : (
        <SectionList onSelect={setSection} />
      )}
    </div>
  )
}
