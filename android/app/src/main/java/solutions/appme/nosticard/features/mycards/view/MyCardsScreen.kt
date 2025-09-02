package solutions.appme.nosticard.features.mycards.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import solutions.appme.nosticard.features.home.view.components.PostcardItem
import solutions.appme.nosticard.features.mycards.viewmodel.*
import solutions.appme.nosticard.ui.components.showSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCardsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToPreview: (String) -> Unit,
    viewModel: MyCardsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MyCardsEffect.NavigateToEditor -> onNavigateToEditor(effect.cardId)
                is MyCardsEffect.NavigateToPreview -> onNavigateToPreview(effect.cardId)
                is MyCardsEffect.ShowSnackbar -> {
                    showSnackbar(snackbarHostState, effect.message)
                }
                is MyCardsEffect.ShowDeleteConfirmation -> {
                    // TODO: Show delete confirmation dialog
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cards") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        MyCardsContent(
            state = state,
            onIntent = viewModel::handleIntent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun MyCardsContent(
    state: MyCardsState,
    onIntent: (MyCardsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is MyCardsState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is MyCardsState.Empty -> {
            EmptyStateContent(modifier = modifier)
        }
        
        is MyCardsState.Success -> {
            SuccessContent(
                state = state,
                onIntent = onIntent,
                modifier = modifier
            )
        }
        
        is MyCardsState.Error -> {
            ErrorContent(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = { onIntent(MyCardsIntent.Load) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun SuccessContent(
    state: MyCardsState.Success,
    onIntent: (MyCardsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Tab selector
        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            CardTab.values().forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onIntent(MyCardsIntent.SelectTab(tab)) },
                    text = {
                        Text(
                            text = when (tab) {
                                CardTab.ALL -> "All (${state.allCards.size})"
                                CardTab.DRAFTS -> "Drafts (${state.drafts.size})"
                                CardTab.COMPLETED -> "Completed (${state.completed.size})"
                            }
                        )
                    }
                )
            }
        }
        
        // Card list
        val cardsToShow = when (state.selectedTab) {
            CardTab.ALL -> state.allCards
            CardTab.DRAFTS -> state.drafts
            CardTab.COMPLETED -> state.completed
        }
        
        if (cardsToShow.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (state.selectedTab) {
                        CardTab.ALL -> "No cards yet"
                        CardTab.DRAFTS -> "No drafts"
                        CardTab.COMPLETED -> "No completed cards"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = cardsToShow,
                    key = { it.id }
                ) { postcard ->
                    PostcardItem(
                        postcard = postcard,
                        onTap = { 
                            if (postcard.isDraft) {
                                onIntent(MyCardsIntent.EditCard(postcard.id))
                            } else {
                                onIntent(MyCardsIntent.ViewCard(postcard.id))
                            }
                        },
                        onDuplicate = { onIntent(MyCardsIntent.DuplicateCard(postcard)) },
                        onDelete = { onIntent(MyCardsIntent.DeleteCard(postcard.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No postcards yet",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first postcard to see it here",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (canRetry) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}