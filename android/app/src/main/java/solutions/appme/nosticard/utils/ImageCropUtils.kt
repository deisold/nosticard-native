package solutions.appme.nosticard.utils

import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

interface ImageCropUtils {
    fun cropToPostcardRatio(bitmap: Bitmap): Bitmap
}

class ImageCropUtilsImpl : ImageCropUtils {
    
    /**
     * Crops a bitmap to postcard aspect ratio (4:3) using center crop.
     * - Portrait images: crop top/bottom, keep full width
     * - Landscape images: crop left/right if too wide, or scale to fit
     * - Square images: crop to 4:3 ratio
     */
    override fun cropToPostcardRatio(bitmap: Bitmap): Bitmap {
        val originalWidth = bitmap.width.toFloat()
        val originalHeight = bitmap.height.toFloat()
        val originalRatio = originalWidth / originalHeight
        
        val targetRatio = 4f / 3f // Postcard aspect ratio
        
        return if (originalRatio > targetRatio) {
            // Image is wider than target ratio - crop sides
            cropImageWidth(bitmap, targetRatio)
        } else {
            // Image is taller than target ratio - crop top/bottom
            cropImageHeight(bitmap, targetRatio)
        }
    }
    
    private fun cropImageWidth(bitmap: Bitmap, targetRatio: Float): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        // Calculate new width based on target ratio
        val newWidth = (originalHeight * targetRatio).toInt()
        val startX = (originalWidth - newWidth) / 2
        
        return Bitmap.createBitmap(
            bitmap,
            max(0, startX),
            0,
            min(newWidth, originalWidth),
            originalHeight
        )
    }
    
    private fun cropImageHeight(bitmap: Bitmap, targetRatio: Float): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        // Calculate new height based on target ratio
        val newHeight = (originalWidth / targetRatio).toInt()
        val startY = (originalHeight - newHeight) / 2
        
        return Bitmap.createBitmap(
            bitmap,
            0,
            max(0, startY),
            originalWidth,
            min(newHeight, originalHeight)
        )
    }
}