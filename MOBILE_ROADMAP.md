# Roadmap Móvil AllinEdu — Ejecución Semana por Semana

**Duración:** 16 semanas (alineado con curso Kotlin TECSUP)  
**Objetivo:** App fluida, de alto nivel, con arquitectura profesional  
**Stack:** Kotlin + Jetpack Compose + Navigation + Room + Retrofit + Material Design 3

---

## Semana 1-4: ✅ YA COMPLETADAS (Fundamentals)

- ✅ Kotlin esencial + POO
- ✅ Jetpack Compose (layouts, Material3)
- ✅ Estado en Compose
- ✅ Git + control de versiones

**Status:** Proyecto actual tiene base Compose sólida.

---

## Semana 5: Navigation Compose (Multi-Screen) — PRÓXIMA

**Tema del curso:** Navegación entre múltiples vistas  
**Duración estimada:** 1-2 días  
**Objetivo:** Poder navegar entre pantallas sin perder estado

### ✅ Checklist

- [ ] **Agregar dependencia**
  ```gradle
  implementation("androidx.navigation:navigation-compose:2.7.7")
  ```

- [ ] **Crear `navigation/NavigationRoutes.kt`**
  ```kotlin
  object Routes {
      const val LOGIN = "login"
      const val SECTIONS = "sections"
      const val ATTENDANCE = "attendance/{sectionId}"
      const val PLANNING = "planning/{sectionId}"
  }
  ```

- [ ] **Crear `navigation/NavGraph.kt`**
  ```kotlin
  @Composable
  fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
      NavHost(
          navController = navController,
          startDestination = Routes.LOGIN,
          modifier = modifier
      ) {
          composable(Routes.LOGIN) {
              LoginScreen(
                  onNavigateToSections = {
                      navController.navigate(Routes.SECTIONS) {
                          popUpTo(Routes.LOGIN) { inclusive = true }
                      }
                  }
              )
          }

          composable(Routes.SECTIONS) {
              SectionsScreen(
                  onNavigateToAttendance = { sectionId ->
                      navController.navigate(Routes.ATTENDANCE.replace("{sectionId}", sectionId))
                  },
                  onNavigateToPlanning = { sectionId ->
                      navController.navigate(Routes.PLANNING.replace("{sectionId}", sectionId))
                  }
              )
          }

          composable(
              Routes.ATTENDANCE,
              arguments = listOf(navArgument("sectionId") { type = NavType.StringType })
          ) { backStackEntry ->
              val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
              AttendanceScreen(
                  sectionId = sectionId,
                  onNavigateBack = { navController.popBackStack() }
              )
          }
      }
  }
  ```

- [ ] **Actualizar MainActivity**
  ```kotlin
  @Composable
  fun MyApp() {
      val navController = rememberNavController()
      
      MyAppTheme {
          AppNavHost(navController = navController)
      }
  }
  ```

- [ ] **Refactorizar screens** para aceptar NavController
  - LoginScreen debe navegar a SectionsScreen
  - SectionsScreen debe navegar a AttendanceScreen
  - Todos deben tener botón "back"

- [ ] **Ejecutar y verificar**
  ```bash
  ./gradlew installDebug
  # Navegar entre pantallas sin perder estado
  ```

### 📚 Referencias
- [Navigation Compose Codelab](https://developer.android.com/codelabs/jetpack-compose-navigation)
- [Type-Safe Navigation](https://developer.android.com/jetpack/compose/navigation#type-safety)

---

## Semana 7: Room + SQLite (Persistencia Local) — PRIORIDAD

**Tema del curso:** Guardar datos en BD local  
**Duración estimada:** 2-3 días  
**Objetivo:** Cachear datos localmente, funcionar offline

### ✅ Checklist

- [ ] **Agregar dependencias**
  ```gradle
  val roomVersion = "2.6.1"
  implementation("androidx.room:room-runtime:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  ```

- [ ] **Crear Entidades (`data/datasource/local/database/entities/`)**
  
  **StudentEntity.kt**
  ```kotlin
  @Entity(tableName = "students")
  data class StudentEntity(
      @PrimaryKey val id: Long,
      val name: String,
      val gradeId: Long,
      val sectionId: Long,
      val enrollmentDate: String
  )
  ```

  **SectionEntity.kt**
  ```kotlin
  @Entity(tableName = "sections")
  data class SectionEntity(
      @PrimaryKey val id: Long,
      val name: String,
      val gradeId: Long
  )
  ```

  **AttendanceEntity.kt**
  ```kotlin
  @Entity(
      tableName = "attendance",
      indices = [Index(value = ["section_id", "date"], unique = true)]
  )
  data class AttendanceEntity(
      @PrimaryKey(autoGenerate = true) val id: Long = 0,
      @ColumnInfo(name = "section_id") val sectionId: Long,
      @ColumnInfo(name = "student_id") val studentId: Long,
      val date: String,
      val status: String, // "PRESENTE", "AUSENTE"
      val confirmedAt: String? = null
  )
  ```

- [ ] **Crear DAOs (`data/datasource/local/database/dao/`)**

  **StudentDao.kt**
  ```kotlin
  @Dao
  interface StudentDao {
      @Query("SELECT * FROM students WHERE section_id = :sectionId")
      fun getBySectionId(sectionId: Long): Flow<List<StudentEntity>>

      @Upsert
      suspend fun save(student: StudentEntity)

      @Upsert
      suspend fun saveAll(students: List<StudentEntity>)

      @Query("DELETE FROM students WHERE section_id = :sectionId")
      suspend fun deleteBySectionId(sectionId: Long)
  }
  ```

  **AttendanceDao.kt**
  ```kotlin
  @Dao
  interface AttendanceDao {
      @Query("SELECT * FROM attendance WHERE section_id = :sectionId AND date = :date")
      fun getByDateAndSection(sectionId: Long, date: String): Flow<List<AttendanceEntity>>

      @Upsert
      suspend fun save(attendance: AttendanceEntity)

      @Upsert
      suspend fun saveAll(attendances: List<AttendanceEntity>)
  }
  ```

- [ ] **Crear AppDatabase.kt**
  ```kotlin
  @Database(
      entities = [StudentEntity::class, SectionEntity::class, AttendanceEntity::class],
      version = 1,
      exportSchema = false
  )
  abstract class AppDatabase : RoomDatabase() {
      abstract fun studentDao(): StudentDao
      abstract fun sectionDao(): SectionDao
      abstract fun attendanceDao(): AttendanceDao

      companion object {
          @Volatile
          private var INSTANCE: AppDatabase? = null

          fun getInstance(context: Context): AppDatabase {
              return INSTANCE ?: synchronized(this) {
                  Room.databaseBuilder(
                      context.applicationContext,
                      AppDatabase::class.java,
                      "edufast.db"
                  ).build().also { INSTANCE = it }
              }
          }
      }
  }
  ```

- [ ] **Crear LocalDataSource para cada feature**
  ```kotlin
  class AttendanceLocalDataSource @Inject constructor(
      private val attendanceDao: AttendanceDao
  ) {
      fun getByDate(sectionId: Long, date: String): Flow<List<AttendanceEntity>> {
          return attendanceDao.getByDateAndSection(sectionId, date)
      }

      suspend fun save(attendance: AttendanceEntity) {
          attendanceDao.save(attendance)
      }
  }
  ```

- [ ] **Actualizar Repository** (combina local + remote)
  ```kotlin
  class AttendanceRepository @Inject constructor(
      private val localDataSource: AttendanceLocalDataSource,
      private val remoteDataSource: AttendanceRemoteDataSource
  ) {
      fun getAttendance(sectionId: Long, date: String): Flow<List<Attendance>> {
          return localDataSource.getByDate(sectionId, date).map { entities ->
              entities.map { it.toDomain() }
          }
      }

      suspend fun registerAttendance(sectionId: Long, studentId: Long, isPresent: Boolean) {
          val entity = AttendanceEntity(
              sectionId = sectionId,
              studentId = studentId,
              date = LocalDate.now().toString(),
              status = if (isPresent) "PRESENTE" else "AUSENTE"
          )
          localDataSource.save(entity)
          // Luego sync con backend (próximo paso)
      }
  }
  ```

- [ ] **Crear Hilt Module para DB**
  ```kotlin
  @Module
  @InstallIn(SingletonComponent::class)
  object DatabaseModule {
      @Provides
      @Singleton
      fun provideAppDatabase(context: Context): AppDatabase {
          return AppDatabase.getInstance(context)
      }

      @Provides
      @Singleton
      fun provideAttendanceDao(db: AppDatabase): AttendanceDao = db.attendanceDao()

      @Provides
      @Singleton
      fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()
  }
  ```

- [ ] **Ejecutar y verificar**
  ```bash
  ./gradlew installDebug
  # Registrar asistencia
  # Cerrar app
  # Abrir app
  # Los datos deben estar ahí (offline-first)
  ```

### 📚 Referencias
- [Room Persistence](https://developer.android.com/training/data-storage/room)
- [Room Codelab](https://developer.android.com/codelabs/android-room-with-a-view-kotlin)

---

## Semana 8: MVVM Formal + UDF — CRÍTICA

**Tema del curso:** Arquitectura MVVM + gestión de estado  
**Duración estimada:** 2-3 días  
**Objetivo:** Separar UI de lógica, state unidireccional

### ✅ Checklist

- [ ] **Crear UiState data classes** (uno por screen)

  **LoginUiState.kt**
  ```kotlin
  data class LoginUiState(
      val email: String = "",
      val password: String = "",
      val isLoading: Boolean = false,
      val error: String? = null,
      val success: Boolean = false
  )
  ```

  **AttendanceUiState.kt**
  ```kotlin
  data class AttendanceUiState(
      val sectionId: String = "",
      val date: String = LocalDate.now().toString(),
      val students: List<StudentUi> = emptyList(),
      val selected: Map<Long, Boolean> = emptyMap(),
      val isLoading: Boolean = false,
      val isSaving: Boolean = false,
      val error: String? = null,
      val success: Boolean = false
  )

  data class StudentUi(
      val id: Long,
      val name: String,
      val isPresent: Boolean
  )
  ```

- [ ] **Crear sealed classes para Events** (UiAction)

  **AttendanceUiAction.kt**
  ```kotlin
  sealed class AttendanceUiAction {
      data class LoadStudents(val sectionId: String) : AttendanceUiAction()
      data class TogglePresence(val studentId: Long) : AttendanceUiAction()
      object SaveAttendance : AttendanceUiAction()
      object Retry : AttendanceUiAction()
  }
  ```

- [ ] **Refactorizar ViewModels** para exponer StateFlow

  **AttendanceViewModel.kt**
  ```kotlin
  @HiltViewModel
  class AttendanceViewModel @Inject constructor(
      private val attendanceRepository: AttendanceRepository,
      private val savedStateHandle: SavedStateHandle
  ) : ViewModel() {

      private val _state = MutableStateFlow(AttendanceUiState())
      val state: StateFlow<AttendanceUiState> = _state.asStateFlow()

      fun handleAction(action: AttendanceUiAction) {
          when (action) {
              is AttendanceUiAction.LoadStudents -> loadStudents(action.sectionId)
              is AttendanceUiAction.TogglePresence -> togglePresence(action.studentId)
              AttendanceUiAction.SaveAttendance -> saveAttendance()
              AttendanceUiAction.Retry -> loadStudents(_state.value.sectionId)
          }
      }

      private fun loadStudents(sectionId: String) {
          viewModelScope.launch {
              _state.update { it.copy(isLoading = true, error = null) }
              try {
                  attendanceRepository.getStudents(sectionId).collect { students ->
                      _state.update { it.copy(students = students, isLoading = false) }
                  }
              } catch (e: Exception) {
                  _state.update { it.copy(error = e.message, isLoading = false) }
              }
          }
      }

      private fun togglePresence(studentId: Long) {
          val current = _state.value.selected.toMutableMap()
          current[studentId] = !(current[studentId] ?: false)
          _state.update { it.copy(selected = current) }
      }

      private fun saveAttendance() {
          viewModelScope.launch {
              _state.update { it.copy(isSaving = true) }
              try {
                  attendanceRepository.saveAttendance(_state.value)
                  _state.update { it.copy(isSaving = false, success = true) }
              } catch (e: Exception) {
                  _state.update { it.copy(isSaving = false, error = e.message) }
              }
          }
      }
  }
  ```

- [ ] **Conectar Composables a ViewModel**

  **AttendanceScreen.kt**
  ```kotlin
  @Composable
  fun AttendanceScreen(
      sectionId: String,
      viewModel: AttendanceViewModel = hiltViewModel(),
      onNavigateBack: () -> Unit
  ) {
      val state by viewModel.state.collectAsState()

      LaunchedEffect(sectionId) {
          viewModel.handleAction(AttendanceUiAction.LoadStudents(sectionId))
      }

      when {
          state.isLoading -> LoadingScreen()
          state.error != null -> ErrorScreen(
              message = state.error,
              onRetry = { viewModel.handleAction(AttendanceUiAction.Retry) }
          )
          else -> AttendanceContent(
              state = state,
              onTogglePresence = { studentId ->
                  viewModel.handleAction(AttendanceUiAction.TogglePresence(studentId))
              },
              onSave = {
                  viewModel.handleAction(AttendanceUiAction.SaveAttendance)
              }
          )
      }
  }

  @Composable
  fun AttendanceContent(
      state: AttendanceUiState,
      onTogglePresence: (Long) -> Unit,
      onSave: () -> Unit,
      modifier: Modifier = Modifier
  ) {
      Scaffold(
          topBar = { TopAppBar(title = { Text("Asistencia - ${state.date}") }) },
          floatingActionButton = {
              FloatingActionButton(onClick = onSave, enabled = !state.isSaving) {
                  if (state.isSaving) CircularProgressIndicator(color = Color.White)
                  else Icon(Icons.Default.Check, "Guardar")
              }
          }
      ) { padding ->
          LazyColumn(modifier = Modifier.padding(padding)) {
              items(state.students) { student ->
                  AttendanceCard(
                      student = student,
                      isPresent = state.selected[student.id] ?: false,
                      onToggle = { onTogglePresence(student.id) }
                  )
              }
          }
      }
  }
  ```

- [ ] **Crear tests para ViewModel**
  ```kotlin
  @Test
  fun `when loading students, state updates loading then success`() = runTest {
      val mockRepository = mockk<AttendanceRepository>()
      coEvery { mockRepository.getStudents("123") } returns flowOf(listOf(studentStub))

      val viewModel = AttendanceViewModel(mockRepository, SavedStateHandle())
      viewModel.handleAction(AttendanceUiAction.LoadStudents("123"))

      advanceUntilIdle()
      val state = viewModel.state.value
      assertEquals(false, state.isLoading)
      assertEquals(1, state.students.size)
  }
  ```

### 📚 Referencias
- [MVVM Architecture](https://developer.android.com/topic/architecture/mvvm)
- [UDF in Jetpack Compose](https://developer.android.com/jetpack/compose/state-hoisting)

---

## Semana 9: Retrofit Integration (Reemplaza Ktor) — CRÍTICA

**Tema del curso:** Consumir API remota con Retrofit  
**Duración estimada:** 2-3 días  
**Objetivo:** Comunicar con backend Spring Boot

### ✅ Checklist

- [ ] **Reemplazar dependencias**
  ```gradle
  // Quitar:
  // implementation("io.ktor:ktor-client-android:3.0.3")
  
  // Agregar:
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  ```

- [ ] **Crear DTOs** (`data/datasource/remote/dtos/`)

  **LoginRequest.kt**
  ```kotlin
  @Serializable
  data class LoginRequest(
      val email: String,
      val password: String
  )

  @Serializable
  data class LoginResponse(
      val token: String,
      val userId: Long,
      val email: String
  )
  ```

  **StudentDto.kt**
  ```kotlin
  @Serializable
  data class StudentDto(
      val id: Long,
      val name: String,
      val firstName: String,
      val lastName: String,
      val sectionId: Long
  )
  ```

- [ ] **Crear ApiService.kt**
  ```kotlin
  interface ApiService {
      @POST("api/v1/auth/login")
      suspend fun login(@Body request: LoginRequest): LoginResponse

      @GET("api/v1/sections/{sectionId}/students")
      suspend fun getStudents(@Path("sectionId") sectionId: Long): List<StudentDto>

      @POST("api/v1/attendance")
      suspend fun registerAttendance(@Body request: AttendanceRequest): AttendanceResponse

      @GET("api/v1/attendance/{sectionId}")
      suspend fun getAttendance(
          @Path("sectionId") sectionId: Long,
          @Query("date") date: String
      ): List<AttendanceDto>
  }
  ```

- [ ] **Crear ApiClient.kt** (Singleton Retrofit)
  ```kotlin
  @Singleton
  class ApiClient(context: Context) {
      private val okHttpClient = OkHttpClient.Builder()
          .addInterceptor(LoggingInterceptor().apply { setLevel(HttpLoggingLevel.BODY) })
          .addInterceptor { chain ->
              val original = chain.request()
              val token = getTokenFromPreferences(context) // SharedPreferences
              val request = original.newBuilder()
                  .header("Authorization", "Bearer $token")
                  .build()
              chain.proceed(request)
          }
          .build()

      val retrofit: Retrofit = Retrofit.Builder()
          .baseUrl(BuildConfig.API_BASE_URL) // http://localhost:8080/
          .client(okHttpClient)
          .addConverterFactory(
              Json {
                  ignoreUnknownKeys = true
                  coerceInputValues = true
              }.asConverterFactory("application/json".toMediaType())
          )
          .build()

      fun <T> create(service: Class<T>): T = retrofit.create(service)
  }
  ```

- [ ] **Crear Hilt Module para Network**
  ```kotlin
  @Module
  @InstallIn(SingletonComponent::class)
  object NetworkModule {
      @Provides
      @Singleton
      fun provideApiClient(context: Context): ApiClient = ApiClient(context)

      @Provides
      @Singleton
      fun provideApiService(apiClient: ApiClient): ApiService {
          return apiClient.retrofit.create(ApiService::class.java)
      }
  }
  ```

- [ ] **Crear RemoteDataSource**
  ```kotlin
  class AttendanceRemoteDataSource @Inject constructor(
      private val apiService: ApiService
  ) {
      suspend fun getAttendance(sectionId: Long, date: String): List<AttendanceDto> {
          return apiService.getAttendance(sectionId, date)
      }

      suspend fun registerAttendance(request: AttendanceRequest) {
          apiService.registerAttendance(request)
      }
  }
  ```

- [ ] **Actualizar Repository** para usar Retrofit
  ```kotlin
  class AttendanceRepository @Inject constructor(
      private val local: AttendanceLocalDataSource,
      private val remote: AttendanceRemoteDataSource
  ) {
      suspend fun registerAttendance(sectionId: Long, studentId: Long, isPresent: Boolean) {
          try {
              // Enviar al backend
              remote.registerAttendance(
                  AttendanceRequest(sectionId, studentId, isPresent)
              )
              // Cachear localmente
              local.save(AttendanceEntity(...))
          } catch (e: Exception) {
              // Cachear localmente de todas formas (offline-first)
              local.save(AttendanceEntity(...))
              throw e
          }
      }
  }
  ```

- [ ] **Ejecutar y verificar**
  ```bash
  # Asegúrate que el backend está corriendo:
  ./gradlew bootRun  # En otra terminal, desde backend/

  # Instalar app
  ./gradlew installDebug

  # Logs
  adb logcat | grep Retrofit
  ```

### 📚 Referencias
- [Retrofit Official](https://square.github.io/retrofit/)
- [Consuming APIs with Retrofit](https://developer.android.com/codelabs/obtaining-data)

---

## Semana 10: CRUD + Listas Optimizadas

**Tema del curso:** Integrar CRUD + mostrar datos en listas  
**Duración estimada:** 1-2 días  
**Objetivo:** Create, Read, Update en una pantalla completa

### ✅ Checklist

- [ ] **Implementar LazyColumn** para listas eficientes
- [ ] **Pull-to-refresh** con `pullRefresh()`
- [ ] **Paginación** si hay muchos registros
- [ ] **Optimistic updates** (actualizar UI antes de confirmar en backend)
- [ ] **Eliminar de la lista** después de delete confirmado

### 📚 Referencias
- [LazyColumn Best Practices](https://developer.android.com/jetpack/compose/lists)
- [Pull-to-Refresh](https://m3.material.io/components/search)

---

## Semana 11-15: Avanzado (Firebase, Maps, Animaciones, Widgets, Notificaciones)

Ver [ARCHITECTURE_MOBILE.md](ARCHITECTURE_MOBILE.md) § **Próximas Fases (Post Semana 10)**

---

## Semana 16: Sustentación Final

Presentar app funcional completa con:
- ✅ Arquitectura MVVM limpia
- ✅ Navegación fluida
- ✅ Persistencia local (Room)
- ✅ Conectividad remota (Retrofit)
- ✅ UI Material Design 3
- ✅ Tests
- ✅ Offline-first experience

---

## 🚀 Cómo Empezar AHORA (Semana 5)

```bash
cd /home/gabriel/Escritorio/Repos/AllinEdu/mobile

# 1. Actualizar build.gradle.kts con Navigation
#    (Copiar dependencia de arriba)

# 2. Crear carpeta navigation/
mkdir -p app/src/main/kotlin/com/edufast/mobile/navigation

# 3. Crear NavigationRoutes.kt y NavGraph.kt
#    (Usar código del checklist de Semana 5)

# 4. Actualizar MainActivity.kt
#    (Usar código del checklist de Semana 5)

# 5. Compilar y probar
./gradlew clean build
./gradlew installDebug
```

---

## 📊 Progreso (Actualizar mientras avanzas)

| Semana | Tema | Status | PR |
|--------|------|--------|-----|
| 1-4 | Fundamentals | ✅ Hecho | - |
| 5 | Navigation Compose | ⏳ En progreso | - |
| 7 | Room + SQLite | ⬜ Pendiente | - |
| 8 | MVVM + UDF | ⬜ Pendiente | - |
| 9 | Retrofit | ⬜ Pendiente | - |
| 10 | CRUD + Listas | ⬜ Pendiente | - |
| 11-15 | Avanzado | ⬜ Pendiente | - |
| 16 | Sustentación | ⬜ Pendiente | - |

---

**Última actualización:** 2026-10-12  
**Versión:** 1.0  
**Autores:** Gabriel + Claude AI
