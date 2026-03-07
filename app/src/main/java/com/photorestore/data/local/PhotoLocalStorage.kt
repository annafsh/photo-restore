package com.photorestore.data.local

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.photorestore.domain.model.PhotoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "photo_restore_prefs")

@Singleton
class PhotoLocalStorage @Inject constructor(@ApplicationContext private val context: Context, private val gson: Gson) {
    private val photosKey = stringPreferencesKey("photos")
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val qualityKey = stringPreferencesKey("export_quality")
    
    val photos: Flow<List<PhotoItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[photosKey] ?: "[]"
        val type = object : TypeToken<List<PhotoItemDto>>() {}.type
        val dtos: List<PhotoItemDto> = gson.fromJson(json, type)
        dtos.map { it.toPhotoItem() }
    }
    
    suspend fun savePhoto(photo: PhotoItem) {
        context.dataStore.edit { prefs ->
            val json = prefs[photosKey] ?: "[]"
            val type = object : TypeToken<MutableList<PhotoItemDto>>() {}.type
            val dtos: MutableList<PhotoItemDto> = gson.fromJson(json, type)
            dtos.add(PhotoItemDto.fromPhotoItem(photo))
            prefs[photosKey] = gson.toJson(dtos)
        }
    }
    
    suspend fun updatePhoto(photo: PhotoItem) {
        context.dataStore.edit { prefs ->
            val json = prefs[photosKey] ?: "[]"
            val type = object : TypeToken<MutableList<PhotoItemDto>>() {}.type
            val dtos: MutableList<PhotoItemDto> = gson.fromJson(json, type)
            val index = dtos.indexOfFirst { it.id == photo.id }
            if (index >= 0) { dtos[index] = PhotoItemDto.fromPhotoItem(photo) }
            prefs[photosKey] = gson.toJson(dtos)
        }
    }
    
    suspend fun deletePhoto(photoId: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[photosKey] ?: "[]"
            val type = object : TypeToken<MutableList<PhotoItemDto>>() {}.type
            val dtos: MutableList<PhotoItemDto> = gson.fromJson(json, type)
            dtos.removeAll { it.id == photoId }
            prefs[photosKey] = gson.toJson(dtos)
        }
    }
    
    val apiKey: Flow<String> = context.dataStore.data.map { prefs -> prefs[apiKeyKey] ?: "" }
    suspend fun saveApiKey(key: String) { context.dataStore.edit { prefs -> prefs[apiKeyKey] = key } }
    val exportQuality: Flow<String> = context.dataStore.data.map { prefs -> prefs[qualityKey] ?: "high" }
    suspend fun saveExportQuality(quality: String) { context.dataStore.edit { prefs -> prefs[qualityKey] = quality } }
}

data class PhotoItemDto(val id: String, val originalUri: String, val restoredUri: String?, val isRestored: Boolean, val isRestoring: Boolean, val createdAt: Long, val restoredAt: Long?) {
    fun toPhotoItem() = PhotoItem(id = id, originalUri = Uri.parse(originalUri), restoredUri = restoredUri?.let { Uri.parse(it) }, isRestored = isRestored, isRestoring = isRestoring, createdAt = createdAt, restoredAt = restoredAt)
    companion object { fun fromPhotoItem(photo: PhotoItem) = PhotoItemDto(id = photo.id, originalUri = photo.originalUri.toString(), restoredUri = photo.restoredUri?.toString(), isRestored = photo.isRestored, isRestoring = photo.isRestoring, createdAt = photo.createdAt, restoredAt = photo.restoredAt) }
}
