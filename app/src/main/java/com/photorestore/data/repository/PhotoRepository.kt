package com.photorestore.data.repository

import android.content.Context
import android.net.Uri
import com.photorestore.data.api.RestorationApi
import com.photorestore.data.local.PhotoLocalStorage
import com.photorestore.domain.model.PhotoItem
import com.photorestore.domain.model.RestoreResult
import com.photorestore.domain.model.RestorationSettings
import com.photorestore.util.ImageProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: RestorationApi,
    private val localStorage: PhotoLocalStorage
) {
    val photos: Flow<List<PhotoItem>> = localStorage.photos
    val apiKey: Flow<String> = localStorage.apiKey
    val exportQuality: Flow<String> = localStorage.exportQuality
    
    suspend fun saveScannedPhoto(uri: Uri): PhotoItem {
        val photo = PhotoItem(
            id = UUID.randomUUID().toString(),
            originalUri = uri,
            createdAt = System.currentTimeMillis()
        )
        localStorage.savePhoto(photo)
        return photo
    }
    
    suspend fun restorePhoto(photo: PhotoItem, settings: RestorationSettings = RestorationSettings()): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                // Update photo to show it's being restored
                val restoringPhoto = photo.copy(isRestoring = true)
                localStorage.updatePhoto(restoringPhoto)
                
                // Use local image processor for restoration
                val restoredUri = ImageProcessor.processImage(
                    context = context,
                    originalUri = photo.originalUri,
                    brightness = settings.brightness,
                    contrast = settings.contrast,
                    saturation = settings.saturation,
                    sharpen = if (settings.enhance) 0.5f else 0f,
                    denoise = settings.denoise,
                    colorize = settings.colorize
                )
                
                if (restoredUri != null) {
                    val restoredPhoto = photo.copy(
                        restoredUri = restoredUri,
                        isRestored = true,
                        isRestoring = false,
                        restoredAt = System.currentTimeMillis(),
                        restorationSettings = settings
                    )
                    localStorage.updatePhoto(restoredPhoto)
                    RestoreResult.Success(restoredUri)
                } else {
                    val restoredPhoto = photo.copy(isRestoring = false)
                    localStorage.updatePhoto(restoredPhoto)
                    RestoreResult.Error("Failed to process image")
                }
            } catch (e: Exception) {
                val restoredPhoto = photo.copy(isRestoring = false)
                localStorage.updatePhoto(restoredPhoto)
                RestoreResult.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    // Quick restore with default settings
    suspend fun quickRestore(photo: PhotoItem): RestoreResult {
        return restorePhoto(photo, RestorationSettings(
            denoise = true,
            enhance = true,
            brightness = 0.1f,
            contrast = 1.1f,
            saturation = 1.1f
        ))
    }
    
    // Deep restore with all features
    suspend fun deepRestore(photo: PhotoItem): RestoreResult {
        return restorePhoto(photo, RestorationSettings(
            denoise = true,
            enhance = true,
            brightness = 0.15f,
            contrast = 1.15f,
            saturation = 1.2f,
            colorize = true
        ))
    }
    
    suspend fun deletePhoto(photoId: String) {
        localStorage.deletePhoto(photoId)
    }
    
    suspend fun saveApiKey(key: String) {
        localStorage.saveApiKey(key)
    }
    
    suspend fun saveExportQuality(quality: String) {
        localStorage.saveExportQuality(quality)
    }
    
    fun copyImageToAppStorage(uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "scanned_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output -> inputStream.copyTo(output) }
            inputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
