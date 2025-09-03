package solutions.appme.nosticard.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import androidx.core.graphics.createBitmap
import solutions.appme.nosticard.data.model.FilterSettings
import solutions.appme.nosticard.data.model.FilterType
import solutions.appme.nosticard.data.model.FrameType
import solutions.appme.nosticard.utils.ImageCropUtils
import java.io.File
import java.io.FileOutputStream

class ImageRepositoryImpl(
    private val context: Context,
    private val imageCropUtils: ImageCropUtils
) : ImageRepository {
    
    override suspend fun loadImageFromUri(uri: Uri): Result<Bitmap> {
        return try {
            val originalBitmap = loadBitmapFromUri(uri)
            
            // Apply center-crop to postcard ratio (4:3)
            val croppedBitmap = imageCropUtils.cropToPostcardRatio(originalBitmap)
            Result.success(croppedBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun loadAndSaveImage(uri: Uri, postcardId: String): Result<Pair<Bitmap, String>> {
        return try {
            // Load bitmap from URI
            val originalBitmap = loadBitmapFromUri(uri)
            
            // Apply center-crop to postcard ratio (4:3)
            val croppedBitmap = imageCropUtils.cropToPostcardRatio(originalBitmap)
            
            // Save to internal storage
            val fileName = "postcard_${postcardId}_${System.currentTimeMillis()}.jpg"
            val imagesDir = File(context.filesDir, "images")
            imagesDir.mkdirs()
            val imageFile = File(imagesDir, fileName)
            
            FileOutputStream(imageFile).use { outputStream ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            
            Result.success(Pair(croppedBitmap, imageFile.absolutePath))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }
    
    override suspend fun applyFilter(
        bitmap: Bitmap,
        filterType: FilterType,
        settings: FilterSettings
    ): Result<Bitmap> {
        return try {
            // Convert hardware bitmap to software bitmap for Canvas operations
            val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
            
            val filteredBitmap = when (filterType) {
                FilterType.CLASSIC_BW -> applyBlackAndWhite(softwareBitmap)
                FilterType.SEPIA_MEMORIES -> applySepia(softwareBitmap)
                FilterType.VINTAGE_WARMTH -> applyVintageWarmth(softwareBitmap)
            }
            
            // Apply vintage effects
            val finalBitmap = applyVintageEffects(filteredBitmap, settings)
            Result.success(finalBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun applyFrame(bitmap: Bitmap, frameType: FrameType): Result<Bitmap> {
        return try {
            // Convert hardware bitmap to software bitmap for Canvas operations
            val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
            
            val framedBitmap = when (frameType) {
                FrameType.NONE -> softwareBitmap
                FrameType.WHITE_BORDER -> addWhiteBorder(softwareBitmap)
                FrameType.DECKLE_EDGE -> addDeckleEdge(softwareBitmap)
                FrameType.STAMP_EDGE -> addStampEdge(softwareBitmap)
            }
            Result.success(framedBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addTextToImage(
        bitmap: Bitmap,
        text: String,
        x: Float,
        y: Float,
        fontSize: Float,
        fontFamily: String,
        alignment: String
    ): Result<Bitmap> {
        return try {
            // Convert hardware bitmap to software bitmap for Canvas operations
            val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
            
            val mutableBitmap = softwareBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = fontSize
                isAntiAlias = true
            }
            
            canvas.drawText(text, x, y, paint)
            Result.success(mutableBitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportImage(
        bitmap: Bitmap,
        quality: Int,
        addWatermark: Boolean
    ): Result<String> {
        return try {
            val fileName = "postcard_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            
            var finalBitmap = bitmap
            if (addWatermark) {
                finalBitmap = addWatermark(bitmap)
            }
            
            FileOutputStream(file).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportToPdf(bitmap: Bitmap, fileName: String): Result<String> {
        return try {
            // TODO: Implement PDF export using PdfDocument API
            Result.failure(NotImplementedError("PDF export not yet implemented"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun applyBlackAndWhite(bitmap: Bitmap): Bitmap {
        val bwBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bwBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return bwBitmap
    }
    
    private fun applySepia(bitmap: Bitmap): Bitmap {
        val sepiaBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(sepiaBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return sepiaBitmap
    }
    
    private fun applyVintageWarmth(bitmap: Bitmap): Bitmap {
        val warmBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(warmBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply {
            // Apply warm tone filter with slight sepia and increased contrast
            set(floatArrayOf(
                1.2f, 0.1f, 0f, 0f, 10f,
                0f, 1.1f, 0f, 0f, 5f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return warmBitmap
    }
    
    private fun applyVintageEffects(bitmap: Bitmap, settings: FilterSettings): Bitmap {
        // TODO: Implement scratches, dust, grain, and vignette effects
        return bitmap
    }
    
    private fun addWhiteBorder(bitmap: Bitmap): Bitmap {
        val borderSize = 20
        val framedBitmap = createBitmap(
            bitmap.width + borderSize * 2,
            bitmap.height + borderSize * 2,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(framedBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, borderSize.toFloat(), borderSize.toFloat(), null)
        return framedBitmap
    }
    
    private fun addDeckleEdge(bitmap: Bitmap): Bitmap {
        // TODO: Implement deckle edge effect
        return bitmap
    }
    
    private fun addStampEdge(bitmap: Bitmap): Bitmap {
        // TODO: Implement stamp edge effect
        return bitmap
    }
    
    private fun addWatermark(bitmap: Bitmap): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.GRAY
            alpha = 128
            textSize = 32f
            isAntiAlias = true
        }
        
        val watermarkText = "NostiCard Free"
        val x = bitmap.width - paint.measureText(watermarkText) - 20
        val y = bitmap.height - 20f
        
        canvas.drawText(watermarkText, x, y, paint)
        return mutableBitmap
    }
}