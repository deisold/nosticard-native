package solutions.appme.nosticard.features.editor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import solutions.appme.nosticard.data.model.*
import solutions.appme.nosticard.data.repository.ImageRepository
import solutions.appme.nosticard.features.editor.domain.ApplyFilterUseCase
import solutions.appme.nosticard.features.editor.domain.GetPostcardUseCase
import solutions.appme.nosticard.features.editor.domain.SavePostcardUseCase
import solutions.appme.nosticard.ui.components.SnackbarMessages
import android.net.Uri
import java.util.*

class EditorViewModel(
    private val postcardId: String?,
    private val getPostcardUseCase: GetPostcardUseCase,
    private val imageRepository: ImageRepository,
    private val applyFilterUseCase: ApplyFilterUseCase,
    private val savePostcardUseCase: SavePostcardUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<EditorState>(EditorState.Loading)
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EditorEffect>()
    val effect: SharedFlow<EditorEffect> = _effect.asSharedFlow()
    
    init {
        handleIntent(EditorIntent.LoadPostcard(postcardId = postcardId))
    }

    fun handleIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadPostcard -> loadPostcard(postcardId)
            is EditorIntent.LoadImage -> loadImage(intent.imagePath)
            is EditorIntent.ApplyFilter -> applyFilter(intent.filterType)
            is EditorIntent.ApplyFrame -> applyFrame(intent.frameType)
            is EditorIntent.UpdateText -> updateText(intent.text)
            is EditorIntent.UpdateTextPosition -> updateTextPosition(intent.position)
            is EditorIntent.UpdateTextAlignment -> updateTextAlignment(intent.alignment)
            is EditorIntent.UpdateFilterSettings -> updateFilterSettings(intent.settings)
            is EditorIntent.GeneratePreview -> generatePreview()
            is EditorIntent.SaveDraft -> saveDraft()
            is EditorIntent.SaveCompleted -> saveCompleted()
            is EditorIntent.NavigateToPreview -> navigateToPreview()
            is EditorIntent.SelectImage -> selectImage()
        }
    }

    private fun loadPostcard(postcardId: String?) {
        viewModelScope.launch {
            try {
                if (postcardId == null) {
                    // Create new postcard
                    createNewPostcard()
                } else {
                    // Load existing postcard
                    try {
                        val result = getPostcardUseCase(postcardId)
                        result.onSuccess { postcard ->
                            if (postcard != null) {
                                loadExistingPostcard(postcard)
                            } else {
                                _state.value = EditorState.Error(
                                    message = "Postcard with ID '$postcardId' not found in database"
                                )
                            }
                        }.onFailure { error ->
                            _state.value = EditorState.Error(
                                message = "Database query failed: ${error.message ?: "Unknown database error"}"
                            )
                        }
                    } catch (e: Exception) {
                        _state.value = EditorState.Error(
                            message = "GetPostcardUseCase failed: ${e.message ?: "Unknown error"}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = EditorState.Error(
                    message = e.message ?: "Failed to load postcard"
                )
            }
        }
    }

    private fun createNewPostcard() {
        val newPostcard = Postcard(
            id = UUID.randomUUID().toString(),
            title = "New Postcard",
            imagePath = "",
            text = "",
            filterType = FilterType.CLASSIC_BW,
            frameType = FrameType.NONE,
            textPosition = TextPosition(0.5f, 0.8f),
            textAlignment = TextAlignment.LEFT,
            filterSettings = FilterSettings(),
            isDraft = true,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        _state.value = EditorState.Ready(postcard = newPostcard)
    }
    
    private fun loadExistingPostcard(postcard: Postcard) {
        val initialState = EditorState.Ready(
            postcard = postcard,
            textInput = postcard.text,
            selectedFilter = postcard.filterType,
            selectedFrame = postcard.frameType,
            textAlignment = postcard.textAlignment,
            textPosition = postcard.textPosition,
            filterSettings = postcard.filterSettings
        )
        
        _state.value = initialState
        
        // If postcard has an image, load it
        if (postcard.imagePath.isNotBlank()) {
            loadImage(postcard.imagePath)
        }
    }

    private fun loadImage(imagePath: String) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(isProcessing = true)
            
            viewModelScope.launch {
                try {
                    // Check if this is a file path (existing draft) or URI (new image)
                    if (imagePath.startsWith("content://") || imagePath.startsWith("file://")) {
                        // This is a URI from image picker - load and save to persistent storage
                        val uri = Uri.parse(imagePath)
                        imageRepository.loadAndSaveImage(uri, currentState.postcard.id)
                            .onSuccess { (bitmap, persistentPath) ->
                                val updatedPostcard = currentState.postcard.copy(
                                    imagePath = persistentPath,
                                    updatedAt = Date()
                                )
                                
                                // Save the updated postcard with persistent path immediately
                                savePostcardUseCase(updatedPostcard)
                                
                                val updatedState = currentState.copy(
                                    postcard = updatedPostcard,
                                    originalBitmap = bitmap,
                                    previewBitmap = bitmap,
                                    isProcessing = false
                                )
                                _state.value = updatedState
                                
                                // Always generate preview with filters applied for new images
                                viewModelScope.launch {
                                    _state.value = updatedState.copy(isProcessing = true)
                                    generateCompositePreview(updatedState.copy(isProcessing = true))
                                }
                            }
                            .onFailure { error ->
                                _state.value = currentState.copy(isProcessing = false)
                                _effect.emit(EditorEffect.ShowSnackbar(
                                    SnackbarMessages.error("Failed to load image: ${error.message}")
                                ))
                            }
                    } else {
                        // This is a file path from existing draft - load directly
                        val file = java.io.File(imagePath)
                        if (file.exists()) {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(imagePath)
                            if (bitmap != null) {
                                val updatedState = currentState.copy(
                                    originalBitmap = bitmap,
                                    previewBitmap = bitmap,
                                    isProcessing = false
                                )
                                _state.value = updatedState
                                
                                // Always regenerate preview with filters applied
                                viewModelScope.launch {
                                    _state.value = updatedState.copy(isProcessing = true)
                                    generateCompositePreview(updatedState.copy(isProcessing = true))
                                }
                            } else {
                                _state.value = currentState.copy(isProcessing = false)
                                _effect.emit(EditorEffect.ShowSnackbar(
                                    SnackbarMessages.error("Failed to decode image file")
                                ))
                            }
                        } else {
                            _state.value = currentState.copy(isProcessing = false)
                            _effect.emit(EditorEffect.ShowSnackbar(
                                SnackbarMessages.error("Image file not found: $imagePath")
                            ))
                        }
                    }
                } catch (e: Exception) {
                    _state.value = currentState.copy(isProcessing = false)
                    _effect.emit(EditorEffect.ShowSnackbar(
                        SnackbarMessages.error("Error loading image: ${e.message}")
                    ))
                }
            }
        }
    }

    private fun applyFilter(filterType: FilterType) {
        val currentState = _state.value
        if (currentState is EditorState.Ready && currentState.originalBitmap != null) {
            _state.value = currentState.copy(isProcessing = true)
            
            viewModelScope.launch {
                val updatedState = currentState.copy(
                    postcard = currentState.postcard.copy(
                        filterType = filterType,
                        updatedAt = Date()
                    ),
                    selectedFilter = filterType
                )
                
                // Generate composite preview with all effects
                generateCompositePreview(updatedState)
            }
        }
    }

    private fun applyFrame(frameType: FrameType) {
        val currentState = _state.value
        if (currentState is EditorState.Ready && currentState.originalBitmap != null) {
            _state.value = currentState.copy(isProcessing = true)
            
            viewModelScope.launch {
                val updatedState = currentState.copy(
                    postcard = currentState.postcard.copy(
                        frameType = frameType,
                        updatedAt = Date()
                    ),
                    selectedFrame = frameType
                )
                
                // Generate composite preview with all effects
                generateCompositePreview(updatedState)
            }
        }
    }

    private fun updateText(text: String) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            val updatedState = currentState.copy(
                postcard = currentState.postcard.copy(
                    text = text,
                    updatedAt = Date()
                ),
                textInput = text
            )
            _state.value = updatedState
            
            // Regenerate preview if we have an image
            if (currentState.originalBitmap != null) {
                viewModelScope.launch {
                    _state.value = updatedState.copy(isProcessing = true)
                    generateCompositePreview(updatedState)
                }
            }
        }
    }

    private fun updateTextPosition(position: TextPosition) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            val updatedState = currentState.copy(
                postcard = currentState.postcard.copy(
                    textPosition = position,
                    updatedAt = Date()
                ),
                textPosition = position
            )
            _state.value = updatedState
            
            // Regenerate preview if we have an image and text
            if (currentState.originalBitmap != null && currentState.textInput.isNotBlank()) {
                viewModelScope.launch {
                    _state.value = updatedState.copy(isProcessing = true)
                    generateCompositePreview(updatedState)
                }
            }
        }
    }

    private fun updateTextAlignment(alignment: TextAlignment) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            val updatedState = currentState.copy(
                postcard = currentState.postcard.copy(
                    textAlignment = alignment,
                    updatedAt = Date()
                ),
                textAlignment = alignment
            )
            _state.value = updatedState
            
            // Regenerate preview if we have an image and text
            if (currentState.originalBitmap != null && currentState.textInput.isNotBlank()) {
                viewModelScope.launch {
                    _state.value = updatedState.copy(isProcessing = true)
                    generateCompositePreview(updatedState)
                }
            }
        }
    }

    private fun updateFilterSettings(settings: FilterSettings) {
        val currentState = _state.value
        if (currentState is EditorState.Ready && currentState.originalBitmap != null) {
            val updatedState = currentState.copy(
                postcard = currentState.postcard.copy(
                    filterSettings = settings,
                    updatedAt = Date()
                ),
                filterSettings = settings,
                isProcessing = true
            )
            _state.value = updatedState
            
            viewModelScope.launch {
                // Re-apply all effects with new settings
                generateCompositePreview(updatedState)
            }
        }
    }

    private fun generatePreview() {
        val currentState = _state.value
        if (currentState is EditorState.Ready && currentState.originalBitmap != null) {
            _state.value = currentState.copy(isProcessing = true)
            
            viewModelScope.launch {
                generateCompositePreview(currentState)
            }
        }
    }
    
    /**
     * Generates a composite preview with all effects applied:
     * 1. Apply filter to original bitmap
     * 2. Apply frame to filtered bitmap  
     * 3. Add text overlay to final bitmap
     */
    private suspend fun generateCompositePreview(state: EditorState.Ready) {
        try {
            var workingBitmap = state.originalBitmap!!
            
            // Step 1: Apply filter (always applied since we removed NONE option)
            val filterResult = applyFilterUseCase(
                bitmap = workingBitmap,
                filterType = state.selectedFilter,
                settings = state.filterSettings
            )
            
            if (filterResult.isSuccess) {
                workingBitmap = filterResult.getOrThrow()
            } else {
                _state.value = state.copy(isProcessing = false)
                _effect.emit(EditorEffect.ShowSnackbar(
                    SnackbarMessages.error("Filter failed: ${filterResult.exceptionOrNull()?.message}")
                ))
                return
            }
            
            // Step 2: Apply frame
            if (state.selectedFrame != FrameType.NONE) {
                val frameResult = imageRepository.applyFrame(workingBitmap, state.selectedFrame)
                
                if (frameResult.isSuccess) {
                    workingBitmap = frameResult.getOrThrow()
                } else {
                    _state.value = state.copy(isProcessing = false)
                    _effect.emit(EditorEffect.ShowSnackbar(
                        SnackbarMessages.error("Frame failed: ${frameResult.exceptionOrNull()?.message}")
                    ))
                    return
                }
            }
            
            // Step 3: Add text overlay (if text exists)
            if (state.textInput.isNotBlank()) {
                val textX = state.textPosition.x * workingBitmap.width
                val textY = state.textPosition.y * workingBitmap.height
                
                val textResult = imageRepository.addTextToImage(
                    bitmap = workingBitmap,
                    text = state.textInput,
                    x = textX,
                    y = textY,
                    fontSize = 32f,
                    fontFamily = "sans-serif",
                    alignment = state.textAlignment.name.lowercase()
                )
                
                if (textResult.isSuccess) {
                    workingBitmap = textResult.getOrThrow()
                } else {
                    _state.value = state.copy(isProcessing = false)
                    _effect.emit(EditorEffect.ShowSnackbar(
                        SnackbarMessages.error("Text failed: ${textResult.exceptionOrNull()?.message}")
                    ))
                    return
                }
            }
            
            // Update state with final composite bitmap
            _state.value = state.copy(
                previewBitmap = workingBitmap,
                isProcessing = false
            )
            
        } catch (e: Exception) {
            _state.value = state.copy(isProcessing = false)
            _effect.emit(EditorEffect.ShowSnackbar(
                SnackbarMessages.error("Preview error: ${e.message}")
            ))
        }
    }

    private fun saveDraft() {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(isSaving = true)
            
            viewModelScope.launch {
                val draftPostcard = currentState.postcard.copy(
                    isDraft = true,
                    updatedAt = Date()
                )
                
                savePostcardUseCase(draftPostcard)
                    .onSuccess {
                        _state.value = currentState.copy(isSaving = false)
                        // Navigate back to home after successful save
                        _effect.emit(EditorEffect.NavigateBack)
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isSaving = false)
                        _effect.emit(EditorEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Failed to save draft")
                        ))
                    }
            }
        }
    }

    private fun saveCompleted() {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(isSaving = true)
            
            viewModelScope.launch {
                val completedPostcard = currentState.postcard.copy(
                    isDraft = false,
                    updatedAt = Date()
                )
                
                savePostcardUseCase(completedPostcard)
                    .onSuccess {
                        _state.value = currentState.copy(isSaving = false)
                        _effect.emit(EditorEffect.ShowSnackbar(SnackbarMessages.success("Postcard saved")))
                        _effect.emit(EditorEffect.NavigateToPreview)
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isSaving = false)
                        _effect.emit(EditorEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Failed to save postcard")
                        ))
                    }
            }
        }
    }

    private fun navigateToPreview() {
        viewModelScope.launch {
            _effect.emit(EditorEffect.NavigateToPreview)
        }
    }
    
    private fun selectImage() {
        viewModelScope.launch {
            _effect.emit(EditorEffect.LaunchPhotoPicker)
        }
    }
}