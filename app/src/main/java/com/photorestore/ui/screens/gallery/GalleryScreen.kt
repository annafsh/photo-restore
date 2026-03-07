package com.photorestore.ui.screens.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.photorestore.domain.model.PhotoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(onPhotoClick: (String) -> Unit, viewModel: GalleryViewModel = hiltViewModel()) {
    val photos by viewModel.photos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(topBar = { TopAppBar(title = { Text("Gallery") }) }) { padding ->
        if (photos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No photos yet. Scan your first photo!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(photos, key = { it.id }) { photo ->
                    PhotoGridItem(
                        photo = photo,
                        isRestoring = uiState.restoringPhotoId == photo.id,
                        onClick = { onPhotoClick(photo.id) },
                        onRestore = { viewModel.restorePhoto(photo) },
                        onDelete = { viewModel.deletePhoto(photo.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoGridItem(photo: PhotoItem, isRestoring: Boolean, onClick: () -> Unit, onRestore: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Box {
            AsyncImage(model = photo.restoredUri ?: photo.originalUri, contentDescription = "Photo", modifier = Modifier.aspectRatio(1f).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Crop)
            if (isRestoring) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            Column(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) {
                if (!photo.isRestored) {
                    IconButton(onClick = onRestore, modifier = Modifier.padding(0.dp)) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.padding(0.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
