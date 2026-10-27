# Arquitectura Móvil AllinEdu — Guía Profesional de Alto Nivel

> Basado en [Google Architecture Guides](https://developer.android.com/topic/architecture) y [Material Design 3](https://m3.material.io/)

**Versión:** 1.0  
**Fecha:** 2026-10-12  
**Stack:** Kotlin + Jetpack Compose + Navigation + Room + Retrofit + Material Design 3

---

## 📐 Arquitectura Moderna de Android: 3 Capas + MVVM + UDF

La arquitectura moderna de Android está diseñada para ser **escalable, testeable y mantenible**. Se estructura en tres capas independientes con flujo de datos unidireccional.

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                   │
│  (Jetpack Compose + Scaffold + Navigation Compose)      │
│                                                          │
│  ┌────────────────┐      ┌──────────────┐               │
│  │  Composables   │◄────►│  ViewModels  │               │
│  │  (UI)          │      │  + UDF       │               │
│  └────────────────┘      └──────┬───────┘               │
│                                  │                       │
│                              (State)                     │
│                                  │                       │
└──────────────────────────────────┼──────────────────────┘
                                   │
                   ┌───────────────┼───────────────┐
                   │               │               │
                   ▼               ▼               ▼
         ┌─────────────┐ ┌──────────────┐ ┌────────────┐
         │   Events    │ │   Effects    │ │   State    │
         └─────────────┘ └──────────────┘ └────────────┘
                                   │
┌──────────────────────────────────┼──────────────────────┐
│                    DOMAIN LAYER                         │
│  (Use Cases / Interactors — OPCIONAL para apps simples) │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Contiene la lógica de negocio agnóstica a      │  │
│  │  framework. Opcional si los UsesCases son       │  │
│  │  simples; los repositorios pueden hacer el      │  │
│  │  trabajo directamente.                           │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
└──────────────────────────────────────────────────────────┘
                                   │
┌──────────────────────────────────┼──────────────────────┐
│                    DATA LAYER                           │
│  (Repositories + Data Sources: Room + Retrofit + API)   │
│                                                          │
│  ┌─────────────────┐  ┌──────────────┐                 │
│  │  Repository     │  │  RemoteDS    │                 │
│  │  (SSOT)         │  │  (Retrofit)   │                 │
│  │                 │  │              │                 │
│  │  LocalDS        │  └──────────────┘                 │
│  │  (Room/SQLite)  │                                    │
│  └─────────────────┘                                    │
│         │                                               │
│         ▼                                               │
│  ┌─────────────────────────────────────────────┐       │
│  │   Backend API (Spring Boot en :8080)        │       │
│  └─────────────────────────────────────────────┘       │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🔄 Unidirectional Data Flow (UDF) en Compose

El flujo de datos es **unidireccional y reactivo**:

```
1. USER INTERACTION
   (click, input, etc.)
            ▼
2. EVENT FIRED IN COMPOSABLE
   onClick { viewModel.handleAction(...) }
            ▼
3. VIEWMODEL RECEIVES EVENT
   fun handleAction(action: UiAction) {
       state.value = state.value.copy(...)
       // O emit a Repository
   }
            ▼
4. STATE UPDATES
   _state.value = newState
            ▼
5. COMPOSABLE RECOMPOSES
   val state by viewModel.state.collectAsState()
   if (state.isLoading) LoadingScreen()
```

**Reglas críticas:**
- ✅ Composables **NUNCA** acceden directamente a Repositories
- ✅ ViewModels **NUNCA** tienen lógica de UI
- ✅ State fluye DOWN (ViewModel → Composable)
- ✅ Events fluyen UP (Composable → ViewModel → Repository)
- ✅ Composables son **stateless** y **reusables**

---

## 📁 Estructura de Carpetas (Moderno + Escalable)

```
mobile/app/src/main/kotlin/com/allinedu/mobile/
│
├── MainActivity.kt                    (Single Activity, punto de entrada)
├── MyApp.kt                           (Aplicación config)
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt                  (Palette Material 3)
│   │   ├── Type.kt                   (Typography)
│   │   └── Theme.kt                  (TemasCompose + Material3)
│   │
│   ├── components/                    (Componentes reutilizables)
│   │   ├── Buttons.kt
│   │   ├── Cards.kt
│   │   ├── LoadingStates.kt
│   │   ├── ErrorStates.kt
│   │   └── Dialogs.kt
│   │
│   └── screens/                       (Pantallas por feature)
│       ├── login/
│       │   ├── LoginScreen.kt         (Composable)
│       │   ├── LoginViewModel.kt      (ViewModel + UDF)
│       │   └── LoginUiState.kt        (Data class para State)
│       │
│       ├── attendance/
│       │   ├── AttendanceScreen.kt
│       │   ├── AttendanceViewModel.kt
│       │   └── AttendanceUiState.kt
│       │
│       ├── sections/
│       │   ├── SectionsScreen.kt
│       │   ├── SectionsViewModel.kt
│       │   └── SectionsUiState.kt
│       │
│       └── planning/                  (Próximas semanas)
│           ├── PlanningScreen.kt
│           ├── PlanningViewModel.kt
│           └── PlanningUiState.kt
│
├── navigation/
│   ├── NavGraph.kt                   (Define rutas y destinos)
│   ├── NavigationRoutes.kt           (Constantes de rutas)
│   └── NavHost.kt                    (Host navigation)
│
├── data/
│   ├── repository/                   (Repositorios = SSOT)
│   │   ├── AttendanceRepository.kt
│   │   ├── SectionRepository.kt
│   │   ├── AuthRepository.kt
│   │   └── UserRepository.kt
│   │
│   ├── datasource/
│   │   ├── local/                    (Room/SQLite)
│   │   │   ├── database/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   └── entities/
│   │   │   │       ├── StudentEntity.kt
│   │   │   │       ├── AttendanceEntity.kt
│   │   │   │       └── ...
│   │   │   │
│   │   │   └── dao/
│   │   │       ├── StudentDao.kt
│   │   │       ├── AttendanceDao.kt
│   │   │       └── ...
│   │   │
│   │   └── remote/                   (Retrofit API)
│   │       ├── ApiService.kt         (Endpoints)
│   │       ├── ApiClient.kt          (Configuración Retrofit)
│   │       └── dtos/
│   │           ├── LoginRequest.kt
│   │           ├── AttendanceRequest.kt
│   │           └── ...
│   │
│   └── model/                        (Domain models)
│       ├── Student.kt
│       ├── Section.kt
│       ├── Attendance.kt
│       └── ...
│
├── domain/                           (OPCIONAL — Use Cases)
│   ├── usecase/
│   │   ├── GetAttendanceUseCase.kt
│   │   ├── RegisterAttendanceUseCase.kt
│   │   └── ...
│   │
│   └── repository/                   (Interfaces de repositorio)
│       ├── IAttendanceRepository.kt
│       └── ...
│
├── di/                               (Dependency Injection)
│   ├── RepositoryModule.kt           (Provee repositorios)
│   ├── DatabaseModule.kt             (Provee DB + DAOs)
│   ├── NetworkModule.kt              (Provee Retrofit)
│   └── ViewModel Module.kt            (Provee ViewModels)
│
├── util/
│   ├── Constants.kt                  (URLs, Keys, etc.)
│   ├── Extensions.kt                 (Funciones útiles)
│   └── Logging.kt
│
└── build.gradle.kts                  (Dependencies)
```

---

## 🎯 Convenciones de Código

### Composables
```kotlin
// ✅ CORRECTO: Stateless, reusable, descriptivo
@Composable
fun AttendanceCard(
    student: Student,
    isPresent: Boolean,
    onTogglePresence: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Text(student.name)
        Checkbox(checked = isPresent, onCheckedChange = { onTogglePresence() })
    }
}

// ❌ INCORRECTO: Stateful, lógica en Composable
@Composable
fun AttendanceCard(student: Student) {
    var isPresent by remember { mutableStateOf(false) }
    // Pedir al backend, validar, etc.
    // NO!
}
```

### ViewModels
```kotlin
// ✅ CORRECTO: Expone State, recibe Events, emite Effects
@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceUiState())
    val state: StateFlow<AttendanceUiState> = _state.asStateFlow()

    fun onTogglePresence(studentId: Long, isPresent: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                attendanceRepository.registerAttendance(studentId, isPresent)
                _state.update { it.copy(isLoading = false, success = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
```

### Data Classes para State
```kotlin
// ✅ CORRECTO: Immutable, copy-safe
data class AttendanceUiState(
    val isLoading: Boolean = false,
    val students: List<Student> = emptyList(),
    val selected: Map<Long, Boolean> = emptyMap(),
    val error: String? = null,
    val success: Boolean = false
)
```

### Repository (SSOT)
```kotlin
// ✅ CORRECTO: Una fuente de verdad
class AttendanceRepository @Inject constructor(
    private val localDataSource: AttendanceLocalDataSource,
    private val remoteDataSource: AttendanceRemoteDataSource
) {
    suspend fun getAttendance(sectionId: Long, date: LocalDate): Flow<List<Attendance>> {
        return localDataSource.getAttendance(sectionId, date)
            .combine(
                remoteDataSource.getAttendance(sectionId, date)
            ) { local, remote ->
                // Merge: mostrar local primero, actualizar con remote
                remote
            }
    }

    suspend fun registerAttendance(studentId: Long, isPresent: Boolean) {
        remoteDataSource.registerAttendance(studentId, isPresent)  // Envía a backend
        localDataSource.save(...)  // Cachea localmente
    }
}
```

---

## 🏗️ Semana por Semana: Implementación

### Semana 5: Navigation Compose + Multi-Screen

**Objetivo:** Navegar entre múltiples pantallas con rutas tipadas.

**Tareas:**
1. Agregar dependencia `androidx.navigation:navigation-compose`
2. Crear `NavGraph` con rutas (Login → Sections → Attendance)
3. Mover screens a carpeta `ui/screens/`
4. Configurar `NavHost` en MainActivity

**Archivo clave:** `navigation/NavGraph.kt`
```kotlin
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("sections") { SectionsScreen(navController) }
        composable("attendance/{sectionId}") { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getString("sectionId")
            AttendanceScreen(navController, sectionId)
        }
    }
}
```

---

### Semana 7: Room + SQLite (Persistencia Local)

**Objetivo:** Cachear datos localmente para offline-first experience.

**Tareas:**
1. Agregar `androidx.room:room-*` dependencies
2. Crear Entidades: `StudentEntity`, `AttendanceEntity`, `SectionEntity`
3. Crear DAOs: `StudentDao`, `AttendanceDao`
4. Crear `AppDatabase` (singleton)
5. Implementar `LocalDataSource` para cada Repository

**Estructura Room:**
```kotlin
@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val sectionId: Long
)

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE sectionId = :sectionId")
    fun getBySection(sectionId: Long): Flow<List<StudentEntity>>

    @Upsert
    suspend fun save(student: StudentEntity)
}

@Database(entities = [StudentEntity::class, AttendanceEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}
```

---

### Semana 8: MVVM Formal + UDF

**Objetivo:** Estructurar ViewModels y State correctamente.

**Tareas:**
1. Crear `*UiState` data classes para cada pantalla
2. Refactorizar ViewModels para exponer `StateFlow<UiState>`
3. Implementar sealed classes para `UiAction` y `UiEffect`
4. Conectar Composables a ViewModels con `collectAsState()`

---

### Semana 9: Retrofit Integration (Reemplaza Ktor)

**Objetivo:** Consumir API backend (Spring Boot) con Retrofit.

**Tareas:**
1. Reemplazar `ktor-client` por `com.squareup.retrofit2:retrofit`
2. Agregar `com.squareup.retrofit2:converter-gson` o `kotlinx-serialization`
3. Crear `ApiService` con endpoints
4. Crear `ApiClient` (singleton Retrofit)
5. Implementar `RemoteDataSource` para cada feature

**Archivo clave:** `datasource/remote/ApiService.kt`
```kotlin
interface ApiService {
    @GET("api/v1/sections/{sectionId}/students")
    suspend fun getStudents(@Path("sectionId") sectionId: Long): Response<List<StudentDto>>

    @POST("api/v1/attendance")
    suspend fun registerAttendance(@Body request: AttendanceRequest): Response<Unit>
}
```

---

### Semana 10: CRUD + Listas

**Objetivo:** Integrar Retrofit + Room en un flujo completo (read, create, update).

**Tareas:**
1. Implementar Repository que combine local + remote
2. Usar `LazyColumn` para listas eficientes
3. Implementar pull-to-refresh
4. Error handling + retry logic

---

## 🎨 Material Design 3 en Compose

```kotlin
// Colores dinámicos (Material You)
@Composable
fun MyApp() {
    val colorScheme = dynamicLightColorScheme(LocalContext.current)
    MaterialTheme(colorScheme = colorScheme) {
        // Tu app
    }
}

// Componentes de Material 3
Scaffold(
    topBar = { TopAppBar(title = { Text("Asistencia") }) },
    floatingActionButton = { FloatingActionButton(onClick = { /*...*/ }) }
) { padding ->
    LazyColumn(modifier = Modifier.padding(padding)) {
        items(students) { student ->
            AttendanceCard(student)
        }
    }
}
```

---

## 🧪 Testing Strategy

```kotlin
// Unit Test: ViewModel
@Test
fun `when registering attendance, state updates loading then success`() = runTest {
    val viewModel = AttendanceViewModel(mockRepository)
    viewModel.onTogglePresence(studentId = 1, isPresent = true)

    advanceUntilIdle()
    val state = viewModel.state.value
    assertEquals(false, state.isLoading)
    assertEquals(true, state.success)
}

// Integration Test: Room + Repository
@Test
fun `repository saves to local and fetches from backend`() = runTest {
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val repository = AttendanceRepository(
        localDataSource = AttendanceLocalDataSource(db.attendanceDao()),
        remoteDataSource = mockApiService
    )
    repository.registerAttendance(1, true)
    val local = db.attendanceDao().getAll().first()
    assertEquals(1, local.studentId)
}
```

---

## 📦 Dependency Injection (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAttendanceRepository(
        local: AttendanceLocalDataSource,
        remote: AttendanceRemoteDataSource
    ): AttendanceRepository {
        return AttendanceRepository(local, remote)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "allinedu.db")
            .build()
    }
}
```

---

## ✅ Checklist de Buenas Prácticas

- [ ] Composables son **stateless** y **100% reusables**
- [ ] ViewModels exponen **StateFlow<UiState>**, NO mutable state directo
- [ ] **Un solo Activity** (MainActivity)
- [ ] Cada feature tiene su carpeta (login/, attendance/, etc.)
- [ ] Repository es **SSOT** (Single Source of Truth)
- [ ] Room cacheado, Retrofit para network, ambos en paralelo
- [ ] Tests para ViewModel, Repository, DAOs
- [ ] Manejo de errores y retry logic
- [ ] Material 3 + temas dinámicos
- [ ] Offline-first: mostrar local, actualizar con remote

---

## 🚀 Próximas Fases (Post Semana 10)

- **Semana 11:** Firebase Authentication + Firestore
- **Semana 12:** Google Maps SDK
- **Semana 13:** Animaciones con `animateAsState()` y `Animatable`
- **Semana 14:** Widgets con Jetpack Glance
- **Semana 15:** Notificaciones + WorkManager
- **Semana 16:** Sustentación final

---

## 📚 Referencias Oficiales

- [Google Architecture Guide](https://developer.android.com/topic/architecture)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Room Persistence](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)
- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose State Management](https://developer.android.com/jetpack/compose/state)
- [Testing in Compose](https://developer.android.com/jetpack/compose/testing)

---

**Última actualización:** 2026-10-12  
**Versión:** 1.0  
**Autores:** Gabriel (AllinEdu Team) + Claude AI
