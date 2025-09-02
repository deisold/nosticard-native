package solutions.appme.nosticard.features.preview.viewmodel

import android.graphics.Bitmap
import solutions.appme.nosticard.data.model.Postcard
import solutions.appme.nosticard.ui.components.SnackbarMessage

sealed class PreviewState {
    object Loading : PreviewState()
    
    data class Ready(
        val postcard: Postcard,
        val previewBitmap: Bitmap? = null,
        val exportedImagePath: String? = null,
        val exportedPdfPath: String? = null,
        val isExporting: Boolean = false,
        val isSharing: Boolean = false,
        val isPremiumUser: Boolean = false
    ) : PreviewState()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : PreviewState()
}

sealed class PreviewIntent {
    object ExportAsJpeg : PreviewIntent()
    object ExportAsPdf : PreviewIntent()
    data class ShareImage(val message: String = "Check out my postcard!") : PreviewIntent()
    data class SharePdf(val message: String = "Check out my postcard!") : PreviewIntent()
    object NavigateBack : PreviewIntent()
    object NavigateToMyCards : PreviewIntent()
}

sealed class PreviewEffect {
    data class ShowSnackbar(val message: SnackbarMessage) : PreviewEffect()
    object NavigateBack : PreviewEffect()
    object NavigateToMyCards : PreviewEffect()
    data class ShowUpgradeDialog(val feature: String) : PreviewEffect()
}