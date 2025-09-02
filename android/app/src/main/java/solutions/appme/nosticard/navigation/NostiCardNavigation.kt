package solutions.appme.nosticard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import solutions.appme.nosticard.features.editor.view.EditorScreen
import solutions.appme.nosticard.features.home.view.HomeScreen
import solutions.appme.nosticard.features.mycards.view.MyCardsScreen
import solutions.appme.nosticard.features.preview.view.PreviewScreen
import solutions.appme.nosticard.features.settings.view.SettingsScreen

@Composable
fun NostiCardNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEditor = {
                    navController.navigate(Screen.Editor.route)
                },
                onNavigateToMyCards = {
                    navController.navigate(Screen.MyCards.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPostcard = { postcardId ->
                    navController.navigate(Screen.Preview.createRoute(postcardId))
                }
            )
        }
        
        composable(
            route = Screen.Editor.route,
            arguments = Screen.Editor.arguments
        ) { backStackEntry ->
            val postcardId = backStackEntry.arguments?.getString("postcardId")
            EditorScreen(
                postcardId = postcardId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPreview = { postcardId ->
                    navController.navigate(Screen.Preview.createRoute(postcardId))
                }
            )
        }
        
        composable(
            route = Screen.Preview.route,
            arguments = Screen.Preview.arguments
        ) { backStackEntry ->
            val postcardId = backStackEntry.arguments?.getString("postcardId") ?: ""
            PreviewScreen(
                postcardId = postcardId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMyCards = {
                    navController.navigate(Screen.MyCards.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        
        composable(Screen.MyCards.route) {
            MyCardsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditor = { postcardId ->
                    navController.navigate(Screen.Editor.createRoute(postcardId))
                },
                onNavigateToPreview = { postcardId ->
                    navController.navigate(Screen.Preview.createRoute(postcardId))
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}