package solutions.appme.nosticard.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect

suspend fun showSnackbar(
    snackbarHostState: SnackbarHostState,
    message: SnackbarMessage,
    onActionClick: ((String) -> Unit)? = null
) {
    val result = snackbarHostState.showSnackbar(
        message = message.text,
        actionLabel = message.actionLabel,
        withDismissAction = message.withDismissAction,
        duration = message.duration
    )
    
    if (result == SnackbarResult.ActionPerformed && message.actionLabel != null) {
        onActionClick?.invoke(message.actionLabel)
    }
}

data class SnackbarMessage(
    val text: String,
    val actionLabel: String? = null,
    val withDismissAction: Boolean = true,
    val duration: androidx.compose.material3.SnackbarDuration = androidx.compose.material3.SnackbarDuration.Short
)

object SnackbarMessages {
    fun success(message: String) = SnackbarMessage(
        text = message,
        duration = androidx.compose.material3.SnackbarDuration.Short
    )
    
    fun error(message: String, actionLabel: String? = "Retry") = SnackbarMessage(
        text = message,
        actionLabel = actionLabel,
        duration = androidx.compose.material3.SnackbarDuration.Long
    )
    
    fun info(message: String) = SnackbarMessage(
        text = message,
        duration = androidx.compose.material3.SnackbarDuration.Short
    )
}