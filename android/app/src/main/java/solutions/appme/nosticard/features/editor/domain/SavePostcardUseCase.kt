package solutions.appme.nosticard.features.editor.domain

import solutions.appme.nosticard.data.model.Postcard
import solutions.appme.nosticard.data.repository.StorageRepository

class SavePostcardUseCase(
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(postcard: Postcard): Result<Unit> {
        return try {
            storageRepository.savePostcard(postcard)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}