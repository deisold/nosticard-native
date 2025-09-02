package solutions.appme.nosticard.data.repository

import kotlinx.coroutines.flow.Flow
import solutions.appme.nosticard.data.database.PostcardDao
import solutions.appme.nosticard.data.model.Postcard
import java.util.*

class StorageRepositoryImpl(
    private val postcardDao: PostcardDao
) : StorageRepository {
    
    override fun getAllPostcards(): Flow<List<Postcard>> {
        return postcardDao.getAllPostcards()
    }
    
    override fun getDrafts(): Flow<List<Postcard>> {
        return postcardDao.getDrafts()
    }
    
    override fun getCompletedPostcards(): Flow<List<Postcard>> {
        return postcardDao.getCompletedPostcards()
    }
    
    override suspend fun getPostcardById(id: String): Postcard? {
        return postcardDao.getPostcardById(id)
    }
    
    override suspend fun savePostcard(postcard: Postcard) {
        val updatedPostcard = postcard.copy(updatedAt = Date())
        postcardDao.insertPostcard(updatedPostcard)
    }
    
    override suspend fun deletePostcard(id: String) {
        postcardDao.deletePostcardById(id)
    }
    
    override suspend fun duplicatePostcard(postcard: Postcard): Postcard {
        val newPostcard = postcard.copy(
            id = UUID.randomUUID().toString(),
            title = "Copy of ${postcard.title}",
            isDraft = true,
            createdAt = Date(),
            updatedAt = Date(),
            exportPath = null
        )
        postcardDao.insertPostcard(newPostcard)
        return newPostcard
    }
    
    override suspend fun getPostcardCount(): Int {
        return postcardDao.getPostcardCount()
    }
    
    override suspend fun getLastPostcard(): Postcard? {
        return postcardDao.getLastPostcard()
    }
}