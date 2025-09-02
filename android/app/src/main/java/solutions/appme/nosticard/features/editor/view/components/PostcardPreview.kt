package solutions.appme.nosticard.features.editor.view.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import solutions.appme.nosticard.data.model.Postcard

@Composable
fun PostcardPreview(
    postcard: Postcard,
    previewBitmap: Bitmap?,
    isProcessing: Boolean,
    onSelectImage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = "Postcard preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onSelectImage() },
                contentScale = ContentScale.Fit
            )
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSelectImage,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add image",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Image")
                    }
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