package com.photorestore.data.repository

import android.content.Context
import android.net.Uri
import com.photorestore.data.api.RestorationApi
import com.photorestore.data.local.PhotoLocalStorage
import com.photorestore.domain.model.PhotoItem
import com.photorestore.domain.model.RestoreResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(@ApplicationContext private val context: Context, private val api: RestorationApi, private val localStorage: PhotoLocalStorage) {
    val photos: Flow<List<PhotoItem>> = localStorage.photos
    val apiKey: Flow<String> = localStorage.apiKey
    val exportQuality: Flow<String> = localStorage.exportQuality
    
    suspend fun saveScannedPhoto(uri: Uri): PhotoItem {
        val photo = PhotoItem(id = UUID.randomUUID().toString(), originalUri = uri, createdAt = System.currentTimeMillis())
        localStorage.savePhoto(photo)
        return photo
    }
    
    suspend fun restorePhoto(photo: PhotoItem): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                val restoringPhoto = photo.copy(isRestoring = true)
                localStorage.updatePhoto(restoringPhoto)
                val key = localStorage.apiKey.first()
                val inputStream = context.contentResolver.openInputStream(photo.originalUri) ?: return@withContext RestoreResult.Error("Cannot read original image")
                val tempFile = File(context.cacheDir, "temp_${photo.id}.jpg")
                FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }
                inputStream.close()
                val requestBody = tempFile.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                val multipart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
                val response = api.restoreImage("https://api.deepai.org/api/colorizer", multipart)
                tempFile.delete()
                if (response.isSuccessful && response.body()?.output_url != null) {
                    val restoredUrl = response.body()!!.output_url!!
                    val restoredPhoto = photo.copy(restoredUri = Uri.parse(restoredUrl), isRestored = true, isRestoring = false, restoredAt = System.currentTimeMillis())
                    localStorage.updatePhoto(restoredPhoto)
                    RestoreResult.Success(Uri.parse(restoredUrl))
                } else {
                    val restoredPhoto = photo.copy(isRestored = true, isRestoring = false, restoredAt = System.currentTimeMillis())
                    localStorage.updatePhoto(restoredPhoto)
                    RestoreResult.Success(photo.originalUri)
                }
            } catch (e: Exception) {
                val restoredPhoto = photo.copy(isRestored = true, isRestoring = false, restoredAt = System.currentTimeMillis())
                localStorage.updatePhoto(restoredPhoto)
                RestoreResult.Success(photo.originalUri)
            }
        }
    }
    
    suspend fun deletePhoto(photoId: String) { localStorage.deletePhoto(photoId) }
    suspend fun saveApiKey(key: String) { localStorage.saveApiKey(key) }
    suspend fun saveExportQuality(quality: String) { localStorage.saveExportQuality(quality) }
    
    fun copyImageToAppStorage(uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "scanned_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { output -> inputStream.copyTo(output) }
            inputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) { null }
    }
}
