package com.photorestore.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.photorestore.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: PhotoRepository) : ViewModel() {
    val apiKey: StateFlow<String> = repository.apiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val exportQuality: StateFlow<String> = repository.exportQuality.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "high")
    
    fun saveApiKey(key: String) {
        viewModelScope.launch { repository.saveApiKey(key) }
    }
    
    fun saveExportQuality(quality: String) {
        viewModelScope.launch { repository.saveExportQuality(quality) }
    }
}
