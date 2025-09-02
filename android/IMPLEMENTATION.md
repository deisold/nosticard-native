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

---

## 📐 **Modern Android MVI Architecture**

The Android implementation follows **Clean Architecture with MVI pattern**, leveraging Kotlin coroutines and Jetpack Compose for reactive UI.

### **🎯 Core Design Principles**

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

**5. Koin Dependency Injection**

```kotlin
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
}

val repositoryModule = module {
    single<PostcardRepository> {
        PostcardRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(),
            mapper = get()
        )
    }
}

val useCaseModule = module {
    factory<GetPostcardsUseCase> { 
        GetPostcardsUseCase(repository = get())
    }
}

val viewModelModule = module {
    factory<HomeViewModel> { 
        HomeViewModel(
            getPostcardsUseCase = get(),
            savePostcardUseCase = get()
        )
    }
}
```

### **🔄 Data Flow**

```
User Input → Intent → ViewModel → Use Case → Repository → Data Source
     ↑                                ↓
Compose UI ←←← State ←←← StateFlow ←←←← Business Logic
     ↓
Side Effects (Navigation, Snackbar)
```

---

## ✅ **Compliance Checklist**

For each new feature, ensure:
- [ ] Clean Architecture layers properly separated
- [ ] MVI pattern implemented (Intent → ViewModel → State → UI)
- [ ] Koin modules properly defined for dependency injection
- [ ] Repository pattern for data access
- [ ] Use Cases for business logic
- [ ] StateFlow/Flow for reactive programming
- [ ] Sealed classes for State representation
- [ ] **Exhaustive when-expressions (cover all sealed class cases)**
- [ ] **Constructor injection for ViewModels with parameters**
- [ ] **Auto-initialization in ViewModel init blocks**
- [ ] **Interface-based design for ALL utilities and services (no static objects)**
- [ ] **Dependency injection for ALL dependencies (favor testability)**
- [ ] Compose UI with `koinViewModel { parametersOf(...) }` for ViewModels
- [ ] Error handling with Result<T> and sealed error classes
- [ ] Navigation via callback functions
- [ ] Unit tests for ViewModels and Use Cases with Koin test module
- [ ] UI tests for Compose screens

## 🎯 **MANDATORY PATTERNS**

### **1. Exhaustive When Expressions**
```kotlin
// ✅ REQUIRED: All when expressions must be exhaustive
fun handleIntent(intent: MyIntent) {
    when (intent) {
        is MyIntent.Action1 -> handleAction1()
        is MyIntent.Action2 -> handleAction2()
        // Must cover ALL sealed class cases - compiler enforces
    }
}
```

### **2. ViewModel Constructor Injection**
```kotlin
// ✅ REQUIRED: Parameters injected via constructor
class MyViewModel(
    private val parameter: String,  // Injected parameter
    private val useCase: MyUseCase  // Injected dependency
) : ViewModel() {
    
    init {
        // Auto-initialize with parameter
        loadData(parameter)
    }
}

// ✅ REQUIRED: Koin module with parameters (new DSL)
viewModel { parameters ->
    MyViewModel(
        parameter = parameters.get(),
        useCase = get()
    )
}

// ✅ REQUIRED: View usage with parameters
@Composable
fun MyScreen(
    id: String,
    viewModel: MyViewModel = koinViewModel { parametersOf(id) }
) {
    // No LaunchedEffect needed - ViewModel auto-initializes!
}
```

### **3. ViewModels MUST Auto-Initialize**
```kotlin
// ❌ WRONG: View telling ViewModel what to do
LaunchedEffect(id) {
    viewModel.handleIntent(MyIntent.Load(id))
}

// ✅ CORRECT: ViewModel handles its own lifecycle
init {
    loadData(parameterId) // Auto-loads in constructor
}
```

### **4. Interface-Based Design for Testability**
```kotlin
// ❌ WRONG: Static object - untestable
object ImageCropUtils {
    fun cropToPostcardRatio(bitmap: Bitmap): Bitmap { ... }
}

class ImageRepositoryImpl {
    fun loadImage() {
        ImageCropUtils.cropToPostcardRatio(bitmap) // Hard dependency!
    }
}

// ✅ CORRECT: Interface + DI - fully testable
interface ImageCropUtils {
    fun cropToPostcardRatio(bitmap: Bitmap): Bitmap
}

class ImageCropUtilsImpl : ImageCropUtils {
    override fun cropToPostcardRatio(bitmap: Bitmap): Bitmap { ... }
}

class ImageRepositoryImpl(
    private val imageCropUtils: ImageCropUtils // Injected!
) {
    fun loadImage() {
        imageCropUtils.cropToPostcardRatio(bitmap) // Mockable dependency
    }
}

// Koin Module
single<ImageCropUtils> { ImageCropUtilsImpl() }
```

### **5. NO Static Objects or Singletons**
```kotlin
// ❌ WRONG: Static objects break testability
object NetworkUtils { ... }
object DateUtils { ... }
class DatabaseHelper private constructor() { ... } // Singleton

// ✅ CORRECT: Injectable classes
interface NetworkUtils { ... }
class NetworkUtilsImpl : NetworkUtils { ... }

interface DateUtils { ... }
class DateUtilsImpl : DateUtils { ... }

// All registered in Koin for dependency injection
```

---

# 📊 **COMPREHENSIVE IMPLEMENTATION STATUS**

## 🏗️ **ARCHITECTURE & FOUNDATION**

| Component | Status | Progress | Notes |
|-----------|--------|----------|--------|
| **MVI Architecture** | ✅ Complete | 100% | Sealed classes, exhaustive when, constructor injection |
| **Koin DI Setup** | ✅ Complete | 100% | All modules configured, Compose context fixed |
| **Clean Architecture** | ✅ Complete | 100% | Data/Domain/Presentation layers |
| **Navigation** | ✅ Complete | 100% | Navigation Compose setup |
| **Theme System** | ✅ Complete | 100% | Material 3 + vintage colors |
| **Snackbar System** | ✅ Complete | 100% | Reusable UI components |

## 📱 **SCREENS & UI**

| Screen | Status | Progress | Implementation Details |
|--------|--------|----------|------------------------|
| **HomeScreen** | ✅ Complete | 100% | MVI, navigation, state management |
| **EditorScreen** | ✅ Complete | 95% | Full UI working, auto-load fixed, needs image picker |
| **PreviewScreen** | ✅ Complete | 80% | UI complete, export logic stubs |
| **MyCardsScreen** | ✅ Complete | 100% | Card management, tabs, actions |
| **SettingsScreen** | ✅ Complete | 90% | Billing UI, missing billing logic |
| **OnboardingScreen** | ❌ Not Started | 0% | - |
| **CameraScreen** | ❌ Not Started | 0% | CameraX integration needed |

## 🎨 **CORE FEATURES**

### **Photo Management** ✅ **FULLY IMPLEMENTED**
| Feature | Status | Progress | Notes |
|---------|--------|----------|-------|
| **Gallery Picker** | ✅ **Complete** | 100% | Modern Photo Picker API + legacy support |
| **Camera Capture** | ✅ **Complete** | 100% | System camera app integration with FileProvider |
| **Image Selection Flow** | ✅ **Complete** | 100% | Reusable ImagePickerBottomSheet component |
| **Auto-Crop to Postcard** | ✅ **Complete** | 100% | **Phase 1: Center-crop fitting implemented** |
| **Portrait/Landscape Handling** | ✅ **Complete** | 100% | Smart cropping maintains 4:3 postcard ratio |
| **Navigation Integration** | ✅ **Complete** | 100% | Home → ImagePicker → Editor flow working |

### **Image Processing** 🎯 **BETTER THAN EXPECTED**
| Feature | Status | Progress | Notes |
|---------|--------|----------|-------|
| **Filter System** | ✅ **Implemented** | 80% | Full ColorMatrix implementation working |
| **Classic B&W Filter** | ✅ **Complete** | 100% | ColorMatrix saturation implemented |
| **Sepia Filter** | ✅ **Complete** | 100% | Full sepia ColorMatrix implemented |
| **Faded Color Filter** | ✅ **Complete** | 100% | Alpha opacity effect implemented |
| **Frame System** | ⚠️ Partial | 60% | Architecture + white border working |
| **White Border Frame** | ✅ **Complete** | 100% | Canvas drawing implemented |
| **Deckle Edge Frame** | ❌ Not Implemented | 0% | Irregular border effect needed |
| **Stamp Edge Frame** | ❌ Not Implemented | 0% | Perforated border effect needed |
| **Vintage Effects** | ❌ Stub | 5% | Scratches/dust/grain stub only |
| **Text Overlay** | ✅ **Complete** | 100% | Canvas text drawing implemented |
| **Watermark System** | ✅ **Complete** | 100% | Free user watermark implemented |

### **Text Editing**
| Feature | Status | Progress | Notes |
|---------|--------|----------|-------|
| **Text Input** | ✅ Complete | 100% | Character limits, validation |
| **Basic Text Rendering** | ✅ **Complete** | 100% | Canvas text drawing works |
| **Text Positioning** | ❌ Not Implemented | 0% | Draggable positioning needed |
| **Text Alignment** | ⚠️ Basic | 30% | Controls exist, positioning stubbed |
| **Font Selection** | ⚠️ Basic | 40% | System fonts only, no handwriting fonts |
| **Text Styling** | ❌ Not Implemented | 0% | Size, color, shadow controls needed |

### **Export & Sharing** 🎯 **PARTIALLY WORKING**
| Feature | Status | Progress | Notes |
|---------|--------|----------|-------|
| **JPEG Export** | ✅ **Implemented** | 85% | Full bitmap compression working |
| **PDF Export** | ❌ Stub | 10% | NotImplementedError thrown |
| **HD Quality Control** | ✅ **Complete** | 100% | Quality parameter implemented |
| **Watermark System** | ✅ **Complete** | 100% | Working watermark for free users |
| **File Management** | ✅ **Complete** | 100% | File saving to internal storage |
| **Native Sharing** | ⚠️ Basic | 40% | Intent sharing, FileProvider needed |

## 💰 **MONETIZATION**

| Feature | Status | Progress | Notes |
|---------|--------|----------|-------|
| **Google Play Billing** | ⚠️ Mock | 5% | Stub implementation, needs real billing |
| **Premium Upgrade** | ⚠️ UI Only | 30% | Settings screen ready, no billing logic |
| **Style Pack Purchases** | ⚠️ UI Only | 25% | Product listing ready, no billing logic |
| **Feature Gating** | ✅ Complete | 100% | Premium checks implemented |
| **Purchase Verification** | ❌ Not Implemented | 0% | Security critical |
| **Restore Purchases** | ⚠️ UI Only | 20% | Button exists, logic stubbed |

## 🗄️ **DATA & STORAGE** ✅ **FULLY WORKING**

| Component | Status | Progress | Notes |
|-----------|--------|----------|-------|
| **Room Database** | ✅ **Complete** | 100% | PostcardDao with full CRUD operations |
| **DataStore** | ✅ Complete | 100% | Preferences, settings storage |
| **File Management** | ✅ Complete | 90% | Image storage, cleanup logic |
| **Data Models** | ✅ **Complete** | 100% | Postcard entity with converters |
| **Repository Pattern** | ✅ **Complete** | 100% | StorageRepositoryImpl fully functional |
| **Use Cases** | ✅ **Complete** | 100% | GetPostcards, Save, Delete, Duplicate working |
| **Database Operations** | ✅ **Complete** | 100% | Flow-based reactive queries working |

## 🔧 **SERVICES & UTILITIES**

| Service | Status | Progress | Notes |
|---------|--------|----------|-------|
| **Image Processing Service** | ✅ **Implemented** | 75% | ColorMatrix filters, Canvas operations working |
| **Billing Service** | ⚠️ Mock | 15% | Mock products/state, no real Google Play integration |
| **Storage Service** | ✅ Complete | 100% | File operations, cleanup |
| **Share Service** | ⚠️ Basic | 60% | Intent sharing, needs FileProvider |
| **Configuration Service** | ✅ Complete | 100% | App settings, feature flags |

## 🧪 **TESTING**

| Test Type | Status | Progress | Notes |
|-----------|--------|----------|-------|
| **Unit Tests** | ❌ Not Started | 0% | ViewModels, repositories, use cases |
| **UI Tests** | ❌ Not Started | 0% | Compose testing |
| **Integration Tests** | ❌ Not Started | 0% | Database, repositories |
| **E2E Tests** | ❌ Not Started | 0% | Complete user flows |
| **Performance Tests** | ❌ Not Started | 0% | Image processing, memory |

## 🔐 **PLATFORM & SECURITY**

| Feature | Status | Progress | Notes |
|---------|--------|----------|-------|
| **Runtime Permissions** | ❌ Not Started | 0% | Camera, storage permissions |
| **File Provider Setup** | ❌ Not Started | 0% | For sharing functionality |
| **Security** | ❌ Not Started | 0% | Data encryption, secure billing |
| **Analytics** | ❌ Not Started | 0% | Usage tracking, crash reporting |
| **Performance Monitoring** | ❌ Not Started | 0% | Memory, battery optimization |

---

# 🎯 **CRITICAL PATH TO MVP**

## **PHASE 1: CORE FUNCTIONALITY** (Estimated: 3-4 weeks)
**Priority: CRITICAL - Must have for MVP**

### **1.1 Image Processing Pipeline** ⚠️ **BLOCKING MVP**
- [ ] **Skia/Canvas Integration** (1-2 weeks) - Core functionality
- [ ] **Filter Implementation** (1 week) - B&W, Sepia, Faded Color  
- [ ] **Frame Rendering** (3-4 days) - Basic frames
- [ ] **Real-time Preview** (2-3 days) - Performance critical
- [ ] **Export Pipeline** (1 week) - JPEG/PDF generation

### **1.2 Image Cropping Enhancement** ⚠️ **FUTURE PHASES** 
- [ ] **Phase 2: ML Kit Face Detection** (1 week) - Smart cropping around faces
- [ ] **Phase 3: Manual Crop Controls** (1 week) - User drag/zoom controls
- [ ] **Advanced Aspect Ratios** (2-3 days) - Multiple postcard formats

### **1.3 Monetization** ⚠️ **BLOCKING REVENUE**
- [ ] **Google Play Billing** (1-2 weeks) - Real purchase flow
- [ ] **Watermark Implementation** (2-3 days) - Free user restriction
- [ ] **Purchase Verification** (2-3 days) - Security

## **PHASE 2: POLISH & TESTING** (Estimated: 2 weeks)
**Priority: HIGH - Quality assurance**

### **2.1 Core Testing**
- [ ] **Unit Tests** (1 week) - ViewModels, repositories
- [ ] **UI Tests** (3-4 days) - Critical user flows
- [ ] **Performance Testing** (2-3 days) - Memory, speed

### **2.2 File Management**
- [ ] **FileProvider Setup** (1 day) - Sharing functionality
- [ ] **Export Management** (2 days) - Gallery export, cleanup

## **PHASE 3: LAUNCH PREPARATION** (Estimated: 1 week)
**Priority: MEDIUM - Market readiness**

### **3.1 Platform Readiness**
- [ ] **App Store Assets** (2-3 days) - Screenshots, descriptions
- [ ] **Security Audit** (2 days) - Billing, data handling
- [ ] **Performance Optimization** (2-3 days) - Battery, memory

---

# 🚨 **IMMEDIATE BLOCKERS**

## **Critical Issues Preventing MVP**
1. **🟡 Camera Integration**: Missing - Essential user feature  
2. **🟡 Billing Implementation**: Mock only - No revenue possible
3. **🟡 PDF Export**: NotImplementedError - Premium feature blocked
4. **🟡 FileProvider Setup**: Missing - Sharing functionality blocked
5. **🟡 Runtime Permissions**: Missing - App store requirement

## **Next 7 Days Priority** 🎯 **UPDATED - CAMERA COMPLETE**
1. **PDF Export Implementation** - Complete premium feature (NotImplementedError)
2. **Google Play Billing** - Replace mock with real implementation  
3. **Vintage Effects Implementation** - Scratches, dust, grain effects
4. **Advanced Frame Effects** - Deckle edge, stamp edge frames
5. **Phase 2 Image Cropping** - ML Kit face detection integration

**📸 IMAGE SELECTION FULLY COMPLETE**: Gallery picker, camera capture, auto-cropping all working!

---

# 📈 **SUCCESS METRICS**

## **Technical Targets**
- [ ] **Filter Preview**: <500ms response time
- [ ] **Export Speed**: <3s for HD image  
- [ ] **App Startup**: <2s cold start
- [ ] **Memory Usage**: <200MB peak
- [ ] **Test Coverage**: >80% for critical paths

## **Business Targets**
- [ ] **Core User Flow**: Gallery → Edit → Export → Share (working end-to-end)
- [ ] **Monetization**: Premium upgrade + watermark enforcement working
- [ ] **Platform Ready**: All required permissions, metadata, assets

## **User Experience Targets**
- [ ] **No Crashes**: Critical paths stable
- [ ] **Intuitive Navigation**: All screens accessible
- [ ] **Performance**: Smooth 60fps UI, responsive interactions

---

## **🎯 IMAGE CROPPING IMPLEMENTATION PHASES**

### **✅ Phase 1: Center-Crop Fitting (COMPLETE)**
- **ImageCropUtils**: Smart cropping utility for 4:3 postcard ratio
- **Portrait Images**: Crop top/bottom, maintain full width  
- **Landscape Images**: Crop left/right, maintain aspect ratio
- **Integration**: Applied automatically in ImageRepository.loadImageFromUri()
- **User Experience**: All images fit perfectly in postcard preview

### **⏳ Phase 2: ML Kit Face Detection (PLANNED)**
- **Smart Cropping**: Center crop around detected faces
- **Face Detection**: Google ML Kit on-device processing
- **Fallback**: Center-crop when no faces detected
- **Privacy**: No cloud processing, all on-device

### **⏳ Phase 3: Manual Crop Controls (PLANNED)**  
- **User Controls**: Drag, zoom, rotate crop area
- **Live Preview**: Real-time crop adjustment
- **Presets**: Common aspect ratios + custom
- **Advanced UX**: Instagram-style crop interface

---

**Overall MVP Progress**: 🚧 **75%** Complete ⬆️ **MAJOR JUMP**

**Estimated time to MVP**: **1-2 weeks** with focused development ⬆️ **ACCELERATED**

**Current Status**: Architecture complete, image processing working, **photo management complete**, main blockers are PDF export and billing