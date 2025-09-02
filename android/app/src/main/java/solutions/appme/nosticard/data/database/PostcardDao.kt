package solutions.appme.nosticard.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import solutions.appme.nosticard.data.model.Postcard

@Dao
interface PostcardDao {
    
    @Query("SELECT * FROM postcards ORDER BY updatedAt DESC")
    fun getAllPostcards(): Flow<List<Postcard>>
    
    @Query("SELECT * FROM postcards WHERE isDraft = 1 ORDER BY updatedAt DESC")
    fun getDrafts(): Flow<List<Postcard>>
    
    @Query("SELECT * FROM postcards WHERE isDraft = 0 ORDER BY updatedAt DESC")
    fun getCompletedPostcards(): Flow<List<Postcard>>
    
    @Query("SELECT * FROM postcards WHERE id = :id")
    suspend fun getPostcardById(id: String): Postcard?
    
    @Query("SELECT COUNT(*) FROM postcards")
    suspend fun getPostcardCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostcard(postcard: Postcard)
    
    @Update
    suspend fun updatePostcard(postcard: Postcard)
    
    @Delete
    suspend fun deletePostcard(postcard: Postcard)
    
    @Query("DELETE FROM postcards WHERE id = :id")
    suspend fun deletePostcardById(id: String)
    
    @Query("SELECT * FROM postcards ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLastPostcard(): Postcard?
}