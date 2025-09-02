package solutions.appme.nosticard.features.settings.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import solutions.appme.nosticard.data.repository.BillingProduct
import solutions.appme.nosticard.data.repository.ProductType
import solutions.appme.nosticard.features.settings.viewmodel.*
import solutions.appme.nosticard.ui.components.showSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowSnackbar -> {
                    showSnackbar(snackbarHostState, effect.message)
                }
                is SettingsEffect.ShowPurchaseDialog -> {
                    // TODO: Show purchase dialog
                }
                is SettingsEffect.OpenUrl -> {
                    // TODO: Open URL
                }
                is SettingsEffect.SendEmail -> {
                    // TODO: Send email
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        SettingsContent(
            state = state,
            onIntent = viewModel::handleIntent,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is SettingsState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is SettingsState.Ready -> {
            SettingsReadyContent(
                state = state,
                onIntent = onIntent,
                modifier = modifier
            )
        }
        
        is SettingsState.Error -> {
            ErrorContent(
                message = state.message,
                canRetry = state.canRetry,
                onRetry = { onIntent(SettingsIntent.Load) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun SettingsReadyContent(
    state: SettingsState.Ready,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium status
        item {
            PremiumStatusCard(
                isPremium = state.isPremiumUser,
                onUpgrade = { onIntent(SettingsIntent.PurchasePremium) },
                isProcessing = state.isProcessingPurchase
            )
        }
        
        // Available products
        if (state.availableProducts.isNotEmpty()) {
            item {
                Text(
                    text = "Available Purchases",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            items(state.availableProducts) { product ->
                ProductCard(
                    product = product,
                    isOwned = state.ownedProducts.contains(product.productId),
                    onPurchase = {
                        if (product.type == ProductType.PREMIUM) {
                            onIntent(SettingsIntent.PurchasePremium)
                        } else {
                            onIntent(SettingsIntent.PurchaseStylePack(product.productId))
                        }
                    },
                    isProcessing = state.isProcessingPurchase
                )
            }
        }
        
        item {
            Divider()
        }
        
        // Restore purchases
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onIntent(SettingsIntent.RestorePurchases) }
            ) {
                ListItem(
                    headlineContent = { Text("Restore Purchases") },
                    supportingContent = { Text("Restore your previous purchases") },
                    trailingContent = {
                        if (state.isRestoringPurchases) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                )
            }
        }
        
        item {
            Divider()
        }
        
        // Support & Legal
        item {
            Text(
                text = "Support & Legal",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onIntent(SettingsIntent.ContactSupport) }
            ) {
                ListItem(
                    headlineContent = { Text("Contact Support") },
                    supportingContent = { Text("Get help with NostiCard") }
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onIntent(SettingsIntent.ShowPrivacyPolicy) }
            ) {
                ListItem(
                    headlineContent = { Text("Privacy Policy") },
                    supportingContent = { Text("How we protect your data") }
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onIntent(SettingsIntent.ShowTermsOfService) }
            ) {
                ListItem(
                    headlineContent = { Text("Terms of Service") },
                    supportingContent = { Text("Terms and conditions") }
                )
            }
        }
    }
}

@Composable
private fun PremiumStatusCard(
    isPremium: Boolean,
    onUpgrade: () -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = if (isPremium) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isPremium) "Premium Active" else "Free Version",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = if (isPremium) {
                            "Thank you for supporting NostiCard!"
                        } else {
                            "Upgrade to unlock all features"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (isPremium) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Premium active",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (!isPremium) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Upgrade to Premium")
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: BillingProduct,
    isOwned: Boolean,
    onPurchase: () -> Unit,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isOwned && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    when {
                        isOwned -> "Owned"
                        isProcessing -> "Processing..."
                        else -> "Purchase"
                    }
                )
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