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
        route = "editor?postcardId={postcardId}&imageUri={imageUri}",
        arguments = listOf(
            navArgument("postcardId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("imageUri") {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        fun createRoute(postcardId: String? = null, imageUri: String? = null): String {
            return buildString {
                append("editor")
                val params = mutableListOf<String>()
                postcardId?.let { params.add("postcardId=$it") }
                imageUri?.let { params.add("imageUri=${java.net.URLEncoder.encode(it, "UTF-8")}") }
                if (params.isNotEmpty()) {
                    append("?")
                    append(params.joinToString("&"))
                }
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