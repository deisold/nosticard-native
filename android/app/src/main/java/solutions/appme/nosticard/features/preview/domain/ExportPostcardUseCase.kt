package solutions.appme.nosticard.features.preview.domain

import android.graphics.Bitmap
import solutions.appme.nosticard.data.repository.BillingRepository
import solutions.appme.nosticard.data.repository.ImageRepository

class ExportPostcardUseCase(
    private val imageRepository: ImageRepository,
    private val billingRepository: BillingRepository
) {
    suspend fun exportAsJpeg(bitmap: Bitmap): Result<String> {
        val isPremium = billingRepository.getOwnedProducts().contains("com.nosticard.premium")
        val quality = if (isPremium) 95 else 80
        val addWatermark = !isPremium
        
        return imageRepository.exportImage(
            bitmap = bitmap,
            quality = quality,
            addWatermark = addWatermark
        )
    }
    
    suspend fun exportAsPdf(bitmap: Bitmap, fileName: String): Result<String> {
        val isPremium = billingRepository.getOwnedProducts().contains("com.nosticard.premium")
        
        return if (isPremium) {
            imageRepository.exportToPdf(bitmap, fileName)
        } else {
            Result.failure(Exception("PDF export requires premium subscription"))
        }
    }
}