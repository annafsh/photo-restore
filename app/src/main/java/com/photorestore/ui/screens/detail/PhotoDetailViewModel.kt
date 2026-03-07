package com.photorestore.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photorestore.data.repository.PhotoRepository
import com.photorestore.domain.model.PhotoItem
import com.photorestore.domain.model.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoDetailUiState(
    val isRestoring: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(private val repository: PhotoRepository) : ViewModel() {
    private val _photo = MutableStateFlow<PhotoItem?>(null)
    val photo: StateFlow<PhotoItem?> = _photo.asStateFlow()
    
    private val _uiState = MutableStateFlow(PhotoDetailUiState())
    val uiState: StateFlow<PhotoDetailUiState> = _uiState.asStateFlow()
    
    private var currentPhotoId: String? = null
    
    fun loadPhoto(photoId: String) {
        currentPhotoId = photoId
        viewModelScope.launch {
            val photos = repository.photos.first()
            _photo.value = photos.find { it.id == photoId }
        }
    }
    
    fun restorePhoto() {
        val p = _photo.value ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true) }
            when (val result = repository.restorePhoto(p)) {
                is RestoreResult.Success -> {
                    loadPhoto(p.id)
                    _uiState.update { it.copy(isRestoring = false) }
                }
                is RestoreResult.Error -> {
                    _uiState.update { it.copy(isRestoring = false, error = result.message) }
                }
                RestoreResult.Loading -> {}
            }
        }
    }
    
    fun deletePhoto() {
        val photoId = currentPhotoId ?: return
        viewModelScope.launch {
            repository.deletePhoto(photoId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
