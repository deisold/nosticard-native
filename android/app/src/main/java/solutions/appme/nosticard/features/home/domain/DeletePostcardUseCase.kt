package solutions.appme.nosticard.features.home.domain

import solutions.appme.nosticard.data.repository.StorageRepository

class DeletePostcardUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(postcardId: String): Result<Unit> {
        return try {
            storageRepository.deletePostcard(postcardId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}