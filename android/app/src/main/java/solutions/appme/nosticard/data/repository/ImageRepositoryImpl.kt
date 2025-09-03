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
        var workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        
        // Apply effects in order: grain -> vignette -> dust/scratches
        if (settings.grainLevel > 0f) {
            workingBitmap = applyGrainEffect(workingBitmap, settings.grainLevel)
        }
        
        if (settings.vignetteLevel > 0f) {
            workingBitmap = applyVignetteEffect(workingBitmap, settings.vignetteLevel)
        }
        
        if (settings.dustLevel > 0f) {
            workingBitmap = applyDustEffect(workingBitmap, settings.dustLevel, settings.scratchSeed)
        }
        
        return workingBitmap
    }
    
    private fun applyGrainEffect(bitmap: Bitmap, intensity: Float): Bitmap {
        if (intensity <= 0f) return bitmap
        
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val random = kotlin.random.Random(42) // Fixed seed for consistent grain
        
        // Apply VISIBLE noise to every pixel
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)
            
            // Make noise much stronger and visible
            val noiseStrength = intensity * 80f // Much stronger noise
            val noise = (random.nextFloat() - 0.5f) * noiseStrength
            
            val newRed = (red + noise).coerceIn(0f, 255f).toInt()
            val newGreen = (green + noise).coerceIn(0f, 255f).toInt()
            val newBlue = (blue + noise).coerceIn(0f, 255f).toInt()
            
            pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
        }
        
        val grainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        grainBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return grainBitmap
    }
    
    private fun applyVignetteEffect(bitmap: Bitmap, intensity: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val vignetteBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(vignetteBitmap)
        
        // Create radial gradient for vignette
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = kotlin.math.min(width, height) / 2f
        
        val radialGradient = android.graphics.RadialGradient(
            centerX, centerY, radius,
            Color.TRANSPARENT,
            Color.argb((intensity * 128).toInt(), 0, 0, 0),
            android.graphics.Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = radialGradient
            isAntiAlias = true
        }
        
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return vignetteBitmap
    }
    
    private fun applyDustEffect(bitmap: Bitmap, intensity: Float, seed: Int): Bitmap {
        if (intensity <= 0f) return bitmap
        
        val dustBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(dustBitmap)
        val random = kotlin.random.Random(seed)
        
        // Realistic dust spots with random blur
        val numDustSpots = (intensity * 40).toInt() // Reasonable amount
        
        for (i in 0 until numDustSpots) {
            val x = random.nextFloat() * bitmap.width
            val y = random.nextFloat() * bitmap.height
            val radius = random.nextFloat() * 8f + 2f // Smaller, more realistic spots
            
            // Random blur for each dust spot
            val blurRadius = random.nextFloat() * 6f + 1f
            val blurFilter = android.graphics.BlurMaskFilter(blurRadius, android.graphics.BlurMaskFilter.Blur.NORMAL)
            
            // Subtle dust colors
            val dustColor = when (random.nextInt(4)) {
                0 -> Color.argb((intensity * 100).toInt(), 80, 60, 50) // Dark dust
                1 -> Color.argb((intensity * 80).toInt(), 180, 160, 140) // Light dust  
                2 -> Color.argb((intensity * 90).toInt(), 120, 100, 80) // Medium dust
                else -> Color.argb((intensity * 70).toInt(), 200, 190, 180) // Very light dust
            }
            
            val paint = Paint().apply {
                isAntiAlias = true
                color = dustColor
                maskFilter = blurFilter
            }
            
            canvas.drawCircle(x, y, radius, paint)
        }
        
        // Realistic scratches with random blur
        val numScratches = (intensity * 8).toInt() // Fewer, more realistic
        
        for (i in 0 until numScratches) {
            val startX = random.nextFloat() * bitmap.width
            val startY = random.nextFloat() * bitmap.height
            val endX = startX + (random.nextFloat() - 0.5f) * 100f // Shorter scratches
            val endY = startY + (random.nextFloat() - 0.5f) * 30f
            
            // Random blur for each scratch
            val scratchBlur = random.nextFloat() * 3f + 1f
            val scratchFilter = android.graphics.BlurMaskFilter(scratchBlur, android.graphics.BlurMaskFilter.Blur.NORMAL)
            
            val scratchPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = random.nextFloat() * 2f + 1f // Thinner scratches
                strokeCap = Paint.Cap.ROUND
                color = Color.argb((intensity * 90).toInt(), 100, 80, 60) // Subtle scratch color
                maskFilter = scratchFilter
            }
            
            canvas.drawLine(startX, startY, endX, endY, scratchPaint)
        }
        
        return dustBitmap
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