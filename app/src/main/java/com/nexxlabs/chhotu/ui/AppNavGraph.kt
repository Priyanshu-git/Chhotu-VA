package com.nexxlabs.chhotu.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavGraph(
    assistantViewModel: AssistantViewModel,
    settingsViewModel: SettingsViewModel,
    onMicClick: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "assistant",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                "assistant",
                exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
                popEnterTransition = { slideInHorizontally { -it / 3 } + fadeIn() }
            ) {
                AssistantScreen(
                    viewModel = assistantViewModel,
                    onMicClick = onMicClick,
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(
                "settings",
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                popExitTransition = { slideOutHorizontally { it } + fadeOut() }
            ) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
