package com.photorestore.ui.screens.scan

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors

private fun takePhoto(context: Context, imageCapture: ImageCapture, viewModel: ScanViewModel) {
    val photoFile = File(context.cacheDir, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    val executor = Executors.newSingleThreadExecutor()
    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            viewModel.onPhotoSelected(Uri.fromFile(photoFile), context)
        }
        override fun onError(exception: ImageCaptureException) {
            exception.printStackTrace()
        }
    })
}

@Composable
fun ScanScreen(onPhotoScanned: () -> Unit, viewModel: ScanViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted -> hasCameraPermission = isGranted }
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> uri?.let { viewModel.onPhotoSelected(it, context) } }
    
    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.CAMERA) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) { Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show(); onPhotoScanned() } }
    LaunchedEffect(uiState.error) { uiState.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try { cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture) } catch (e: Exception) { e.printStackTrace() }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            })
            Box(modifier = Modifier.fillMaxSize().padding(32.dp).border(2.dp, Color.White.copy(alpha = 0.7f), MaterialTheme.shapes.medium))
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 32.dp), contentAlignment = Alignment.BottomCenter) {
                FloatingActionButton(onClick = { imageCapture?.let { takePhoto(context, it, viewModel) } }, modifier = Modifier.size(72.dp), containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Camera, contentDescription = "Take Photo", modifier = Modifier.size(36.dp), tint = Color.White)
                }
            }
            IconButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required to scan photos", style = MaterialTheme.typography.bodyLarge)
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Grant Permission") }
            }
        }
    }
}
