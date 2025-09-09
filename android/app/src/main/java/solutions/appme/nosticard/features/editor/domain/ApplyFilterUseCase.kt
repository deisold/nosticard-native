package solutions.appme.nosticard.features.editor.domain

import android.graphics.Bitmap
import solutions.appme.nosticard.data.image.ImageFilterProcessor
import solutions.appme.nosticard.data.model.FilterSettings
import solutions.appme.nosticard.data.model.FilterType

class ApplyFilterUseCase(
    private val imageFilterProcessor: ImageFilterProcessor
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        filterType: FilterType,
        settings: FilterSettings
    ): Result<Bitmap> {
        return try {
            val filteredBitmap = imageFilterProcessor.applyAllFilters(bitmap, filterType, settings)
            Result.success(filteredBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}