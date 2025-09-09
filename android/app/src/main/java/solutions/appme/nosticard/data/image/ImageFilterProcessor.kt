package solutions.appme.nosticard.data.image

import android.graphics.Bitmap
import solutions.appme.nosticard.data.model.FilterSettings
import solutions.appme.nosticard.data.model.FilterType

interface ImageFilterProcessor {

    fun applyAllFilters(
        bitmap: Bitmap,
        filterType: FilterType,
        settings: FilterSettings
    ): Bitmap
}