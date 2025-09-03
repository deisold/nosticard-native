package solutions.appme.nosticard.features.editor.domain

import solutions.appme.nosticard.data.model.Postcard
import solutions.appme.nosticard.data.repository.StorageRepository

class GetPostcardUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(postcardId: String): Result<Postcard?> {
        return try {
            val postcard = storageRepository.getPostcardById(postcardId)
            Result.success(postcard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}