package solutions.appme.nosticard.features.editor.viewmodel

import android.graphics.Bitmap
import solutions.appme.nosticard.data.model.*
import solutions.appme.nosticard.ui.components.SnackbarMessage

sealed class EditorState {
    object Loading : EditorState()
    
    data class Ready(
        val postcard: Postcard,
        val originalBitmap: Bitmap? = null,
        val previewBitmap: Bitmap? = null,
        val isProcessing: Boolean = false,
        val isSaving: Boolean = false,
        val filterSettings: FilterSettings = FilterSettings(),
        val textInput: String = "",
        val selectedFilter: FilterType = FilterType.NONE,
        val selectedFrame: FrameType = FrameType.NONE,
        val textAlignment: TextAlignment = TextAlignment.LEFT,
        val textPosition: TextPosition = TextPosition(0.5f, 0.8f)
    ) : EditorState()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : EditorState()
}

sealed class EditorIntent {
    data class LoadPostcard(val postcardId: String?) : EditorIntent()
    data class LoadImage(val imagePath: String) : EditorIntent()
    data class ApplyFilter(val filterType: FilterType) : EditorIntent()
    data class ApplyFrame(val frameType: FrameType) : EditorIntent()
    data class UpdateText(val text: String) : EditorIntent()
    data class UpdateTextPosition(val position: TextPosition) : EditorIntent()
    data class UpdateTextAlignment(val alignment: TextAlignment) : EditorIntent()
    data class UpdateFilterSettings(val settings: FilterSettings) : EditorIntent()
    object GeneratePreview : EditorIntent()
    object SaveDraft : EditorIntent()
    object SaveCompleted : EditorIntent()
    object NavigateToPreview : EditorIntent()
}

sealed class EditorEffect {
    data class ShowSnackbar(val message: SnackbarMessage) : EditorEffect()
    object NavigateToPreview : EditorEffect()
    object NavigateBack : EditorEffect()
    data class RequestPermission(val permission: String) : EditorEffect()
}