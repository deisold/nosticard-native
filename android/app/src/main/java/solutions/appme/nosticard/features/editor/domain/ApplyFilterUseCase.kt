package solutions.appme.nosticard.features.editor.domain

import android.graphics.Bitmap
import solutions.appme.nosticard.data.model.FilterSettings
import solutions.appme.nosticard.data.model.FilterType
import solutions.appme.nosticard.data.repository.ImageRepository

class ApplyFilterUseCase(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        filterType: FilterType,
        settings: FilterSettings
    ): Result<Bitmap> {
        return imageRepository.applyFilter(bitmap, filterType, settings)
    }
}