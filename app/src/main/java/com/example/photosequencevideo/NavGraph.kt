package com.example.photosequencevideo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.photosequencevideo.presentation.CreateVideoScreen
import com.example.photosequencevideo.presentation.HomeScreen
import com.example.photosequencevideo.presentation.CreditsScreen
import kotlinx.serialization.Serializable

@Serializable object Home
@Serializable object Credits
@Serializable object CreateVideo

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(
                onNavToCreateVideo = { navController.navigate(CreateVideo) },
                onNavToMyVideos = { navController.navigate(Credits) }
            )
        }
        composable<CreateVideo> {
            CreateVideoScreen()
        }
        composable<Credits> {
            CreditsScreen()
        }
    }
}
