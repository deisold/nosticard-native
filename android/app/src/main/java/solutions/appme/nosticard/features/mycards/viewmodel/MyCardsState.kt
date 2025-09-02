package solutions.appme.nosticard.features.mycards.viewmodel

import solutions.appme.nosticard.data.model.Postcard
import solutions.appme.nosticard.ui.components.SnackbarMessage

sealed class MyCardsState {
    object Loading : MyCardsState()
    
    data class Success(
        val allCards: List<Postcard>,
        val drafts: List<Postcard>,
        val completed: List<Postcard>,
        val selectedTab: CardTab = CardTab.ALL,
        val isRefreshing: Boolean = false
    ) : MyCardsState()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : MyCardsState()
    
    object Empty : MyCardsState()
}

enum class CardTab {
    ALL, DRAFTS, COMPLETED
}

sealed class MyCardsIntent {
    object Load : MyCardsIntent()
    object Refresh : MyCardsIntent()
    data class SelectTab(val tab: CardTab) : MyCardsIntent()
    data class DeleteCard(val cardId: String) : MyCardsIntent()
    data class DuplicateCard(val card: Postcard) : MyCardsIntent()
    data class EditCard(val cardId: String) : MyCardsIntent()
    data class ViewCard(val cardId: String) : MyCardsIntent()
}

sealed class MyCardsEffect {
    data class ShowSnackbar(val message: SnackbarMessage) : MyCardsEffect()
    data class NavigateToEditor(val cardId: String) : MyCardsEffect()
    data class NavigateToPreview(val cardId: String) : MyCardsEffect()
    data class ShowDeleteConfirmation(val cardId: String, val cardTitle: String) : MyCardsEffect()
}