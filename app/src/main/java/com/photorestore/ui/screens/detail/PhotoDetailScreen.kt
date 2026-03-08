package com.photorestore.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.photorestore.domain.model.RestorationSettings
import com.photorestore.ui.components.BeforeAfterSlider
import com.photorestore.ui.components.RestoreSettingsPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photoId: String,
    onNavigateBack: () -> Unit,
    viewModel: PhotoDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val photo by viewModel.photo.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    
    LaunchedEffect(photoId) { viewModel.loadPhoto(photoId) }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onNavigateBack() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { photo?.let { sharePhoto(context, it.originalUri) } }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(onClick = { viewModel.deletePhoto() }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        }
    ) { padding ->
        photo?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Before/After Comparison
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    BeforeAfterSlider(
                        beforeImage = p.originalUri,
                        afterImage = p.restoredUri ?: p.originalUri,
                        position = sliderPosition,
                        onPositionChange = { sliderPosition = it }
                    )
                }
                
                // Loading indicator
                if (uiState.isRestoring) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // Restore options panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!p.isRestored) {
                        // Quick restore buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.quickRestore() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isRestoring
                            ) {
                                Text("Quick Restore")
                            }
                            OutlinedButton(
                                onClick = { viewModel.deepRestore() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isRestoring
                            ) {
                                Text("Deep Restore")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { showSettings = !showSettings },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showSettings) "Hide Settings" else "Custom Settings")
                        }
                        
                        if (showSettings) {
                            RestoreSettingsPanel(
                                onRestore = { settings -> viewModel.customRestore(settings) }
                            )
                        }
                    } else {
                        // Already restored - show info and option to restore again
                        Text(
                            "Restored! Drag the slider above to compare before/after",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { showSettings = !showSettings },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Restore Again")
                        }
                        
                        if (showSettings) {
                            RestoreSettingsPanel(
                                onRestore = { settings -> viewModel.customRestore(settings) }
                            )
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

private fun sharePhoto(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Photo"))
}
