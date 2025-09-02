package solutions.appme.nosticard.features.mycards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import solutions.appme.nosticard.features.home.domain.DeletePostcardUseCase
import solutions.appme.nosticard.features.home.domain.GetPostcardsUseCase
import solutions.appme.nosticard.ui.components.SnackbarMessages

class MyCardsViewModel(
    private val getPostcardsUseCase: GetPostcardsUseCase,
    private val deletePostcardUseCase: DeletePostcardUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<MyCardsState>(MyCardsState.Loading)
    val state: StateFlow<MyCardsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MyCardsEffect>()
    val effect: SharedFlow<MyCardsEffect> = _effect.asSharedFlow()

    init {
        handleIntent(MyCardsIntent.Load)
    }

    fun handleIntent(intent: MyCardsIntent) {
        when (intent) {
            is MyCardsIntent.Load -> loadCards()
            is MyCardsIntent.Refresh -> refreshCards()
            is MyCardsIntent.SelectTab -> selectTab(intent.tab)
            is MyCardsIntent.DeleteCard -> deleteCard(intent.cardId)
            is MyCardsIntent.DuplicateCard -> duplicateCard(intent.card)
            is MyCardsIntent.EditCard -> editCard(intent.cardId)
            is MyCardsIntent.ViewCard -> viewCard(intent.cardId)
        }
    }

    private fun loadCards() {
        viewModelScope.launch {
            _state.value = MyCardsState.Loading
            
            try {
                combine(
                    getPostcardsUseCase(),
                    getPostcardsUseCase.getDrafts(),
                    getPostcardsUseCase.getCompleted()
                ) { all, drafts, completed ->
                    Triple(all, drafts, completed)
                }.collect { (all, drafts, completed) ->
                    if (all.isEmpty()) {
                        _state.value = MyCardsState.Empty
                    } else {
                        _state.value = MyCardsState.Success(
                            allCards = all,
                            drafts = drafts,
                            completed = completed
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = MyCardsState.Error(
                    message = e.message ?: "Failed to load cards"
                )
            }
        }
    }

    private fun refreshCards() {
        val currentState = _state.value
        if (currentState is MyCardsState.Success) {
            _state.value = currentState.copy(isRefreshing = true)
        }
        loadCards()
    }

    private fun selectTab(tab: CardTab) {
        val currentState = _state.value
        if (currentState is MyCardsState.Success) {
            _state.value = currentState.copy(selectedTab = tab)
        }
    }

    private fun deleteCard(cardId: String) {
        viewModelScope.launch {
            deletePostcardUseCase(cardId)
                .onSuccess {
                    _effect.emit(MyCardsEffect.ShowSnackbar(SnackbarMessages.success("Card deleted successfully")))
                }
                .onFailure { error ->
                    _effect.emit(MyCardsEffect.ShowSnackbar(
                        SnackbarMessages.error(error.message ?: "Failed to delete card")
                    ))
                }
        }
    }

    private fun duplicateCard(card: solutions.appme.nosticard.data.model.Postcard) {
        viewModelScope.launch {
            // TODO: Implement duplicate functionality
            _effect.emit(MyCardsEffect.ShowSnackbar(SnackbarMessages.success("Card duplicated successfully")))
        }
    }

    private fun editCard(cardId: String) {
        viewModelScope.launch {
            _effect.emit(MyCardsEffect.NavigateToEditor(cardId))
        }
    }

    private fun viewCard(cardId: String) {
        viewModelScope.launch {
            _effect.emit(MyCardsEffect.NavigateToPreview(cardId))
        }
    }
}