package solutions.appme.nosticard.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import solutions.appme.nosticard.data.model.FilterType
import solutions.appme.nosticard.data.model.FrameType
import solutions.appme.nosticard.data.model.TextAlignment
import solutions.appme.nosticard.data.model.TextPosition
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromFilterType(value: FilterType): String {
        return value.name
    }
    
    @TypeConverter
    fun toFilterType(value: String): FilterType {
        return FilterType.valueOf(value)
    }
    
    @TypeConverter
    fun fromFrameType(value: FrameType): String {
        return value.name
    }
    
    @TypeConverter
    fun toFrameType(value: String): FrameType {
        return FrameType.valueOf(value)
    }
    
    @TypeConverter
    fun fromTextAlignment(value: TextAlignment): String {
        return value.name
    }
    
    @TypeConverter
    fun toTextAlignment(value: String): TextAlignment {
        return TextAlignment.valueOf(value)
    }
    
    @TypeConverter
    fun fromTextPosition(value: TextPosition): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toTextPosition(value: String): TextPosition {
        return Gson().fromJson(value, object : TypeToken<TextPosition>() {}.type)
    }
}