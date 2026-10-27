// Capa de servicios: la única que habla con el backend (HTTP + JSON)

const BASE = '/api/v1'

// Evento que avisa a la UI (App.tsx) que la sesión dejó de ser válida.
// La capa de servicios no manipula el DOM: solo notifica.
export const SESSION_EXPIRED_EVENT = 'allinedu:session-expired'

// El backend no respondió (servidor apagado, sin red, CORS...). Se distingue
// de un 401 para que la UI no confunda un problema de red con credenciales.
export class NetworkError extends Error {
  constructor() {
    super('Sin conexión con el servidor')
    this.name = 'NetworkError'
  }
}

function getToken(): string | null {
  return localStorage.getItem('allinedu_token')
}

function authHeaders(): Record<string, string> {
  return { Authorization: `Bearer ${getToken()}` }
}

// Si el backend responde 401 (token vencido o inválido), la sesión ya no sirve:
// se limpia y se notifica para que la UI vuelva al login.
function handleUnauthorized(res: Response): void {
  if (res.status === 401) {
    logout()
    window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT))
  }
}

export interface LoginResponse {
  token: string
  userId: number
  name: string
  email: string
  role: string
  roleScope?: string | null
  educationLevelId?: number | null
  supervisorUserId?: number | null
}

export interface Section {
  id: number
  nombre: string
  grado: string
  descripcion: string
}

export interface WeekBlock {
  slotId: number
  dayOfWeek: number
  date: string
  start: string
  end: string
  area: string
  mine: boolean
  session: { id: number; unitId: number; title: string; approved: boolean; activities: number } | null
}

export interface SlotOptions {
  slotId: number
  date: string
  area: string
  start: string
  end: string
  unitId: number | null
  unitTitle: string | null
  existingSessionId: number | null
  candidates: UnitDetail['sessions']
}

export interface LearningUnit {
  id: number
  title: string
  situation: string | null
  startDate: string
  endDate: string
  approvedSessions: number
  totalSessions: number
}

export interface Week {
  sectionId: number
  weekStart: string
  today: string
  blocks: WeekBlock[]
  unit: LearningUnit | null
}

export interface UnitDetail {
  id: number
  title: string
  situation: string | null
  purpose: string | null
  startDate: string | null
  endDate: string | null
  competencies: { id: number; name: string; purpose: string | null; sessions: number }[]
  sessions: {
    id: number
    number: number
    title: string
    purpose: string | null
    approved: boolean
    competencies: string[]
    activities: number
  }[]
}

export interface SessionDetail {
  id: number
  unitId: number
  unitTitle: string
  number: number
  title: string
  purpose: string | null
  durationMinutes: number | null
  approved: boolean
  generated: boolean
  editedAt: string | null
  approvedAt: string | null
  competencies: string[]
  criteria: string[]
  evidences: { description: string; format: string | null }[]
  instrument: {
    name: string
    type: string
    criteria: { description: string; levels: { code: string; description: string }[] }[]
  } | null
  dua: { title: string; text: string | null }[]
  approaches: { title: string; text: string | null }[]
  materials: { name: string; quantity: number | null; required: boolean; description: string | null }[]
  activities: SessionActivity[]
}

export type Moment = 'INICIO' | 'DESARROLLO' | 'CIERRE' | 'OTRO'

export interface SessionActivity {
  id: number
  order: number
  moment: Moment | null
  title: string | null
  instructions: string
  minutes: number | null
  generated: boolean
}

export interface SessionUpdate {
  title: string
  purpose: string
  durationMinutes: number | null
  evidence: string
  activities: { id: number | null; moment: Moment; title: string; instructions: string; minutes: number | null }[]
}

export type Level = 'AD' | 'A' | 'B' | 'C'

export interface ClosureMark {
  competencyId: number
  level: Level
  previousLevel: Level | null
  conclusion: string | null
  recommendation: string | null
  missingConclusion: boolean
}

export interface Closure {
  periodId: number
  periodName: string
  startDate: string
  endDate: string
  competencies: { id: number; name: string }[]
  students: { enrollmentId: number; name: string; code: string; marks: ClosureMark[] }[]
  pendingConclusions: number
}

export interface MarkInput {
  enrollmentId: number
  competencyId: number
  level: Level
  conclusion: string
  recommendation: string
}

export interface Student {
  id: number
  name: string
  code: string
}

// TARDANZA cuenta como presente en los resúmenes, pero se registra aparte.
export type AttendanceStatus = 'PRESENTE' | 'AUSENTE' | 'TARDANZA'

export interface AttendanceEntry {
  studentId: number
  status: AttendanceStatus
}

export interface AttendanceRecord {
  studentId: number
  studentName: string
  date: string
  present: boolean
  status: AttendanceStatus
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  let res: Response
  try {
    res = await fetch(`${BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
  } catch {
    throw new NetworkError()
  }
  // Solo un 401 significa credenciales inválidas; un 5xx (o el proxy si el
  // backend está caído) es un problema de servidor, no de credenciales.
  if (res.status === 401) throw new Error('Credenciales inválidas')
  if (!res.ok) throw new NetworkError()
  const data = (await res.json()) as LoginResponse
  localStorage.setItem('allinedu_token', data.token)
  localStorage.setItem('allinedu_name', data.name)
  return data
}

export function logout(): void {
  localStorage.removeItem('allinedu_token')
  localStorage.removeItem('allinedu_name')
}

export function loggedName(): string {
  return localStorage.getItem('allinedu_name') ?? ''
}

export async function getMySections(): Promise<Section[]> {
  const res = await fetch(`${BASE}/sections/me`, { headers: authHeaders() })
  handleUnauthorized(res)
  if (!res.ok) throw new Error('Error al cargar secciones')
  return res.json()
}

export async function getWeek(sectionId: number, date?: string): Promise<Week> {
  const query = date ? `?date=${date}` : ''
  const res = await fetch(`${BASE}/sections/${sectionId}/week${query}`, { headers: authHeaders() })
  handleUnauthorized(res)
  if (!res.ok) throw new Error('Error al cargar el horario')
  return res.json()
}

async function authGet<T>(path: string, what: string): Promise<T> {
  const res = await fetch(`${BASE}${path}`, { headers: authHeaders() })
  handleUnauthorized(res)
  if (!res.ok) throw new Error(`Error al cargar ${what}`)
  return res.json()
}

export function getUnit(unitId: number): Promise<UnitDetail> {
  return authGet(`/units/${unitId}`, 'la unidad')
}

export function getSession(sessionId: number): Promise<SessionDetail> {
  return authGet(`/sessions/${sessionId}`, 'la sesión')
}

export async function updateSession(sessionId: number, body: SessionUpdate): Promise<SessionDetail> {
  const res = await fetch(`${BASE}/sessions/${sessionId}`, {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  handleUnauthorized(res)
  if (!res.ok) {
    const err = await res.json().catch(() => null)
    throw new Error(err?.message ?? 'No se pudo guardar la sesión')
  }
  return res.json()
}

// La generación tarda unos segundos; el backend responde 503 si el modelo está ocupado.
export async function generateDraft(sessionId: number): Promise<SessionDetail> {
  const res = await fetch(`${BASE}/sessions/${sessionId}/draft`, { method: 'POST', headers: authHeaders() })
  handleUnauthorized(res)
  if (!res.ok) {
    const err = await res.json().catch(() => null)
    throw new Error(err?.message ?? 'No se pudo preparar el borrador')
  }
  return res.json()
}

export function getSlotOptions(sectionId: number, slotId: number, date: string): Promise<SlotOptions> {
  return authGet(`/sections/${sectionId}/slots/${slotId}?date=${date}`, 'la clase')
}

// sessionId null: crear una sesión nueva para ese bloque y esa fecha.
export async function prepareSlot(
  sectionId: number,
  slotId: number,
  date: string,
  sessionId: number | null,
): Promise<SessionDetail> {
  const res = await fetch(`${BASE}/sections/${sectionId}/slots/${slotId}/session`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ date, sessionId }),
  })
  handleUnauthorized(res)
  if (!res.ok) {
    const err = await res.json().catch(() => null)
    throw new Error(err?.message ?? 'No se pudo preparar la clase')
  }
  return res.json()
}

export async function approveSession(sessionId: number): Promise<SessionDetail> {
  const res = await fetch(`${BASE}/sessions/${sessionId}/approval`, { method: 'POST', headers: authHeaders() })
  handleUnauthorized(res)
  if (!res.ok) throw new Error('No se pudo aprobar la sesión')
  return res.json()
}

export function getClosure(sectionId: number): Promise<Closure> {
  return authGet(`/sections/${sectionId}/closure`, 'el cierre del bimestre')
}

// El backend explica en `message` qué regla no se cumplió; se muestra tal cual.
export async function saveMark(sectionId: number, mark: MarkInput): Promise<Closure> {
  const res = await fetch(`${BASE}/sections/${sectionId}/closure/marks`, {
    method: 'PUT',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(mark),
  })
  handleUnauthorized(res)
  if (!res.ok) {
    const body = await res.json().catch(() => null)
    throw new Error(body?.message ?? 'No se pudo guardar')
  }
  return res.json()
}

export async function getSectionStudents(sectionId: number): Promise<Student[]> {
  const res = await fetch(`${BASE}/sections/${sectionId}/students`, { headers: authHeaders() })
  handleUnauthorized(res)
  if (!res.ok) throw new Error('Error al cargar alumnos')
  return res.json()
}

export async function saveAttendance(
  sectionId: number,
  date: string,
  attendance: AttendanceEntry[],
): Promise<AttendanceRecord[]> {
  const res = await fetch(`${BASE}/sections/${sectionId}/attendance`, {
    method: 'POST',
    headers: { ...authHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify({ date, attendance }),
  })
  handleUnauthorized(res)
  if (!res.ok) throw new Error('Error al guardar asistencia')
  return res.json()
}

export async function getAttendance(sectionId: number, date: string): Promise<AttendanceRecord[]> {
  const res = await fetch(`${BASE}/sections/${sectionId}/attendance?date=${date}`, {
    headers: authHeaders(),
  })
  handleUnauthorized(res)
  if (!res.ok) throw new Error('Error al consultar asistencia')
  return res.json()
}
