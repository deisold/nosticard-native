package solutions.appme.nosticard.data.repository

import kotlinx.coroutines.flow.Flow
import solutions.appme.nosticard.data.model.Postcard

interface StorageRepository {
    fun getAllPostcards(): Flow<List<Postcard>>
    fun getDrafts(): Flow<List<Postcard>>
    fun getCompletedPostcards(): Flow<List<Postcard>>
    suspend fun getPostcardById(id: String): Postcard?
    suspend fun savePostcard(postcard: Postcard)
    suspend fun deletePostcard(id: String)
    suspend fun duplicatePostcard(postcard: Postcard): Postcard
    suspend fun getPostcardCount(): Int
    suspend fun getLastPostcard(): Postcard?
}