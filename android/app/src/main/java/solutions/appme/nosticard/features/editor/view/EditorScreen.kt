package solutions.appme.nosticard.features.editor.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import solutions.appme.nosticard.features.editor.view.components.*
import solutions.appme.nosticard.features.editor.viewmodel.*
import solutions.appme.nosticard.ui.components.showSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    postcardId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit,
    viewModel: EditorViewModel = koinViewModel { parametersOf(postcardId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is EditorEffect.NavigateBack -> onNavigateBack()
                is EditorEffect.NavigateToPreview -> onNavigateToPreview("")
                is EditorEffect.ShowSnackbar -> {
                    showSnackbar(snackbarHostState, effect.message)
                }
                is EditorEffect.RequestPermission -> {
                    // TODO: Request permission
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Postcard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleIntent(EditorIntent.SaveDraft) }
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Save Draft")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        EditorContent(
            state = state,
            onIntent = viewModel::handleIntent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun EditorContent(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is EditorState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is EditorState.Ready -> {
            EditorReadyContent(
                state = state,
                onIntent = onIntent,
                modifier = modifier
            )
        }
        
        is EditorState.Error -> {
            ErrorContent(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = { /* TODO: Implement retry */ },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun EditorReadyContent(
    state: EditorState.Ready,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Main editing area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            PostcardPreview(
                postcard = state.postcard,
                previewBitmap = state.previewBitmap,
                isProcessing = state.isProcessing,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Controls panel
        EditorControls(
            state = state,
            onIntent = onIntent,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Bottom actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { onIntent(EditorIntent.SaveDraft) },
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Save Draft")
                }
            }
            
            Button(
                onClick = { onIntent(EditorIntent.SaveCompleted) },
                modifier = Modifier.weight(1f),
                enabled = !state.isSaving
            ) {
                Text("Complete")
            }
        }
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