package solutions.appme.nosticard.features.settings.viewmodel

import solutions.appme.nosticard.data.repository.BillingProduct
import solutions.appme.nosticard.ui.components.SnackbarMessage

sealed class SettingsState {
    object Loading : SettingsState()
    
    data class Ready(
        val isPremiumUser: Boolean,
        val availableProducts: List<BillingProduct>,
        val ownedProducts: List<String>,
        val isProcessingPurchase: Boolean = false,
        val isRestoringPurchases: Boolean = false
    ) : SettingsState()
    
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : SettingsState()
}

sealed class SettingsIntent {
    object Load : SettingsIntent()
    object PurchasePremium : SettingsIntent()
    data class PurchaseStylePack(val productId: String) : SettingsIntent()
    object RestorePurchases : SettingsIntent()
    object ShowPrivacyPolicy : SettingsIntent()
    object ShowTermsOfService : SettingsIntent()
    object ContactSupport : SettingsIntent()
}

sealed class SettingsEffect {
    data class ShowSnackbar(val message: SnackbarMessage) : SettingsEffect()
    data class ShowPurchaseDialog(val product: BillingProduct) : SettingsEffect()
    data class OpenUrl(val url: String) : SettingsEffect()
    data class SendEmail(val email: String, val subject: String) : SettingsEffect()
}