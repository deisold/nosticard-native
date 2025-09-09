package solutions.appme.nosticard.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PointF
import androidx.core.graphics.createBitmap
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter
import solutions.appme.nosticard.data.model.FilterSettings
import solutions.appme.nosticard.data.model.FilterType
import solutions.appme.nosticard.data.image.ImageFilterProcessor
import kotlin.random.Random

class ImageFilterProcessorImpl(
    private val context: Context
) : ImageFilterProcessor {
    
    override fun applyAllFilters(
        bitmap: Bitmap,
        filterType: FilterType,
        settings: FilterSettings
    ): Bitmap {
        // Convert hardware bitmap to software bitmap for Canvas operations
        val softwareBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
        
        // Apply base filter
        val baseFilteredBitmap = when (filterType) {
            FilterType.CLASSIC_BW -> applyBlackAndWhite(softwareBitmap)
            FilterType.SEPIA_MEMORIES -> applySepia(softwareBitmap)
            FilterType.VINTAGE_WARMTH -> applyVintageWarmth(softwareBitmap)
        }
        
        // Apply all vintage effects on top
        var workingBitmap = baseFilteredBitmap
        
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
        // First apply textures using Canvas operations
        val workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)
        
        val overlayPaint = Paint().apply {
            isAntiAlias = true
            xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.OVERLAY)
        }
        
        // Apply paper fiber texture
        val paperFiberBitmap = createPaperFiberTexture(bitmap.width, bitmap.height)
        canvas.drawBitmap(paperFiberBitmap, 0f, 0f, overlayPaint)
        
        // Apply fingerprint smudges
        val smudgeSeed = System.currentTimeMillis().toInt() % 1000
        val smudgeBitmap = selectRandomSmudge(smudgeSeed, bitmap.width, bitmap.height)
        canvas.drawBitmap(smudgeBitmap, 0f, 0f, overlayPaint)
        
        // Apply stamp marks
        val stampSeed = (System.currentTimeMillis() / 2).toInt() % 1000
        val stampBitmap = selectRandomStamp(stampSeed, bitmap.width, bitmap.height)
        canvas.drawBitmap(stampBitmap, 0f, 0f, overlayPaint)
        
        // Apply grain
        val grainBitmap = createGrainTexture(bitmap.width, bitmap.height)
        canvas.drawBitmap(grainBitmap, 0f, 0f, overlayPaint)
        
        // Now apply GPU filters for sepia and vignette
        val gpuImage = GPUImage(context)
        val filterGroup = GPUImageFilterGroup()
        
        // 1. Desaturate slightly (faded paper color)
        val sepia = GPUImageSepiaToneFilter().apply {
            setIntensity(0.08f) // very subtle
        }
        
        // 2. Vignette (dark edges, light center)
        val vignette = GPUImageVignetteFilter().apply {
            setVignetteCenter(PointF(0.5f, 0.5f))
            setVignetteColor(floatArrayOf(0f, 0f, 0f))
            setVignetteStart(0.3f)
            setVignetteEnd(0.95f)
        }
        
        filterGroup.addFilter(sepia)
        filterGroup.addFilter(vignette)
        
        gpuImage.setFilter(filterGroup)
        return gpuImage.getBitmapWithFilterApplied(workingBitmap)
    }
    
    private fun applyGrainEffect(bitmap: Bitmap, intensity: Float): Bitmap {
        if (intensity <= 0f) return bitmap
        
        val gpuImage = GPUImage(context)
        val sharpenFilter = GPUImageSharpenFilter().apply {
            setSharpness(intensity * 2.0f) // Creates texture enhancement that simulates grain
        }
        
        gpuImage.setFilter(sharpenFilter)
        return gpuImage.getBitmapWithFilterApplied(bitmap)
    }
    
    private fun applyVignetteEffect(bitmap: Bitmap, intensity: Float): Bitmap {
        if (intensity <= 0f) return bitmap
        
        val gpuImage = GPUImage(context)
        val vignetteFilter = GPUImageVignetteFilter().apply {
            setVignetteCenter(PointF(0.5f, 0.5f)) // Center the vignette
            setVignetteStart(0.7f - (intensity * 0.4f)) // Start further in based on intensity
            setVignetteEnd(0.9f - (intensity * 0.1f))   // End closer based on intensity
        }
        
        gpuImage.setFilter(vignetteFilter)
        return gpuImage.getBitmapWithFilterApplied(bitmap)
    }
    
    private fun applyDustEffect(bitmap: Bitmap, intensity: Float, seed: Int): Bitmap {
        if (intensity <= 0f) return bitmap
        
        val gpuImage = GPUImage(context)
        val blurFilter = GPUImageGaussianBlurFilter().apply {
            setBlurSize(intensity * 3.0f) // Increased blur for more visible effect
        }
        
        gpuImage.setFilter(blurFilter)
        return gpuImage.getBitmapWithFilterApplied(bitmap)
    }
    
    // Texture generation functions
    private fun createPaperFiberTexture(width: Int, height: Int): Bitmap {
        val paperBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(paperBitmap)
        
        // Base aged paper color
        canvas.drawColor(Color.argb(120, 245, 235, 215)) // more visible beige base
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        val random = Random(42) // Fixed seed for consistent paper texture
        
        // Add paper fiber patterns
        repeat(300) {
            paint.color = Color.argb(
                random.nextInt(40, 70),
                random.nextInt(200, 240),
                random.nextInt(190, 230),
                random.nextInt(180, 220)
            )
            paint.strokeWidth = random.nextFloat() * 1.5f + 0.5f
            
            val startX = random.nextFloat() * width
            val startY = random.nextFloat() * height
            val endX = startX + random.nextFloat() * 40 - 20
            val endY = startY + random.nextFloat() * 40 - 20
            
            canvas.drawLine(startX, startY, endX, endY, paint)
        }
        
        return paperBitmap
    }
    
    private fun selectRandomSmudge(seed: Int, width: Int, height: Int): Bitmap {
        val smudgeBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(smudgeBitmap)
        val random = Random(seed)
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // Generate 1-2 fingerprint-like smudges
        repeat(random.nextInt(1, 3)) {
            val centerX = random.nextFloat() * width
            val centerY = random.nextFloat() * height
            
            // Create fingerprint-like pattern with concentric ovals
            repeat(random.nextInt(3, 6)) {
                val radiusX = random.nextFloat() * 30 + 20
                val radiusY = radiusX * (0.6f + random.nextFloat() * 0.4f)
                
                paint.color = Color.argb(random.nextInt(40, 70), 80, 60, 40)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = random.nextFloat() * 2f + 1f
                
                val rect = android.graphics.RectF(
                    centerX - radiusX,
                    centerY - radiusY,
                    centerX + radiusX,
                    centerY + radiusY
                )
                canvas.drawOval(rect, paint)
            }
        }
        
        return smudgeBitmap
    }
    
    private fun selectRandomStamp(seed: Int, width: Int, height: Int): Bitmap {
        val stampBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(stampBitmap)
        val random = Random(seed)
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // Generate 1-2 coffee ring or stamp marks
        repeat(random.nextInt(1, 3)) {
            val centerX = random.nextFloat() * width
            val centerY = random.nextFloat() * height
            val radius = random.nextFloat() * 60 + 50
            
            // Coffee ring style - multiple concentric circles
            repeat(random.nextInt(2, 5)) {
                val ringRadius = radius * (0.5f + random.nextFloat() * 0.5f)
                paint.color = Color.argb(
                    random.nextInt(50, 80),
                    random.nextInt(100, 140),
                    random.nextInt(50, 90),
                    random.nextInt(20, 60)
                )
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = random.nextFloat() * 3f + 1f
                
                canvas.drawCircle(centerX, centerY, ringRadius, paint)
            }
            
            // Add some ink blotches inside
            repeat(random.nextInt(2, 4)) {
                val blotchX = centerX + (random.nextFloat() - 0.5f) * radius * 0.8f
                val blotchY = centerY + (random.nextFloat() - 0.5f) * radius * 0.8f
                val blotchRadius = random.nextFloat() * 8 + 3
                
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(random.nextInt(60, 90), 120, 80, 40)
                canvas.drawCircle(blotchX, blotchY, blotchRadius, paint)
            }
        }
        
        return stampBitmap
    }
    
    private fun createGrainTexture(width: Int, height: Int): Bitmap {
        val grainBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grainBitmap)
        
        val paint = Paint().apply {
            isAntiAlias = false
        }
        
        val random = Random(123) // Fixed seed for consistent grain
        
        // Add random dust speckles
        repeat(width * height / 20) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val intensity = random.nextInt(10, 40)
            
            paint.color = if (random.nextBoolean()) {
                Color.argb(intensity, 0, 0, 0) // dark speckles
            } else {
                Color.argb(intensity, 255, 255, 255) // light speckles
            }
            
            canvas.drawPoint(x, y, paint)
        }
        
        return grainBitmap
    }
}