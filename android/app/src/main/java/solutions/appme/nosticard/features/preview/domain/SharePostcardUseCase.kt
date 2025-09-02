package solutions.appme.nosticard.features.preview.domain

import solutions.appme.nosticard.data.repository.ShareRepository

class SharePostcardUseCase(
    private val shareRepository: ShareRepository
) {
    suspend fun shareImage(imagePath: String, message: String): Result<Unit> {
        return shareRepository.sharePostcard(imagePath, message)
    }
    
    suspend fun sharePdf(pdfPath: String, message: String): Result<Unit> {
        return shareRepository.sharePdf(pdfPath, message)
    }
}