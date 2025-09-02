package solutions.appme.nosticard.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import solutions.appme.nosticard.data.model.Postcard

@Database(
    entities = [Postcard::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NostiCardDatabase : RoomDatabase() {
    
    abstract fun postcardDao(): PostcardDao
    
    companion object {
        const val DATABASE_NAME = "nosticard_database"
        
        fun create(context: Context): NostiCardDatabase {
            return Room.databaseBuilder(
                context,
                NostiCardDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}