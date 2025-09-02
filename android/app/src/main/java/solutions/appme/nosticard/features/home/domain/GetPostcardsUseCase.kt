package solutions.appme.nosticard.features.home.domain

import kotlinx.coroutines.flow.Flow
import solutions.appme.nosticard.data.model.Postcard
import solutions.appme.nosticard.data.repository.StorageRepository

class GetPostcardsUseCase(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(): Flow<List<Postcard>> {
        return storageRepository.getAllPostcards()
    }
    
    fun getDrafts(): Flow<List<Postcard>> {
        return storageRepository.getDrafts()
    }
    
    fun getCompleted(): Flow<List<Postcard>> {
        return storageRepository.getCompletedPostcards()
    }
}