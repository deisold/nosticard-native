package solutions.appme.nosticard.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import androidx.core.graphics.createBitmap
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
        val borderSize = 30
        val framedBitmap = createBitmap(
            bitmap.width + borderSize * 2,
            bitmap.height + borderSize * 2,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(framedBitmap)
        canvas.drawColor(Color.WHITE)
        
        // Create rough, torn edge effect
        val paint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = false
            strokeWidth = 3f
        }
        
        // Create jagged edge by drawing irregular border
        val path = android.graphics.Path()
        val random = kotlin.random.Random(42) // Fixed seed for consistent results
        
        // Top edge
        path.moveTo(0f, borderSize.toFloat())
        for (x in 0..bitmap.width + borderSize * 2 step 8) {
            val jitter = random.nextFloat() * 8 - 4
            path.lineTo(x.toFloat(), borderSize + jitter)
        }
        
        // Right edge
        path.lineTo((bitmap.width + borderSize * 2).toFloat(), borderSize.toFloat())
        for (y in borderSize..bitmap.height + borderSize step 8) {
            val jitter = random.nextFloat() * 8 - 4
            path.lineTo(bitmap.width + borderSize * 2 - jitter, y.toFloat())
        }
        
        // Bottom edge
        path.lineTo((bitmap.width + borderSize).toFloat(), (bitmap.height + borderSize * 2).toFloat())
        for (x in bitmap.width + borderSize downTo 0 step 8) {
            val jitter = random.nextFloat() * 8 - 4
            path.lineTo(x.toFloat(), bitmap.height + borderSize * 2 - jitter)
        }
        
        // Left edge
        path.lineTo(0f, (bitmap.height + borderSize).toFloat())
        for (y in bitmap.height + borderSize downTo borderSize step 8) {
            val jitter = random.nextFloat() * 8 - 4
            path.lineTo(jitter, y.toFloat())
        }
        
        path.close()
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, borderSize.toFloat(), borderSize.toFloat(), null)
        
        return framedBitmap
    }
    
    private fun addStampEdge(bitmap: Bitmap): Bitmap {
        val borderSize = 25
        val framedBitmap = createBitmap(
            bitmap.width + borderSize * 2,
            bitmap.height + borderSize * 2,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(framedBitmap)
        canvas.drawColor(Color.WHITE)
        
        // First draw the image with border
        canvas.drawBitmap(bitmap, borderSize.toFloat(), borderSize.toFloat(), null)
        
        // Create stamp-like scalloped border by cutting out scalloped shapes from the white border
        val scallop = 6f
        val spacing = 12f
        val paint = Paint().apply {
            color = Color.TRANSPARENT
            isAntiAlias = true
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
        }
        
        // Top edge scallops
        var x = borderSize.toFloat()
        while (x < bitmap.width + borderSize) {
            canvas.drawCircle(x, borderSize / 2f, scallop, paint)
            x += spacing
        }
        
        // Right edge scallops
        var y = borderSize.toFloat()
        while (y < bitmap.height + borderSize) {
            canvas.drawCircle(bitmap.width + borderSize + borderSize / 2f, y, scallop, paint)
            y += spacing
        }
        
        // Bottom edge scallops
        x = borderSize.toFloat()
        while (x < bitmap.width + borderSize) {
            canvas.drawCircle(x, bitmap.height + borderSize + borderSize / 2f, scallop, paint)
            x += spacing
        }
        
        // Left edge scallops
        y = borderSize.toFloat()
        while (y < bitmap.height + borderSize) {
            canvas.drawCircle(borderSize / 2f, y, scallop, paint)
            y += spacing
        }
        
        return framedBitmap
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