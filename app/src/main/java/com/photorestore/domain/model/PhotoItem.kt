package com.photorestore.domain.model

import android.net.Uri

data class PhotoItem(
    val id: String,
    val originalUri: Uri,
    val restoredUri: Uri? = null,
    val isRestored: Boolean = false,
    val isRestoring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val restoredAt: Long? = null
)

data class RestoreOptions(
    val colorize: Boolean = true,
    val denoise: Boolean = true,
    val enhance: Boolean = true,
    val removeScratches: Boolean = true
)

sealed class RestoreResult {
    data class Success(val restoredUri: Uri) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
    data object Loading : RestoreResult()
}
