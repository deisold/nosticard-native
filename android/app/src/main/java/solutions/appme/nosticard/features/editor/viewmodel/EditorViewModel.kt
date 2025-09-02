package solutions.appme.nosticard.features.editor.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import solutions.appme.nosticard.data.model.*
import solutions.appme.nosticard.data.repository.ImageRepository
import solutions.appme.nosticard.features.editor.domain.ApplyFilterUseCase
import solutions.appme.nosticard.features.editor.domain.SavePostcardUseCase
import solutions.appme.nosticard.ui.components.SnackbarMessages
import android.net.Uri
import java.util.*

class EditorViewModel(
    private val postcardId: String?,
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
                    // TODO: Implement loading existing postcard
                    createNewPostcard()
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
            filterType = FilterType.NONE,
            frameType = FrameType.NONE,
            textPosition = TextPosition(0.5f, 0.8f),
            textAlignment = TextAlignment.LEFT,
            isDraft = true,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        _state.value = EditorState.Ready(postcard = newPostcard)
    }

    private fun loadImage(imagePath: String) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            val updatedPostcard = currentState.postcard.copy(
                imagePath = imagePath,
                updatedAt = Date()
            )
            _state.value = currentState.copy(
                postcard = updatedPostcard,
                isProcessing = true
            )
            
            viewModelScope.launch {
                try {
                    val uri = Uri.parse(imagePath)
                    imageRepository.loadImageFromUri(uri)
                        .onSuccess { bitmap ->
                            _state.value = currentState.copy(
                                postcard = updatedPostcard,
                                originalBitmap = bitmap,
                                previewBitmap = bitmap,
                                isProcessing = false
                            )
                            _effect.emit(EditorEffect.ShowSnackbar(
                                SnackbarMessages.success("Image loaded successfully")
                            ))
                        }
                        .onFailure { error ->
                            _state.value = currentState.copy(isProcessing = false)
                            _effect.emit(EditorEffect.ShowSnackbar(
                                SnackbarMessages.error("Failed to load image: ${error.message}")
                            ))
                        }
                } catch (e: Exception) {
                    _state.value = currentState.copy(isProcessing = false)
                    _effect.emit(EditorEffect.ShowSnackbar(
                        SnackbarMessages.error("Invalid image URI")
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
                applyFilterUseCase(
                    bitmap = currentState.originalBitmap,
                    filterType = filterType,
                    settings = currentState.filterSettings
                ).onSuccess { filteredBitmap ->
                    _state.value = currentState.copy(
                        postcard = currentState.postcard.copy(
                            filterType = filterType,
                            updatedAt = Date()
                        ),
                        previewBitmap = filteredBitmap,
                        selectedFilter = filterType,
                        isProcessing = false
                    )
                }.onFailure { error ->
                    _state.value = currentState.copy(isProcessing = false)
                    _effect.emit(EditorEffect.ShowSnackbar(
                        SnackbarMessages.error(error.message ?: "Failed to apply filter")
                    ))
                }
            }
        }
    }

    private fun applyFrame(frameType: FrameType) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(
                postcard = currentState.postcard.copy(
                    frameType = frameType,
                    updatedAt = Date()
                ),
                selectedFrame = frameType
            )
        }
    }

    private fun updateText(text: String) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(
                postcard = currentState.postcard.copy(
                    text = text,
                    updatedAt = Date()
                ),
                textInput = text
            )
        }
    }

    private fun updateTextPosition(position: TextPosition) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(
                postcard = currentState.postcard.copy(
                    textPosition = position,
                    updatedAt = Date()
                ),
                textPosition = position
            )
        }
    }

    private fun updateTextAlignment(alignment: TextAlignment) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(
                postcard = currentState.postcard.copy(
                    textAlignment = alignment,
                    updatedAt = Date()
                ),
                textAlignment = alignment
            )
        }
    }

    private fun updateFilterSettings(settings: FilterSettings) {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(
                filterSettings = settings
            )
            // Re-apply current filter with new settings
            applyFilter(currentState.selectedFilter)
        }
    }

    private fun generatePreview() {
        val currentState = _state.value
        if (currentState is EditorState.Ready) {
            _state.value = currentState.copy(isProcessing = true)
            
            viewModelScope.launch {
                // TODO: Generate final preview with all effects
                _state.value = currentState.copy(isProcessing = false)
                _effect.emit(EditorEffect.ShowSnackbar(SnackbarMessages.success("Preview generated")))
            }
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
                        _effect.emit(EditorEffect.ShowSnackbar(SnackbarMessages.success("Draft saved")))
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