package solutions.appme.nosticard.features.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import solutions.appme.nosticard.data.repository.BillingRepository
import solutions.appme.nosticard.data.repository.PurchaseResult
import solutions.appme.nosticard.ui.components.SnackbarMessages

class SettingsViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        handleIntent(SettingsIntent.Load)
    }

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.Load -> loadSettings()
            is SettingsIntent.PurchasePremium -> purchasePremium()
            is SettingsIntent.PurchaseStylePack -> purchaseStylePack(intent.productId)
            is SettingsIntent.RestorePurchases -> restorePurchases()
            is SettingsIntent.ShowPrivacyPolicy -> showPrivacyPolicy()
            is SettingsIntent.ShowTermsOfService -> showTermsOfService()
            is SettingsIntent.ContactSupport -> contactSupport()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = SettingsState.Loading
            
            try {
                billingRepository.initializeBilling()
                
                combine(
                    billingRepository.isPremiumUser,
                    billingRepository.availableProducts
                ) { isPremium, products ->
                    Pair(isPremium, products)
                }.collect { (isPremium, products) ->
                    val ownedProducts = billingRepository.getOwnedProducts()
                    
                    _state.value = SettingsState.Ready(
                        isPremiumUser = isPremium,
                        availableProducts = products,
                        ownedProducts = ownedProducts
                    )
                }
            } catch (e: Exception) {
                _state.value = SettingsState.Error(
                    message = e.message ?: "Failed to load settings"
                )
            }
        }
    }

    private fun purchasePremium() {
        val currentState = _state.value
        if (currentState is SettingsState.Ready) {
            _state.value = currentState.copy(isProcessingPurchase = true)
            
            viewModelScope.launch {
                billingRepository.purchasePremium()
                    .onSuccess { result ->
                        _state.value = currentState.copy(isProcessingPurchase = false)
                        handlePurchaseResult(result)
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isProcessingPurchase = false)
                        _effect.emit(SettingsEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Purchase failed")
                        ))
                    }
            }
        }
    }

    private fun purchaseStylePack(productId: String) {
        val currentState = _state.value
        if (currentState is SettingsState.Ready) {
            _state.value = currentState.copy(isProcessingPurchase = true)
            
            viewModelScope.launch {
                billingRepository.purchaseStylePack(productId)
                    .onSuccess { result ->
                        _state.value = currentState.copy(isProcessingPurchase = false)
                        handlePurchaseResult(result)
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isProcessingPurchase = false)
                        _effect.emit(SettingsEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Purchase failed")
                        ))
                    }
            }
        }
    }

    private fun restorePurchases() {
        val currentState = _state.value
        if (currentState is SettingsState.Ready) {
            _state.value = currentState.copy(isRestoringPurchases = true)
            
            viewModelScope.launch {
                billingRepository.restorePurchases()
                    .onSuccess { restoredProducts ->
                        _state.value = currentState.copy(isRestoringPurchases = false)
                        
                        val message = if (restoredProducts.isNotEmpty()) {
                            "Restored ${restoredProducts.size} purchases"
                        } else {
                            "No purchases found to restore"
                        }
                        _effect.emit(SettingsEffect.ShowSnackbar(SnackbarMessages.success(message)))
                    }
                    .onFailure { error ->
                        _state.value = currentState.copy(isRestoringPurchases = false)
                        _effect.emit(SettingsEffect.ShowSnackbar(
                            SnackbarMessages.error(error.message ?: "Failed to restore purchases")
                        ))
                    }
            }
        }
    }

    private suspend fun handlePurchaseResult(result: PurchaseResult) {
        when (result) {
            is PurchaseResult.Success -> {
                _effect.emit(SettingsEffect.ShowSnackbar(SnackbarMessages.success("Purchase successful!")))
            }
            is PurchaseResult.Error -> {
                _effect.emit(SettingsEffect.ShowSnackbar(SnackbarMessages.error(result.message)))
            }
            is PurchaseResult.Cancelled -> {
                // Don't show error for user cancellation
            }
            is PurchaseResult.AlreadyOwned -> {
                _effect.emit(SettingsEffect.ShowSnackbar(SnackbarMessages.success("You already own this product")))
            }
        }
    }

    private fun showPrivacyPolicy() {
        viewModelScope.launch {
            _effect.emit(SettingsEffect.OpenUrl("https://nosticard.com/privacy"))
        }
    }

    private fun showTermsOfService() {
        viewModelScope.launch {
            _effect.emit(SettingsEffect.OpenUrl("https://nosticard.com/terms"))
        }
    }

    private fun contactSupport() {
        viewModelScope.launch {
            _effect.emit(SettingsEffect.SendEmail(
                email = "support@nosticard.com",
                subject = "NostiCard Support Request"
            ))
        }
    }
}