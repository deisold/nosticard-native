# NostiCard Android Implementation Guide

## 🏗️ **Modern Android Architecture**

This document outlines the implementation of NostiCard using state-of-the-art Android development practices:

- **UI Framework**: Jetpack Compose
- **Architecture**: MVI (Model-View-Intent) with clean architecture
- **Language**: Kotlin with Coroutines
- **Dependency Injection**: Koin
- **State Management**: StateFlow + Compose State
- **Image Processing**: Custom Canvas + Skia integration
- **Storage**: Room Database + DataStore
- **Networking**: Retrofit + OkHttp
- **Testing**: JUnit5, MockK, Compose Testing

## 📱 **Core Features Implementation Status**

### 🖼️ **Photo Selection**
- ✅ Gallery picker integration (`PhotoPickerScreen`)
- ✅ Camera capture with permissions (`PhotoPickerScreen`)
- ✅ Image preview before editing
- ✅ Permission handling (iOS/Android)
- ✅ Image validation and error handling

### 🎨 **Filters & Effects**
- ✅ Filter system architecture (`PackService`)
- ✅ Free filter presets (Classic B&W, Sepia, Faded Color)
- ✅ Pro filter gating mechanism
- ⚠️ **Skia filter implementation** (stub only)
- ⚠️ **Real-time preview rendering**
- ✅ Filter selection UI (`EditorScreen`)

### 🖼️ **Frames**
- ✅ Frame system architecture
- ✅ Frame options (White Border, Deckle Edge, Stamp Edge)
- ✅ Pro frame restrictions
- ⚠️ **Frame rendering implementation** (stub only)
- ✅ Frame selection UI

### ✏️ **Text Editing**
- ✅ Text input with character limits (220 chars)
- ✅ Character counter with warnings (200 chars)
- ✅ Font selection system
- ⚠️ **Draggable text positioning** (not implemented)
- ⚠️ **Text alignment controls** (left/center/right)
- ⚠️ **Text shadow/backdrop options**
- ✅ Handwriting fonts configuration

### 🌟 **Vintage Effects**
- ✅ Scratch randomization system
- ✅ Grain level controls
- ✅ Vignette level controls
- ⚠️ **Dust overlay implementation** (stub only)
- ⚠️ **Aging effects rendering**

### 📱 **Preview & Export**
- ❌ **PreviewScreen** (not implemented)
- ⚠️ **Export functionality** (service stub only)
- ⚠️ **High-resolution rendering**
- ⚠️ **PDF export** (Pro feature)
- ⚠️ **Watermark application**
- ✅ Export quality configuration

### 📤 **Sharing**
- ✅ Share service architecture
- ✅ Native share sheet integration
- ⚠️ **Platform-specific sharing** (stub implementation)
- ✅ Share message generation
- ⚠️ **Share analytics tracking**

---

## 🏗️ **Architecture Components**

### 📐 **Modern Android MVI Architecture**

The Android implementation follows **Clean Architecture with MVI pattern**, leveraging Kotlin coroutines and Jetpack Compose for reactive UI.

#### **🎯 Core Design Principles**

**1. Feature Module Structure**
Each feature follows this modular architecture:

```
feature/
├── data/
│   ├── repository/
│   │   └── [Feature]RepositoryImpl.kt
│   ├── datasource/
│   │   ├── local/
│   │   └── remote/
│   └── mapper/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── [Feature]Screen.kt
│   ├── [Feature]ViewModel.kt
│   ├── [Feature]Intent.kt
│   ├── [Feature]State.kt
│   └── components/
└── di/
    └── [Feature]Module.kt
```

**2. MVI Components Definition**

```kotlin
// Intent - User actions
sealed interface [Feature]Intent {
    object Initialize : [Feature]Intent
    data class [Action](val param: Type) : [Feature]Intent
    object Refresh : [Feature]Intent
    data class NavigateTo(val destination: String) : [Feature]Intent
}

// State - UI state representation using sealed classes
sealed class [Feature]State {
    object Loading : [Feature]State()
    object Empty : [Feature]State()
    
    data class Success(
        val data: List<DataModel>,
        val isRefreshing: Boolean = false
    ) : [Feature]State()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : [Feature]State()
}

// ViewModel - Business logic coordinator with Koin
class [Feature]ViewModel(
    private val useCase: [Feature]UseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow<[Feature]State>([Feature]State.Loading)
    val state: StateFlow<[Feature]State> = _state.asStateFlow()
    
    fun handleIntent(intent: [Feature]Intent) {
        when (intent) {
            is [Feature]Intent.Initialize -> initialize()
            is [Feature]Intent.[Action] -> handle[Action](intent.param)
            is [Feature]Intent.Refresh -> refresh()
            is [Feature]Intent.NavigateTo -> handleNavigation(intent.destination)
        }
    }
    
    private fun initialize() {
        viewModelScope.launch {
            _state.value = [Feature]State.Loading
            useCase().collect { result ->
                _state.value = when {
                    result.isSuccess -> {
                        val data = result.getOrNull() ?: emptyList()
                        if (data.isEmpty()) [Feature]State.Empty
                        else [Feature]State.Success(data)
                    }
                    result.isFailure -> [Feature]State.Error(
                        message = result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                    else -> [Feature]State.Loading
                }
            }
        }
    }
}
```

**3. Compose Screen Implementation**

```kotlin
@Composable
fun [Feature]Screen(
    viewModel: [Feature]ViewModel = koinViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.handleIntent([Feature]Intent.Initialize)
    }
    
    [Feature]Content(
        state = state,
        onIntent = viewModel::handleIntent,
        onNavigate = onNavigate
    )
}

@Composable
private fun [Feature]Content(
    state: [Feature]State,
    onIntent: ([Feature]Intent) -> Unit,
    onNavigate: (String) -> Unit
) {
    when (state) {
        is [Feature]State.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is [Feature]State.Empty -> {
            EmptyStateComponent(
                message = "No data available",
                onRetry = { onIntent([Feature]Intent.Refresh) }
            )
        }
        
        is [Feature]State.Success -> {
            LazyColumn {
                items(state.data) { item ->
                    [Feature]Item(
                        item = item,
                        onClick = { onIntent([Feature]Intent.[Action](item.id)) }
                    )
                }
            }
            
            if (state.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        is [Feature]State.Error -> {
            ErrorStateComponent(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = { onIntent([Feature]Intent.Refresh) }
            )
        }
    }
}
```

**4. Repository Pattern Implementation**

```kotlin
// Repository Interface (Domain Layer)
interface [Feature]Repository {
    suspend fun get[Data](): Flow<List<DataModel>>
    suspend fun save[Data](data: DataModel): Result<Unit>
}

// Repository Implementation (Data Layer)
class [Feature]RepositoryImpl(
    private val localDataSource: [Feature]LocalDataSource,
    private val remoteDataSource: [Feature]RemoteDataSource,
    private val mapper: [Data]Mapper
) : [Feature]Repository {
    
    override suspend fun get[Data](): Flow<List<DataModel>> = flow {
        try {
            val localData = localDataSource.get[Data]()
            emit(localData.map { mapper.toDomain(it) })
            
            val remoteData = remoteDataSource.get[Data]()
            localDataSource.save[Data](remoteData)
            emit(remoteData.map { mapper.toDomain(it) })
        } catch (e: Exception) {
            emit(localDataSource.get[Data]().map { mapper.toDomain(it) })
        }
    }
}
```

**5. Use Case Implementation**

```kotlin
class [Feature]UseCase(
    private val repository: [Feature]Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Flow<Result<List<DataModel>>> = 
        repository.get[Data]()
            .flowOn(dispatcher)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
```

**6. Koin Dependency Injection**

```kotlin
// Koin Modules
val databaseModule = module {
    single<NostiCardDatabase> {
        Room.databaseBuilder(
            androidContext(),
            NostiCardDatabase::class.java,
            "nosticard_database"
        ).build()
    }
    
    single { get<NostiCardDatabase>().postcardDao() }
    single { get<NostiCardDatabase>().filterDao() }
}

val serviceModule = module {
    single<ImageProcessor> { SkiaImageProcessor() }
    single<ConfigurationManager> { ConfigurationManager(androidContext()) }
    single<PostcardMapper> { PostcardMapper() }
    single<AppStateManager> { 
        AppStateManager(
            entitlementsRepository = get(),
            preferencesRepository = get(),
            postcardRepository = get()
        )
    }
}

val repositoryModule = module {
    single<PostcardLocalDataSource> {
        PostcardLocalDataSourceImpl(dao = get())
    }
    
    single<PostcardRepository> {
        PostcardRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(), // if needed
            mapper = get()
        )
    }
    
    single<FilterRepository> {
        FilterRepositoryImpl(
            localDataSource = get(),
            mapper = get()
        )
    }
    
    single<EntitlementsRepository> {
        EntitlementsRepositoryImpl(
            dataStore = get(),
            mapper = get()
        )
    }
    
    single<PreferencesRepository> {
        PreferencesRepositoryImpl(
            dataStore = get()
        )
    }
}

val useCaseModule = module {
    factory<GetPostcardsUseCase> { 
        GetPostcardsUseCase(
            repository = get()
        )
    }
    factory<SavePostcardUseCase> { 
        SavePostcardUseCase(
            repository = get()
        )
    }
    factory<ProcessImageUseCase> { 
        ProcessImageUseCase(
            imageProcessor = get()
        )
    }
    factory<GetFiltersUseCase> { 
        GetFiltersUseCase(
            repository = get()
        )
    }
    factory<ApplyFilterUseCase> { 
        ApplyFilterUseCase(
            imageProcessor = get(),
            filterRepository = get()
        )
    }
}

val viewModelModule = module {
    factory<HomeViewModel> { 
        HomeViewModel(
            getPostcardsUseCase = get(),
            savePostcardUseCase = get()
        )
    }
    
    factory<EditorViewModel> { 
        EditorViewModel(
            processImageUseCase = get(),
            applyFilterUseCase = get(),
            savePostcardUseCase = get()
        )
    }
    
    factory<PreviewViewModel> {
        PreviewViewModel(
            processImageUseCase = get(),
            shareUseCase = get()
        )
    }
}

// Koin Testing Module
val testModule = module {
    factory<PostcardRepository> { mockk<PostcardRepository>() }
    factory<ImageProcessor> { mockk<ImageProcessor>() }
    factory<GetPostcardsUseCase> { 
        GetPostcardsUseCase(
            repository = get()
        )
    }
}

// Usage in tests
class HomeViewModelTest : KoinTest {
    
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(testModule)
    }
    
    @Test
    fun `should load postcards successfully`() = runTest {
        val repository: PostcardRepository by inject()
        val saveUseCase: SavePostcardUseCase by inject()
        val viewModel = HomeViewModel(
            getPostcardsUseCase = GetPostcardsUseCase(repository),
            savePostcardUseCase = saveUseCase
        )
        
        // Test implementation
        viewModel.handleIntent(HomeIntent.Initialize)
        // Assert states...
    }
}

// Application class
class NostiCardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@NostiCardApplication)
            modules(
                databaseModule,
                serviceModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }
    }
}
```

**7. Koin Best Practices**

```kotlin
// Feature-based module organization
val homeFeatureModule = module {
    scope<HomeActivity> {
        scoped<HomeViewModel> { 
            HomeViewModel(
                getPostcardsUseCase = get(),
                savePostcardUseCase = get()
            )
        }
    }
}

// Qualifier for different implementations
val networkModule = module {
    single<ApiService>(named("production")) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .build()
            .create(ApiService::class.java)
    }
    
    single<ApiService>(named("mock")) {
        MockApiService()
    }
}

// Environment-based injection
val environmentModule = module {
    single<ApiService> {
        if (BuildConfig.DEBUG) {
            get(named("mock"))
        } else {
            get(named("production"))
        }
    }
}

// Property injection example
class MyRepository : KoinComponent {
    private val apiService: ApiService by inject()
    private val database: Database by inject()
    
    suspend fun getData() = apiService.getData()
}
```

**8. State Management & Error Handling**

```kotlin
// Global App State using sealed classes
sealed class AppState {
    object Initializing : AppState()
    
    data class Ready(
        val entitlements: Entitlements,
        val preferences: UserPreferences,
        val cardCount: Int
    ) : AppState()
    
    data class Error(val message: String) : AppState()
}

class AppStateManager(
    private val entitlementsRepository: EntitlementsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val postcardRepository: PostcardRepository
) {
    private val _appState = MutableStateFlow<AppState>(AppState.Initializing)
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    suspend fun initialize() {
        try {
            val entitlements = entitlementsRepository.getEntitlements()
            val preferences = preferencesRepository.getPreferences()
            val cardCount = postcardRepository.getCardCount()
            
            _appState.value = AppState.Ready(
                entitlements = entitlements,
                preferences = preferences,
                cardCount = cardCount
            )
        } catch (e: Exception) {
            _appState.value = AppState.Error(e.message ?: "Failed to initialize app")
        }
    }
    
    fun updateEntitlements(entitlements: Entitlements) {
        val currentState = _appState.value
        if (currentState is AppState.Ready) {
            _appState.value = currentState.copy(entitlements = entitlements)
        }
    }
}

// Error Handling with sealed classes
sealed class AppError : Exception() {
    data class NetworkError(override val message: String) : AppError()
    data class StorageError(override val message: String) : AppError()
    data class ProcessingError(override val message: String) : AppError()
    data class ValidationError(override val message: String) : AppError()
}

// Extension for safe operations
suspend fun <T> safeCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: AppError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AppError.NetworkError(e.message ?: "Unknown error"))
    }
}
```

#### **🚀 Implementation Guidelines**

**Required Files per Feature:**
1. `[Feature]Screen.kt` - Compose UI
2. `[Feature]ViewModel.kt` - Business logic
3. `[Feature]Intent.kt` - User actions
4. `[Feature]State.kt` - UI state
5. `[Feature]Repository.kt` - Data layer interface
6. `[Feature]UseCase.kt` - Business use cases

**Naming Conventions:**
- Screens: `[Feature]Screen`
- ViewModels: `[Feature]ViewModel`
- States: `[Feature]State`
- Intents: `[Feature]Intent`
- Use Cases: `[Action][Feature]UseCase`

**Error Handling:**
- Use `Result<T>` for operations that can fail
- Display errors using Snackbar or Dialog
- Loading states managed in State data class

**Navigation Pattern:**
- Use Navigation Compose
- Navigation triggered via callback functions
- Deep links support for external navigation

**Async Operations:**
- All async operations use Coroutines
- StateFlow for reactive state updates
- Proper cancellation handling in ViewModels

#### **📋 Configuration Management**

**Build Configuration:**
- Use BuildConfig for compile-time constants
- Gradle build variants for different environments
- Secrets in local.properties or environment variables

**Configuration Pattern:**
```kotlin
// Build Configuration
object AppConfig {
    const val API_URL = BuildConfig.API_URL
    const val IAP_PRO_PRODUCT_ID = "com.nosticard.pro"
    const val IAP_STYLE_PACK_VINTAGE_ID = "com.nosticard.pack.vintage"
    const val MAX_CARDS_LIMIT = 200
    const val AUTO_SAVE_DEBOUNCE_MS = 1500L
}

// DataStore for Runtime Configuration
@Singleton
class ConfigurationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.createDataStore("app_config")
    
    val exportQuality = dataStore.data.map { it.exportQuality ?: ExportQuality.HD }
    
    suspend fun updateExportQuality(quality: ExportQuality) {
        dataStore.updateData { it.copy(exportQuality = quality) }
    }
}
```

#### **✅ Compliance Checklist**

For each new feature, ensure:
- [ ] Clean Architecture layers properly separated
- [ ] MVI pattern implemented (Intent → ViewModel → State → UI)
- [ ] Koin modules properly defined for dependency injection
- [ ] Repository pattern for data access
- [ ] Use Cases for business logic
- [ ] StateFlow/Flow for reactive programming
- [ ] Sealed classes for State representation
- [ ] Proper when-expression handling for all state cases
- [ ] Compose UI with `koinViewModel()` for ViewModels
- [ ] Error handling with Result<T> and sealed error classes
- [ ] Navigation via callback functions
- [ ] Unit tests for ViewModels and Use Cases with Koin test module
- [ ] UI tests for Compose screens

#### **🔄 Data Flow**

```
User Input → Intent → ViewModel → Use Case → Repository → Data Source
     ↑                                ↓
Compose UI ←←← State ←←← StateFlow ←←←← Business Logic
     ↓
Side Effects (Navigation, Snackbar)
```

This architecture ensures:
- **Testability**: Each layer can be tested independently
- **Maintainability**: Clear separation of concerns with clean architecture
- **Scalability**: Modular feature-based structure
- **Predictability**: Unidirectional data flow with MVI
- **Performance**: Reactive UI updates with Compose
- **Error Handling**: Centralized error management with Result<T>

### 🗂️ **Screens Implementation**
- ✅ **HomeScreen** - Complete with stats, recent cards, navigation
- ✅ **PhotoPickerScreen** - Complete with gallery/camera selection
- ✅ **EditorScreen** - Complete with filters, frames, text editing
- ❌ **PreviewScreen** - Not implemented
- ❌ **MyCardsScreen** - Not implemented  
- ❌ **SettingsScreen** - Not implemented
- ❌ **OnboardingScreen** - Not implemented
- ❌ **UpgradeScreen** - Not implemented

### 🔧 **Services Layer**
- ✅ **ImageService** - Interface + stub implementation
- ✅ **StorageService** - Complete MMKV + FS implementation
- ✅ **IAPService** - Complete react-native-iap integration
- ✅ **ShareService** - Interface + basic implementation
- ✅ **PackService** - Complete style pack management
- ✅ **Service Provider** - Dependency injection complete

### 🗃️ **State Management**
- ✅ **AppStore** - Complete (entitlements, preferences, cards)
- ✅ **EditorStore** - Complete (current card, editing state)
- ✅ **Store Integration** - All screens connected

### 🎭 **ViewModels**
- ✅ **BaseViewModel** - EventEmitter foundation
- ✅ **HomeViewModel** - Complete (card management, cleanup)
- ✅ **EditorViewModel** - Complete (editing logic, auto-save)
- ⚠️ **ExportViewModel** - Partial (missing actual export)

### 🎨 **UI Components**
- ✅ **Common Components** - Button, Card, EmptyState
- ✅ **Theme System** - Complete vintage design system
- ✅ **Navigation** - Complete stack navigation
- ❌ **Card Components** - Missing card-specific UI

---

## 💾 **Storage & Data**

### 📚 **Local Storage**
- ✅ Card metadata storage (MMKV)
- ✅ Card data persistence (File System)
- ✅ Preferences storage
- ✅ Entitlements storage
- ✅ Auto-save functionality (1.5s debounce)
- ✅ Cleanup system (200 card limit)

### 📊 **Data Models**
- ✅ PostcardData interface
- ✅ PostcardMetadata interface
- ✅ FilterPreset interface
- ✅ StylePack interface
- ✅ AppEntitlements interface
- ✅ Error handling types

---

## 💰 **Monetization**

### 🔒 **In-App Purchases**
- ✅ IAP service integration
- ✅ Pro upgrade system ($9.99)
- ✅ Style pack system ($0.99-$2.99)
- ✅ Entitlements management
- ✅ Restore purchases
- ❌ **Purchase UI flows** (UpgradeScreen missing)

### 🎁 **Free vs Pro Features**
- ✅ Feature gating system
- ✅ Watermark logic for free users
- ✅ Export quality restrictions
- ✅ Filter/frame restrictions
- ⚠️ **Watermark rendering** (stub only)

---

## 🧪 **Testing & Quality**

### ✅ **Architecture Testing**
- ✅ Service interfaces for mocking
- ✅ Dependency injection setup
- ✅ Error handling system
- ✅ TypeScript strict configuration

### ⚠️ **Missing Tests**
- ❌ **Unit tests** for ViewModels
- ❌ **Service layer tests**
- ❌ **Component tests**
- ❌ **Integration tests**
- ❌ **E2E tests**

---

## 📱 **Platform Features**

### 🤖 **Android Specific**
- ✅ Permission handling setup
- ✅ Google Play billing setup
- ⚠️ **Native modules** (needs gradle setup)
- ❌ **Play Store configuration**

---

## 🎯 **Priority Implementation Roadmap**

### **Phase 1: Core MVP** (Critical - 2-3 weeks)
1. ❌ **Complete Skia filter implementation**
2. ❌ **Build PreviewScreen with export functionality**
3. ❌ **Implement actual image processing pipeline**
4. ❌ **Add MyCardsScreen for card management**

### **Phase 2: Polish & Features** (Important - 2 weeks)
5. ❌ **Build SettingsScreen**
6. ❌ **Implement draggable text positioning**
7. ❌ **Add real-time preview updates**
8. ❌ **Complete sharing functionality**

### **Phase 3: Monetization** (Business Critical - 1 week)
9. ❌ **Build UpgradeScreen with purchase flows**
10. ❌ **Implement watermark rendering**
11. ❌ **Add purchase analytics**
12. ❌ **Test IAP flows thoroughly**

### **Phase 4: User Experience** (Polish - 1 week)
13. ❌ **Build OnboardingScreen**
14. ❌ **Add micro-animations**
15. ❌ **Implement haptic feedback**
16. ❌ **Add accessibility features**

### **Phase 5: Platform Readiness** (Launch - 1 week)
17. ❌ **Native module setup (iOS/Android)**
18. ❌ **App store assets and configuration**
19. ❌ **Production build testing**
20. ❌ **Performance optimization**

---

## 📈 **Completion Metrics**

- **Architecture**: ✅ **100%** Complete
- **Core Features**: ⚠️ **40%** Complete  
- **UI/UX**: ⚠️ **60%** Complete
- **Monetization**: ⚠️ **30%** Complete
- **Testing**: ❌ **0%** Complete
- **Platform Ready**: ❌ **10%** Complete

**Overall MVP Progress**: 🚧 **60%** Complete

---

## 🚀 **Next Immediate Actions**

1. **Complete Skia filter pipeline** - Critical for core functionality
2. **Build PreviewScreen** - Required for MVP
3. **Implement actual image export** - Core user value
4. **Add MyCardsScreen** - Essential user experience
5. **Set up testing framework** - Quality assurance

The foundation is solid - now focus on completing the missing screens and image processing pipeline!

---

## 🚀 **Next Steps Implementation Roadmap**

### **Phase 1: Image Processing & Core Features** (Priority: Critical)

#### **1.1 Skia/Canvas Filter Effects** ⚠️
**Status**: Stub implementation only  
**Priority**: CRITICAL - Core app functionality  
**Estimated Time**: 2-3 weeks  

**Tasks:**
- [ ] Set up Skia integration for Android
- [ ] Implement Classic B&W filter using ColorMatrix
- [ ] Implement Sepia Memories filter with proper color transformation  
- [ ] Implement Faded Color filter with alpha adjustments
- [ ] Add vintage effects pipeline:
  - [ ] Randomized scratches generation
  - [ ] Dust overlay system
  - [ ] Grain texture application
  - [ ] Vignette effect implementation
- [ ] Optimize for performance (tiling for large images)
- [ ] Add real-time preview updates (<0.5s target)
- [ ] Memory management for 3-4GB devices

**Files to update:**
- `ImageRepositoryImpl.kt` - Complete filter implementations
- `EditorViewModel.kt` - Add real-time preview logic
- Add new `SkiaFilterProcessor.kt` utility class

#### **1.2 Frame Rendering Implementation** ⚠️  
**Status**: Stub implementation only  
**Priority**: HIGH - Visual appeal  
**Estimated Time**: 1 week  

**Tasks:**
- [ ] Implement White Border frame (simple padding)
- [ ] Implement Deckle Edge frame (irregular border effect)
- [ ] Implement Stamp Edge frame (perforated border effect)
- [ ] Add frame preview in editor
- [ ] Optimize frame rendering performance

**Files to update:**
- `ImageRepositoryImpl.kt` - Complete frame methods
- Add new `FrameRenderer.kt` utility class

#### **1.3 Text Positioning & Styling** ⚠️
**Status**: Basic text input only  
**Priority**: HIGH - User experience  
**Estimated Time**: 1 week  

**Tasks:**
- [ ] Implement draggable text positioning in Compose
- [ ] Add text alignment controls (left/center/right)
- [ ] Add text shadow/backdrop options
- [ ] Integrate Downloadable Fonts API for handwriting styles
- [ ] Add text size adjustment
- [ ] Add text color picker
- [ ] Text bounds checking (keep within postcard)

**Files to update:**
- `EditorScreen.kt` - Add draggable text component
- `EditorControls.kt` - Add text styling controls
- Add new `DraggableTextComponent.kt`

---

### **Phase 2: Camera & Media Integration** (Priority: High)

#### **2.1 CameraX Integration** ❌
**Status**: Not implemented  
**Priority**: HIGH - Core feature  
**Estimated Time**: 1-2 weeks  

**Tasks:**
- [ ] Add CameraX dependencies to build.gradle.kts
- [ ] Create CameraScreen with CameraX integration
- [ ] Implement photo capture functionality
- [ ] Add camera preview with overlay guides
- [ ] Handle different camera orientations
- [ ] Add flash control
- [ ] Add front/back camera switching
- [ ] Integrate with existing editor flow

**Files to create:**
- `features/camera/view/CameraScreen.kt`
- `features/camera/viewmodel/CameraViewModel.kt`
- `features/camera/domain/CapturePhotoUseCase.kt`

**Files to update:**
- `navigation/Screen.kt` - Add camera route
- `navigation/NostiCardNavigation.kt` - Add camera composable

#### **2.2 Photo Picker Enhancement** ⚠️
**Status**: Basic implementation  
**Priority**: MEDIUM - Better UX  
**Estimated Time**: 3 days  

**Tasks:**
- [ ] Implement Android Photo Picker API (Android 13+)
- [ ] Add fallback for older Android versions
- [ ] Add multi-select capability (for batch processing)
- [ ] Add image crop functionality
- [ ] Add image rotation controls

---

### **Phase 3: Monetization & Billing** (Priority: Business Critical)

#### **3.1 Google Play Billing Implementation** ⚠️
**Status**: Mock implementation only  
**Priority**: CRITICAL - Revenue  
**Estimated Time**: 1-2 weeks  

**Tasks:**
- [ ] Set up Google Play Billing v6 properly
- [ ] Implement actual purchase flow
- [ ] Add purchase verification
- [ ] Handle purchase states (pending, purchased, cancelled)
- [ ] Implement subscription management (if applicable)
- [ ] Add purchase analytics tracking
- [ ] Test with Google Play Console
- [ ] Handle edge cases (network issues, refunds)

**Files to update:**
- `BillingRepositoryImpl.kt` - Replace stub with real implementation
- `SettingsViewModel.kt` - Add proper billing state management
- Add new `BillingVerificationService.kt`

#### **3.2 Export Quality & Watermarking** ⚠️
**Status**: Stub implementation only  
**Priority**: HIGH - Monetization enforcement  
**Estimated Time**: 1 week  

**Tasks:**
- [ ] Implement HD export (1800px for premium, 1080px for free)
- [ ] Add watermark rendering for free users
- [ ] Implement PDF export using PdfDocument API
- [ ] Add export progress indication
- [ ] Handle export errors gracefully
- [ ] Add export quality settings

**Files to update:**
- `ImageRepositoryImpl.kt` - Complete export methods
- `PreviewViewModel.kt` - Add proper export flow

---

### **Phase 4: File Management & Sharing** (Priority: Medium)

#### **4.1 File Providers Setup** ❌
**Status**: Not implemented  
**Priority**: MEDIUM - Sharing functionality  
**Estimated Time**: 2-3 days  

**Tasks:**
- [ ] Configure FileProvider in AndroidManifest.xml
- [ ] Create file_paths.xml resource
- [ ] Update sharing implementation with proper URIs
- [ ] Test sharing across different apps (WhatsApp, Gmail, SMS)
- [ ] Handle sharing permissions properly

**Files to create:**
- `app/src/main/res/xml/file_paths.xml`

**Files to update:**
- `AndroidManifest.xml` - Add FileProvider configuration
- `ShareRepositoryImpl.kt` - Use FileProvider URIs

#### **4.2 Export & Storage Management** ⚠️
**Status**: Basic implementation  
**Priority**: MEDIUM - User experience  
**Estimated Time**: 1 week  

**Tasks:**
- [ ] Add export to gallery functionality
- [ ] Implement export history tracking
- [ ] Add file cleanup for old exports
- [ ] Handle storage permissions properly
- [ ] Add export location selection

---

### **Phase 5: Permissions & Security** (Priority: High)

#### **5.1 Runtime Permissions** ❌
**Status**: Not implemented  
**Priority**: HIGH - App functionality  
**Estimated Time**: 3-4 days  

**Tasks:**
- [ ] Add camera permission handling
- [ ] Add storage permission handling (Android versions)
- [ ] Add notification permissions (Android 13+)
- [ ] Implement permission request flows
- [ ] Handle permission denial gracefully
- [ ] Add permission rationale explanations

**Files to create:**
- `utils/PermissionManager.kt`
- `components/PermissionDialog.kt`

**Files to update:**
- `AndroidManifest.xml` - Add required permissions
- All ViewModels - Add permission checks

#### **5.2 Security & Privacy** ❌
**Status**: Not implemented  
**Priority**: MEDIUM - Compliance  
**Estimated Time**: 2-3 days  

**Tasks:**
- [ ] Implement data encryption for sensitive data
- [ ] Add privacy-compliant analytics (optional)
- [ ] Secure IAP verification
- [ ] Add crash reporting (optional)
- [ ] GDPR compliance measures

---

### **Phase 6: Testing & Quality Assurance** (Priority: Critical)

#### **6.1 Unit Testing** ❌
**Status**: Test framework setup only  
**Priority**: CRITICAL - Code quality  
**Estimated Time**: 1-2 weeks  

**Tasks:**
- [ ] Write ViewModel unit tests
- [ ] Write Repository unit tests  
- [ ] Write UseCase unit tests
- [ ] Add database testing with Room testing utilities
- [ ] Mock external dependencies properly
- [ ] Achieve >80% code coverage

**Files to create:**
- `test/viewmodel/HomeViewModelTest.kt`
- `test/viewmodel/EditorViewModelTest.kt`
- `test/repository/StorageRepositoryTest.kt`
- `test/repository/ImageRepositoryTest.kt`
- `test/repository/BillingRepositoryTest.kt`

#### **6.2 UI Testing** ❌
**Status**: Not implemented  
**Priority**: HIGH - User experience  
**Estimated Time**: 1 week  

**Tasks:**
- [ ] Write Compose UI tests for all screens
- [ ] Add navigation testing
- [ ] Test user interaction flows
- [ ] Add accessibility testing
- [ ] Performance testing on different devices

**Files to create:**
- `androidTest/HomeScreenTest.kt`
- `androidTest/EditorScreenTest.kt`
- `androidTest/NavigationTest.kt`

---

### **Phase 7: Performance & Polish** (Priority: Medium)

#### **7.1 Performance Optimization** ⚠️
**Status**: Basic implementation  
**Priority**: MEDIUM - User experience  
**Estimated Time**: 1 week  

**Tasks:**
- [ ] Image loading optimization
- [ ] Memory usage optimization
- [ ] Battery usage optimization
- [ ] App startup time optimization
- [ ] Compose performance tuning
- [ ] Database query optimization

#### **7.2 Accessibility & Localization** ❌
**Status**: Not implemented  
**Priority**: LOW - Market reach  
**Estimated Time**: 1 week  

**Tasks:**
- [ ] Add content descriptions for all UI elements
- [ ] Test with TalkBack
- [ ] Add haptic feedback
- [ ] Add string resources for localization
- [ ] Support for RTL languages

---

## 📊 **Implementation Progress Tracking**

### **Current Status: MVP Scaffold Complete (60%)**
- ✅ **Architecture & Foundation**: 100% Complete
- ✅ **MVI Implementation**: 100% Complete  
- ✅ **Koin DI Setup**: 100% Complete
- ✅ **Compose UI Screens**: 100% Complete (stubs)
- ✅ **Navigation**: 100% Complete
- ⚠️ **Core Features**: 30% Complete (stubs only)
- ❌ **Image Processing**: 0% Complete
- ❌ **Camera Integration**: 0% Complete
- ❌ **Billing**: 5% Complete (mocks only)
- ❌ **Testing**: 0% Complete

### **Immediate Next Actions (This Week)**
1. **Complete Skia filter implementation** - Unblock core functionality
2. **Set up CameraX integration** - Essential user feature
3. **Implement proper Google Play Billing** - Revenue critical
4. **Add runtime permissions** - App store requirement

### **Success Criteria for MVP Launch**
- [ ] All image filters working properly
- [ ] Camera capture integrated
- [ ] Export & sharing functional  
- [ ] IAP working with real products
- [ ] Basic testing coverage (>70%)
- [ ] Performance targets met (<0.5s filter preview, <3s export)

### **Development Environment Setup**
```bash
# Required tools
- Android Studio Ladybug or later
- JDK 17+
- Android SDK API 35
- Google Play Billing Library v6
- CameraX libraries
```

This roadmap provides a clear path from the current scaffold to a production-ready NostiCard Android app! 🎯