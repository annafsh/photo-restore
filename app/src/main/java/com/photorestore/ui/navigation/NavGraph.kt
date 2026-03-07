package com.photorestore.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.photorestore.ui.screens.detail.PhotoDetailScreen
import com.photorestore.ui.screens.gallery.GalleryScreen
import com.photorestore.ui.screens.scan.ScanScreen
import com.photorestore.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Scan.route, modifier = modifier) {
        composable(Screen.Scan.route) { ScanScreen(onPhotoScanned = { navController.navigate(Screen.Gallery.route) { popUpTo(Screen.Scan.route) { inclusive = true } } }) }
        composable(Screen.Gallery.route) { GalleryScreen(onPhotoClick = { photoId -> navController.navigate(Screen.PhotoDetail.createRoute(photoId)) }) }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(route = Screen.PhotoDetail.route, arguments = listOf(navArgument("photoId") { type = NavType.StringType })) { backStackEntry -> PhotoDetailScreen(photoId = backStackEntry.arguments?.getString("photoId") ?: "", onNavigateBack = { navController.popBackStack() }) }
    }
}
