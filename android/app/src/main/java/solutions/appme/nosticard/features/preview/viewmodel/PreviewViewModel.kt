package solutions.appme.nosticard.features.preview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import solutions.appme.nosticard.features.preview.domain.ExportPostcardUseCase
import solutions.appme.nosticard.features.preview.domain.SharePostcardUseCase
import solutions.appme.nosticard.ui.components.SnackbarMessages

class PreviewViewModel(
    private val postcardId: String,
    private val exportPostcardUseCase: ExportPostcardUseCase,
    private val sharePostcardUseCase: SharePostcardUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PreviewState>(PreviewState.Loading)
    val state: StateFlow<PreviewState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PreviewEffect>()
    val effect: SharedFlow<PreviewEffect> = _effect.asSharedFlow()

    init {
        loadPostcard(postcardId)
    }

    fun handleIntent(intent: PreviewIntent) {
        when (intent) {
            is PreviewIntent.ExportAsJpeg -> exportAsJpeg()
            is PreviewIntent.ExportAsPdf -> exportAsPdf()
            is PreviewIntent.ShareImage -> shareImage(intent.message)
            is PreviewIntent.SharePdf -> sharePdf(intent.message)
            is PreviewIntent.NavigateBack -> navigateBack()
            is PreviewIntent.NavigateToMyCards -> navigateToMyCards()
        }
    }

    private fun loadPostcard(postcardId: String) {
        viewModelScope.launch {
            try {
                // TODO: Load postcard from repository
                _state.value = PreviewState.Error("Postcard loading not implemented")
            } catch (e: Exception) {
                _state.value = PreviewState.Error(
                    message = e.message ?: "Failed to load postcard"
                )
            }
        }
    }

    private fun exportAsJpeg() {
        val currentState = _state.value
        if (currentState is PreviewState.Ready && currentState.previewBitmap != null) {
            _state.value = currentState.copy(isExporting = true)
            
            viewModelScope.launch {
                exportPostcardUseCase.exportAsJpeg(currentState.previewBitmap)
                    .onSuccess { exportPath ->
                        _state.value = currentState.copy(
                            isExporting = false,
                            exportedImagePath = exportPath
                        )
                        _effect.emit(PreviewEffect.ShowSnackbar(SnackbarMessages.success("Image exported successfully")))
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isExporting = false)
                        _effect.emit(PreviewEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Failed to export image")
                        ))
                    }
            }
        }
    }

    private fun exportAsPdf() {
        val currentState = _state.value
        if (currentState is PreviewState.Ready && currentState.previewBitmap != null) {
            if (!currentState.isPremiumUser) {
                viewModelScope.launch {
                    _effect.emit(PreviewEffect.ShowUpgradeDialog("PDF Export"))
                }
                return
            }
            
            _state.value = currentState.copy(isExporting = true)
            
            viewModelScope.launch {
                exportPostcardUseCase.exportAsPdf(
                    bitmap = currentState.previewBitmap,
                    fileName = "postcard_${currentState.postcard.id}.pdf"
                )
                    .onSuccess { pdfPath ->
                        _state.value = currentState.copy(
                            isExporting = false,
                            exportedPdfPath = pdfPath
                        )
                        _effect.emit(PreviewEffect.ShowSnackbar(SnackbarMessages.success("PDF exported successfully")))
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isExporting = false)
                        _effect.emit(PreviewEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Failed to export PDF")
                        ))
                    }
            }
        }
    }

    private fun shareImage(message: String) {
        val currentState = _state.value
        if (currentState is PreviewState.Ready) {
            val imagePath = currentState.exportedImagePath 
                ?: run {
                    viewModelScope.launch {
                        _effect.emit(PreviewEffect.ShowSnackbar(SnackbarMessages.error("Please export image first")))
                    }
                    return
                }
            
            _state.value = currentState.copy(isSharing = true)
            
            viewModelScope.launch {
                sharePostcardUseCase.shareImage(imagePath, message)
                    .onSuccess {
                        _state.value = currentState.copy(isSharing = false)
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isSharing = false)
                        _effect.emit(PreviewEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Failed to share image")
                        ))
                    }
            }
        }
    }

    private fun sharePdf(message: String) {
        val currentState = _state.value
        if (currentState is PreviewState.Ready) {
            val pdfPath = currentState.exportedPdfPath 
                ?: run {
                    viewModelScope.launch {
                        _effect.emit(PreviewEffect.ShowSnackbar(SnackbarMessages.error("Please export PDF first")))
                    }
                    return
                }
            
            _state.value = currentState.copy(isSharing = true)
            
            viewModelScope.launch {
                sharePostcardUseCase.sharePdf(pdfPath, message)
                    .onSuccess {
                        _state.value = currentState.copy(isSharing = false)
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isSharing = false)
                        _effect.emit(PreviewEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Failed to share PDF")
                        ))
                    }
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(PreviewEffect.NavigateBack)
        }
    }

    private fun navigateToMyCards() {
        viewModelScope.launch {
            _effect.emit(PreviewEffect.NavigateToMyCards)
        }
    }
}