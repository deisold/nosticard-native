package solutions.appme.nosticard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: LoadingButtonVariant = LoadingButtonVariant.Filled
) {
    val isButtonEnabled = enabled && !isLoading
    
    when (variant) {
        LoadingButtonVariant.Filled -> {
            Button(
                onClick = onClick,
                enabled = isButtonEnabled,
                modifier = modifier
            ) {
                LoadingButtonContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        LoadingButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                enabled = isButtonEnabled,
                modifier = modifier
            ) {
                LoadingButtonContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun LoadingButtonContent(
    text: String,
    isLoading: Boolean,
    contentColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            color = contentColor
        )
    }
}

enum class LoadingButtonVariant {
    Filled,
    Outlined
}