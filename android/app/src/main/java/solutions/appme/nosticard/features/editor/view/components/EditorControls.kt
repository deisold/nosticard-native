package solutions.appme.nosticard.features.editor.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import solutions.appme.nosticard.data.model.*
import solutions.appme.nosticard.features.editor.viewmodel.EditorIntent
import solutions.appme.nosticard.features.editor.viewmodel.EditorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorControls(
    state: EditorState.Ready,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Text input
        OutlinedTextField(
            value = state.textInput,
            onValueChange = { 
                if (it.length <= 220) {
                    onIntent(EditorIntent.UpdateText(it))
                }
            },
            label = { Text("Add your message") },
            supportingText = {
                Text("${state.textInput.length}/220 characters")
            },
            isError = state.textInput.length > 200,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Filter selection
        FilterSelection(
            selectedFilter = state.selectedFilter,
            onFilterSelected = { filter ->
                onIntent(EditorIntent.ApplyFilter(filter))
            }
        )
        
        // Frame selection
        FrameSelection(
            selectedFrame = state.selectedFrame,
            onFrameSelected = { frame ->
                onIntent(EditorIntent.ApplyFrame(frame))
            }
        )
        
        // Text alignment
        TextAlignmentControls(
            selectedAlignment = state.textAlignment,
            onAlignmentChanged = { alignment ->
                onIntent(EditorIntent.UpdateTextAlignment(alignment))
            }
        )
        
        // Filter settings
        FilterSettingsControls(
            settings = state.filterSettings,
            onSettingsChanged = { settings ->
                onIntent(EditorIntent.UpdateFilterSettings(settings))
            }
        )
    }
}

@Composable
private fun FilterSelection(
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FilterType.values()) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter.displayName) }
                )
            }
        }
    }
}

@Composable
private fun FrameSelection(
    selectedFrame: FrameType,
    onFrameSelected: (FrameType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Frames",
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FrameType.values()) { frame ->
                FilterChip(
                    selected = selectedFrame == frame,
                    onClick = { onFrameSelected(frame) },
                    label = { Text(frame.displayName) }
                )
            }
        }
    }
}

@Composable
private fun TextAlignmentControls(
    selectedAlignment: TextAlignment,
    onAlignmentChanged: (TextAlignment) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Text Alignment",
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextAlignment.values().forEach { alignment ->
                FilterChip(
                    selected = selectedAlignment == alignment,
                    onClick = { onAlignmentChanged(alignment) },
                    label = { Text(alignment.displayName) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSettingsControls(
    settings: FilterSettings,
    onSettingsChanged: (FilterSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Vintage Effects",
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Grain level
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Grain")
                Text("${(settings.grainLevel * 100).toInt()}%")
            }
            
            Slider(
                value = settings.grainLevel,
                onValueChange = { value ->
                    onSettingsChanged(settings.copy(grainLevel = value))
                },
                valueRange = 0f..1f
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Vignette level
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vignette")
                Text("${(settings.vignetteLevel * 100).toInt()}%")
            }
            
            Slider(
                value = settings.vignetteLevel,
                onValueChange = { value ->
                    onSettingsChanged(settings.copy(vignetteLevel = value))
                },
                valueRange = 0f..1f
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Dust level
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dust")
                Text("${(settings.dustLevel * 100).toInt()}%")
            }
            
            Slider(
                value = settings.dustLevel,
                onValueChange = { value ->
                    onSettingsChanged(settings.copy(dustLevel = value))
                },
                valueRange = 0f..1f
            )
        }
    }
}

// Extension properties for display names
private val FilterType.displayName: String
    get() = when (this) {
        FilterType.NONE -> "None"
        FilterType.CLASSIC_BW -> "Classic B&W"
        FilterType.SEPIA_MEMORIES -> "Sepia"
        FilterType.FADED_COLOR -> "Faded"
    }

private val FrameType.displayName: String
    get() = when (this) {
        FrameType.NONE -> "None"
        FrameType.WHITE_BORDER -> "White Border"
        FrameType.DECKLE_EDGE -> "Deckle Edge"
        FrameType.STAMP_EDGE -> "Stamp Edge"
    }

private val TextAlignment.displayName: String
    get() = when (this) {
        TextAlignment.LEFT -> "Left"
        TextAlignment.CENTER -> "Center"
        TextAlignment.RIGHT -> "Right"
    }