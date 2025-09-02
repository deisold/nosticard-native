package solutions.appme.nosticard.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import solutions.appme.nosticard.features.home.domain.DeletePostcardUseCase
import solutions.appme.nosticard.features.home.domain.DuplicatePostcardUseCase
import solutions.appme.nosticard.features.home.domain.GetPostcardsUseCase

class HomeViewModel(
    private val getPostcardsUseCase: GetPostcardsUseCase,
    private val deletePostcardUseCase: DeletePostcardUseCase,
    private val duplicatePostcardUseCase: DuplicatePostcardUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        handleIntent(HomeIntent.Load)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Load -> loadPostcards()
            is HomeIntent.Refresh -> refreshPostcards()
            is HomeIntent.DeletePostcard -> deletePostcard(intent.postcardId)
            is HomeIntent.DuplicatePostcard -> duplicatePostcard(intent.postcard)
            is HomeIntent.NavigateToEditor -> navigateToEditor()
            is HomeIntent.NavigateToPostcard -> navigateToPostcard(intent.postcardId)
        }
    }

    private fun loadPostcards() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            
            try {
                combine(
                    getPostcardsUseCase(),
                    getPostcardsUseCase.getDrafts(),
                    getPostcardsUseCase.getCompleted()
                ) { all, drafts, completed ->
                    Triple(all, drafts, completed)
                }.collect { (all, drafts, completed) ->
                    if (all.isEmpty()) {
                        _state.value = HomeState.Empty
                    } else {
                        _state.value = HomeState.Success(
                            postcards = all,
                            drafts = drafts,
                            completed = completed,
                            totalCount = all.size
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = HomeState.Error(
                    message = e.message ?: "Failed to load postcards"
                )
            }
        }
    }

    private fun refreshPostcards() {
        val currentState = _state.value
        if (currentState is HomeState.Success) {
            _state.value = currentState.copy(isRefreshing = true)
        }
        loadPostcards()
    }

    private fun deletePostcard(postcardId: String) {
        viewModelScope.launch {
            deletePostcardUseCase(postcardId)
                .onSuccess {
                    _effect.emit(HomeEffect.ShowSuccess("Postcard deleted successfully"))
                }
                .onFailure { error ->
                    _effect.emit(HomeEffect.ShowError(error.message ?: "Failed to delete postcard"))
                }
        }
    }

    private fun duplicatePostcard(postcard: solutions.appme.nosticard.data.model.Postcard) {
        viewModelScope.launch {
            duplicatePostcardUseCase(postcard)
                .onSuccess { duplicatedPostcard ->
                    _effect.emit(HomeEffect.ShowSuccess("Postcard duplicated successfully"))
                    _effect.emit(HomeEffect.NavigateToPostcard(duplicatedPostcard.id))
                }
                .onFailure { error ->
                    _effect.emit(HomeEffect.ShowError(error.message ?: "Failed to duplicate postcard"))
                }
        }
    }

    private fun navigateToEditor() {
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToEditor)
        }
    }

    private fun navigateToPostcard(postcardId: String) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToPostcard(postcardId))
        }
    }
}