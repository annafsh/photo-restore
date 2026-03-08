package com.photorestore.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt

object ImageProcessor {
    
    suspend fun processImage(
        context: Context,
        originalUri: Uri,
        brightness: Float = 0f,
        contrast: Float = 1f,
        saturation: Float = 1f,
        sharpen: Float = 0f,
        denoise: Boolean = true,
        colorize: Boolean = false
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(originalUri) ?: return@withContext null
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) return@withContext null
            
            // Scale down for faster processing
            val maxSize = 1200
            if (bitmap.width > maxSize || bitmap.height > maxSize) {
                val scale = maxSize.toFloat() / if (bitmap.width > bitmap.height) bitmap.width.toFloat() else bitmap.height.toFloat()
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            }
            
            // Apply denoise
            if (denoise) {
                bitmap = simpleDenoise(bitmap)
            }
            
            // Apply color adjustments
            if (brightness != 0f || contrast != 1f || saturation != 1f) {
                bitmap = adjustColors(bitmap, brightness, contrast, saturation)
            }
            
            // Apply sharpening
            if (sharpen > 0) {
                bitmap = applySharpen(bitmap, sharpen)
            }
            
            // Apply colorize (simple auto-color)
            if (colorize) {
                bitmap = autoColorize(bitmap)
            }
            
            // Save result
            val outputFile = File(context.filesDir, "restored_${UUID.randomUUID()}.jpg")
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            bitmap.recycle()
            
            Uri.fromFile(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun simpleDenoise(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var r = 0
                var g = 0
                var b = 0
                
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val p = bitmap.getPixel(x + dx, y + dy)
                        r += android.graphics.Color.red(p)
                        g += android.graphics.Color.green(p)
                        b += android.graphics.Color.blue(p)
                    }
                }
                
                r = (r / 9).coerceIn(0, 255)
                g = (g / 9).coerceIn(0, 255)
                b = (b / 9).coerceIn(0, 255)
                
                result.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
            }
        }
        
        return result
    }
    
    private fun adjustColors(bitmap: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix()
        
        // Brightness
        val brightOffset = (brightness * 100).roundToInt()
        val brightnessMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightOffset.toFloat(),
            0f, 1f, 0f, 0f, brightOffset.toFloat(),
            0f, 0f, 1f, 0f, brightOffset.toFloat(),
            0f, 0f, 0f, 1f, 0f
        ))
        
        // Contrast
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255
        val contrastMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        
        // Saturation
        val satMatrix = ColorMatrix().apply { setSaturation(saturation) }
        
        colorMatrix.postConcat(brightnessMatrix)
        colorMatrix.postConcat(contrastMatrix)
        colorMatrix.postConcat(satMatrix)
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return result
    }
    
    private fun applySharpen(bitmap: Bitmap, amount: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        val strength = (amount * 2).coerceIn(0f, 2f)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = bitmap.getPixel(x, y)
                val left = bitmap.getPixel(x - 1, y)
                val top = bitmap.getPixel(x, y - 1)
                
                var r = (android.graphics.Color.red(center) * (4 * strength) -
                        android.graphics.Color.red(left) * strength -
                        android.graphics.Color.red(top) * strength).coerceIn(0, 255)
                var g = (android.graphics.Color.green(center) * (4 * strength) -
                        android.graphics.Color.green(left) * strength -
                        android.graphics.Color.green(top) * strength).coerceIn(0, 255)
                var b = (android.graphics.Color.blue(center) * (4 * strength) -
                        android.graphics.Color.blue(left) * strength -
                        android.graphics.Color.blue(top) * strength).coerceIn(0, 255)
                
                result.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
            }
        }
        
        return result
    }
    
    private fun autoColorize(bitmap: Bitmap): Bitmap {
        // Simple warm vintage tint
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        
        val colorMatrix = ColorMatrix(floatArrayOf(
            1.1f, 0f, 0f, 0f, 10f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 0.95f, 0f, -5f,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return result
    }
}
