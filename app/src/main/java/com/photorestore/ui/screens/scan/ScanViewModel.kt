package com.photorestore.ui.screens.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photorestore.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    
    fun onPhotoSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val savedUri = repository.copyImageToAppStorage(uri)
                if (savedUri != null) {
                    repository.saveScannedPhoto(savedUri)
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to save photo") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Unknown error") }
            }
        }
    }
    
    fun resetState() {
        _uiState.update { ScanUiState() }
    }
}
