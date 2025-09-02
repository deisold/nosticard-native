package solutions.appme.nosticard.features.home.domain

import solutions.appme.nosticard.data.model.Postcard
import solutions.appme.nosticard.data.repository.StorageRepository

class DuplicatePostcardUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(postcard: Postcard): Result<Postcard> {
        return try {
            val duplicatedPostcard = storageRepository.duplicatePostcard(postcard)
            Result.success(duplicatedPostcard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}