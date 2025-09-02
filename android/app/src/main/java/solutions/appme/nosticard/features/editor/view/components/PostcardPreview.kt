package solutions.appme.nosticard.features.editor.view.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import solutions.appme.nosticard.data.model.Postcard

@Composable
fun PostcardPreview(
    postcard: Postcard,
    previewBitmap: Bitmap?,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (previewBitmap != null) {
            // TODO: Display the actual bitmap
            Text("Postcard Preview")
        } else {
            // Placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Postcard Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                
                if (postcard.imagePath.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select an image to start",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = Color.White)
            }
        }
    }
}