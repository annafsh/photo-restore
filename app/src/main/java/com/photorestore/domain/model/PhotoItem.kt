package com.photorestore.domain.model

import android.net.Uri

data class PhotoItem(
    val id: String,
    val originalUri: Uri,
    val restoredUri: Uri? = null,
    val isRestored: Boolean = false,
    val isRestoring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val restoredAt: Long? = null,
    val restorationSettings: RestorationSettings = RestorationSettings()
)

data class RestorationSettings(
    val colorize: Boolean = false,
    val denoise: Boolean = true,
    val enhance: Boolean = true,
    val brightness: Float = 0f,      // -1 to 1
    val contrast: Float = 1f,         // 0.5 to 2
    val saturation: Float = 1f,       // 0 to 2
    val sharpness: Float = 0f         // 0 to 1
)

sealed class RestoreResult {
    data class Success(val restoredUri: Uri) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
    data object Loading : RestoreResult()
}
