package solutions.appme.nosticard.features.editor.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import solutions.appme.nosticard.features.editor.view.components.*
import solutions.appme.nosticard.features.editor.viewmodel.*
import solutions.appme.nosticard.ui.components.showSnackbar
import solutions.appme.nosticard.ui.components.ImagePickerBottomSheet
import solutions.appme.nosticard.ui.components.LoadingButton
import solutions.appme.nosticard.ui.components.LoadingButtonVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    postcardId: String? = null,
    initialImageUri: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit,
    viewModel: EditorViewModel = koinViewModel { parametersOf(postcardId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showImagePicker by remember { mutableStateOf(false) }

    LaunchedEffect(initialImageUri) {
        initialImageUri?.let { viewModel.handleIntent(EditorIntent.LoadImage(it)) }
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is EditorEffect.NavigateBack -> onNavigateBack()
                is EditorEffect.NavigateToPreview -> onNavigateToPreview("")
                is EditorEffect.ShowSnackbar -> showSnackbar(snackbarHostState, effect.message)
                is EditorEffect.LaunchPhotoPicker -> showImagePicker = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        //contentWindowInsets = WindowInsets.safeDrawing // handle status/nav bars
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // ← keeps everything inside safe area
        ) {
            // TOP APP BAR is part of the layout → content cannot be behind it
            TopAppBar(
                title = { Text("Edit Postcard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.handleIntent(EditorIntent.SaveDraft) }) {
                        Icon(Icons.Default.Star, contentDescription = "Save Draft")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // force opaque bar
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // ------- EVERYTHING BELOW THE BAR -------
            when (val s = state) {
                is EditorState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is EditorState.Ready -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // STICKY: Postcard preview at top - always visible
                        PostcardPreview(
                            postcard = s.postcard,
                            previewBitmap = s.previewBitmap,
                            isProcessing = s.isProcessing,
                            onSelectImage = { viewModel.handleIntent(EditorIntent.SelectImage) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // SCROLLABLE: Only the middle controls section
                        val scrollState = rememberScrollState()
                        EditorControls(
                            state = s,
                            onIntent = viewModel::handleIntent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Takes remaining space between preview and buttons
                                .verticalScroll(scrollState)
                        )

                        // STICKY: Bottom action buttons - always visible
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LoadingButton(
                                text = "Save Draft",
                                isLoading = s.isSaving,
                                onClick = { viewModel.handleIntent(EditorIntent.SaveDraft) },
                                modifier = Modifier.weight(1f),
                                variant = LoadingButtonVariant.Outlined
                            )
                            Button(
                                onClick = { viewModel.handleIntent(EditorIntent.SaveCompleted) },
                                modifier = Modifier.weight(1f),
                                enabled = !s.isSaving
                            ) {
                                Text("Complete")
                            }
                        }
                    }
                }

                is EditorState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Something went wrong", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(s.message, style = MaterialTheme.typography.bodyMedium)
                        if (s.canRetry) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { /* hook retry */ }) { Text("Try Again") }
                        }
                    }
                }
            }

            // Bottom sheet at the end so it’s not clipped by weights
            ImagePickerBottomSheet(
                isVisible = showImagePicker,
                onImageSelected = { uri ->
                    viewModel.handleIntent(EditorIntent.LoadImage(uri.toString()))
                },
                onDismiss = { showImagePicker = false }
            )
        }
    }
}
