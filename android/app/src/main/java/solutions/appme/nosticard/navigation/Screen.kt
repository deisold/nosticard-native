package solutions.appme.nosticard.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Home : Screen("home")
    
    object Editor : Screen(
        route = "editor?postcardId={postcardId}",
        arguments = listOf(
            navArgument("postcardId") {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        fun createRoute(postcardId: String? = null): String {
            return if (postcardId != null) {
                "editor?postcardId=$postcardId"
            } else {
                "editor"
            }
        }
    }
    
    object Preview : Screen(
        route = "preview/{postcardId}",
        arguments = listOf(
            navArgument("postcardId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(postcardId: String): String {
            return "preview/$postcardId"
        }
    }
    
    object MyCards : Screen("my_cards")
    
    object Settings : Screen("settings")
}