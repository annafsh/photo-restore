package com.photorestore.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Scan : Screen("scan", "Scan", Icons.Default.CameraAlt)
    data object Gallery : Screen("gallery", "Gallery", Icons.Default.PhotoLibrary)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object PhotoDetail : Screen("photo/{photoId}", "Photo Detail") {
        fun createRoute(photoId: String) = "photo/$photoId"
    }
}

val bottomNavItems = listOf(Screen.Scan, Screen.Gallery, Screen.Settings)
