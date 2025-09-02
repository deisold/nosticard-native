package solutions.appme.nosticard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "postcards")
data class Postcard(
    @PrimaryKey val id: String,
    val title: String,
    val imagePath: String,
    val text: String,
    val filterType: FilterType,
    val frameType: FrameType,
    val textPosition: TextPosition,
    val textAlignment: TextAlignment,
    val isDraft: Boolean,
    val createdAt: Date,
    val updatedAt: Date,
    val exportPath: String? = null
)

data class TextPosition(
    val x: Float,
    val y: Float
)

enum class FilterType {
    NONE,
    CLASSIC_BW,
    SEPIA_MEMORIES,
    FADED_COLOR
}

enum class FrameType {
    NONE,
    WHITE_BORDER,
    DECKLE_EDGE,
    STAMP_EDGE
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

data class FilterSettings(
    val scratchSeed: Int = 0,
    val grainLevel: Float = 0.5f,
    val vignetteLevel: Float = 0.3f,
    val dustLevel: Float = 0.2f
)