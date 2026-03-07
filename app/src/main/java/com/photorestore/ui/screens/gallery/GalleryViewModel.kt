package com.photorestore.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photorestore.data.repository.PhotoRepository
import com.photorestore.domain.model.PhotoItem
import com.photorestore.domain.model.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GalleryUiState(
    val isLoading: Boolean = false,
    val restoringPhotoId: String? = null
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {
    
    val photos: StateFlow<List<PhotoItem>> = repository.photos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()
    
    fun restorePhoto(photo: PhotoItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(restoringPhotoId = photo.id) }
            val result = repository.restorePhoto(photo)
            _uiState.update { it.copy(restoringPhotoId = null) }
        }
    }
    
    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            repository.deletePhoto(photoId)
        }
    }
}
