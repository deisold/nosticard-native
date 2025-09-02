package solutions.appme.nosticard.features.preview.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import solutions.appme.nosticard.features.preview.viewmodel.*
import solutions.appme.nosticard.ui.components.showSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    postcardId: String,
    onNavigateBack: () -> Unit,
    onNavigateToMyCards: () -> Unit,
    viewModel: PreviewViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(postcardId) {
        viewModel.handleIntent(PreviewIntent.LoadPostcard(postcardId))
    }
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PreviewEffect.NavigateBack -> onNavigateBack()
                is PreviewEffect.NavigateToMyCards -> onNavigateToMyCards()
                is PreviewEffect.ShowSnackbar -> {
                    showSnackbar(snackbarHostState, effect.message)
                }
                is PreviewEffect.ShowUpgradeDialog -> {
                    // TODO: Show upgrade dialog
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleIntent(PreviewIntent.NavigateBack) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleIntent(PreviewIntent.ShareImage()) }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PreviewContent(
            state = state,
            onIntent = viewModel::handleIntent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun PreviewContent(
    state: PreviewState,
    onIntent: (PreviewIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is PreviewState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is PreviewState.Ready -> {
            PreviewReadyContent(
                state = state,
                onIntent = onIntent,
                modifier = modifier
            )
        }
        
        is PreviewState.Error -> {
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
private fun PreviewReadyContent(
    state: PreviewState.Ready,
    onIntent: (PreviewIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Preview area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.aspectRatio(4f / 3f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Final Postcard Preview")
                    // TODO: Show actual postcard preview
                }
            }
        }
        
        // Export controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onIntent(PreviewIntent.ExportAsJpeg) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isExporting
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Export JPEG")
                    }
                }
                
                Button(
                    onClick = { onIntent(PreviewIntent.ExportAsPdf) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isExporting && state.isPremiumUser
                ) {
                    Text(if (state.isPremiumUser) "Export PDF" else "PDF (Pro)")
                }
            }
            
            if (state.exportedImagePath != null || state.exportedPdfPath != null) {
                Divider()
                
                Text(
                    text = "Share Options",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.exportedImagePath != null) {
                        OutlinedButton(
                            onClick = { onIntent(PreviewIntent.ShareImage()) },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isSharing
                        ) {
                            Text("Share Image")
                        }
                    }
                    
                    if (state.exportedPdfPath != null) {
                        OutlinedButton(
                            onClick = { onIntent(PreviewIntent.SharePdf()) },
                            modifier = Modifier.weight(1f),
                            enabled = !state.isSharing
                        ) {
                            Text("Share PDF")
                        }
                    }
                }
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