package solutions.appme.nosticard.data.repository

import android.graphics.Bitmap
import android.net.Uri
import solutions.appme.nosticard.data.model.FilterSettings
import solutions.appme.nosticard.data.model.FilterType
import solutions.appme.nosticard.data.model.FrameType

interface ImageRepository {
    suspend fun loadImageFromUri(uri: Uri): Result<Bitmap>
    suspend fun applyFilter(
        bitmap: Bitmap,
        filterType: FilterType,
        settings: FilterSettings
    ): Result<Bitmap>
    
    suspend fun applyFrame(
        bitmap: Bitmap,
        frameType: FrameType
    ): Result<Bitmap>
    
    suspend fun addTextToImage(
        bitmap: Bitmap,
        text: String,
        x: Float,
        y: Float,
        fontSize: Float,
        fontFamily: String,
        alignment: String
    ): Result<Bitmap>
    
    suspend fun exportImage(
        bitmap: Bitmap,
        quality: Int,
        addWatermark: Boolean
    ): Result<String>
    
    suspend fun exportToPdf(
        bitmap: Bitmap,
        fileName: String
    ): Result<String>
}