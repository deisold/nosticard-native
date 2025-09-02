package solutions.appme.nosticard.features.home.viewmodel

import solutions.appme.nosticard.data.model.Postcard
import solutions.appme.nosticard.ui.components.SnackbarMessage

sealed class HomeState {
    object Loading : HomeState()
    
    data class Success(
        val postcards: List<Postcard>,
        val drafts: List<Postcard>,
        val completed: List<Postcard>,
        val totalCount: Int,
        val isRefreshing: Boolean = false
    ) : HomeState()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : HomeState()
    
    object Empty : HomeState()
}

sealed class HomeIntent {
    object Load : HomeIntent()
    object Refresh : HomeIntent()
    data class DeletePostcard(val postcardId: String) : HomeIntent()
    data class DuplicatePostcard(val postcard: Postcard) : HomeIntent()
    object NavigateToEditor : HomeIntent()
    data class NavigateToPostcard(val postcardId: String) : HomeIntent()
}

sealed class HomeEffect {
    data class ShowSnackbar(val message: SnackbarMessage) : HomeEffect()
    object NavigateToEditor : HomeEffect()
    data class NavigateToPostcard(val postcardId: String) : HomeEffect()
}