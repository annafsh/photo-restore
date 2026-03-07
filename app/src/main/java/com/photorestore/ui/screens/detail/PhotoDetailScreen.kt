package com.photorestore.ui.screens.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(photoId: String, onNavigateBack: () -> Unit, viewModel: PhotoDetailViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val photo by viewModel.photo.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(photoId) { viewModel.loadPhoto(photoId) }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onNavigateBack() }
    
    Scaffold(topBar = { TopAppBar(title = { Text("Photo Detail") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, actions = { IconButton(onClick = { photo?.let { sharePhoto(context, it.originalUri) } }) { Icon(Icons.Default.Share, "Share") }; IconButton(onClick = { viewModel.deletePhoto() }) { Icon(Icons.Default.Delete, "Delete") } }) }) { padding ->
        photo?.let { p ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) { Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Before") }); Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("After") }) }
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(model = if (selectedTab == 0) p.originalUri else (p.restoredUri ?: p.originalUri), contentDescription = if (selectedTab == 0) "Original" else "Restored", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    if (uiState.isRestoring) { CircularProgressIndicator() }
                }
                if (!p.isRestored) {
                    Button(onClick = { viewModel.restorePhoto() }, modifier = Modifier.fillMaxWidth().padding(16.dp), enabled = !uiState.isRestoring) { Text(if (uiState.isRestoring) "Restoring..." else "Restore Photo") }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
}

private fun sharePhoto(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply { type = "image/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    context.startActivity(Intent.createChooser(intent, "Share Photo"))
}
