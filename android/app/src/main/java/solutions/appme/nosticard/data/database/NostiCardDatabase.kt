package solutions.appme.nosticard.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.google.gson.Gson
import solutions.appme.nosticard.data.model.Postcard

@Database(
    entities = [Postcard::class],
    version = 1,
    exportSchema = false
)
abstract class NostiCardDatabase : RoomDatabase() {
    
    abstract fun postcardDao(): PostcardDao
    
    companion object {
        const val DATABASE_NAME = "nosticard_database"
        
        fun create(context: Context, gson: Gson): NostiCardDatabase {
            return Room.databaseBuilder(
                context,
                NostiCardDatabase::class.java,
                DATABASE_NAME
            ).fallbackToDestructiveMigration()
            .addTypeConverter(Converters(gson))
            .build()
        }
    }
}