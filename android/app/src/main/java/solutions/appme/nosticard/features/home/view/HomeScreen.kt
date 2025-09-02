package solutions.appme.nosticard.features.home.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import solutions.appme.nosticard.features.home.view.components.PostcardItem
import solutions.appme.nosticard.features.home.viewmodel.*
import solutions.appme.nosticard.ui.components.showSnackbar
import solutions.appme.nosticard.ui.components.ImagePickerBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEditor: () -> Unit,
    onNavigateToEditorWithImage: (String) -> Unit,
    onNavigateToMyCards: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPostcard: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showImagePicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToEditor -> onNavigateToEditor()
                is HomeEffect.NavigateToPostcard -> onNavigateToPostcard(effect.postcardId)
                is HomeEffect.ShowSnackbar -> {
                    showSnackbar(snackbarHostState, effect.message)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NostiCard") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showImagePicker = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Postcard")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        HomeContent(
            state = state,
            onIntent = viewModel::handleIntent,
            onNavigateToMyCards = onNavigateToMyCards,
            onShowImagePicker = { showImagePicker = true },
            modifier = Modifier.padding(paddingValues)
        )
        
        ImagePickerBottomSheet(
            isVisible = showImagePicker,
            onImageSelected = { uri ->
                onNavigateToEditorWithImage(uri.toString())
            },
            onDismiss = { showImagePicker = false }
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onNavigateToMyCards: () -> Unit,
    onShowImagePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is HomeState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is HomeState.Empty -> {
            EmptyStateContent(
                onCreateFirst = onShowImagePicker,
                modifier = modifier
            )
        }
        
        is HomeState.Success -> {
            SuccessContent(
                state = state,
                onIntent = onIntent,
                onNavigateToMyCards = onNavigateToMyCards,
                modifier = modifier
            )
        }
        
        is HomeState.Error -> {
            ErrorContent(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = { onIntent(HomeIntent.Load) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun EmptyStateContent(
    onCreateFirst: () -> Unit,
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
            text = "Welcome to NostiCard!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Create your first nostalgic postcard by tapping the + button below",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onCreateFirst) {
            Text("Create First Postcard")
        }
    }
}

@Composable
private fun SuccessContent(
    state: HomeState.Success,
    onIntent: (HomeIntent) -> Unit,
    onNavigateToMyCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StatsCard(
                totalCards = state.totalCount,
                drafts = state.drafts.size,
                completed = state.completed.size,
                onViewAll = onNavigateToMyCards
            )
        }
        
        if (state.drafts.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Drafts",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(
                items = state.drafts.take(3),
                key = { it.id }
            ) { postcard ->
                PostcardItem(
                    postcard = postcard,
                    onTap = { onIntent(HomeIntent.NavigateToPostcard(postcard.id)) },
                    onDuplicate = { onIntent(HomeIntent.DuplicatePostcard(postcard)) },
                    onDelete = { onIntent(HomeIntent.DeletePostcard(postcard.id)) }
                )
            }
        }
        
        if (state.completed.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Completed",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(
                items = state.completed.take(3),
                key = { it.id }
            ) { postcard ->
                PostcardItem(
                    postcard = postcard,
                    onTap = { onIntent(HomeIntent.NavigateToPostcard(postcard.id)) },
                    onDuplicate = { onIntent(HomeIntent.DuplicatePostcard(postcard)) },
                    onDelete = { onIntent(HomeIntent.DeletePostcard(postcard.id)) }
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    totalCards: Int,
    drafts: Int,
    completed: Int,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onViewAll
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Postcards",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Total", value = totalCards.toString())
                StatItem(label = "Drafts", value = drafts.toString())
                StatItem(label = "Completed", value = completed.toString())
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
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
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        if (canRetry) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}