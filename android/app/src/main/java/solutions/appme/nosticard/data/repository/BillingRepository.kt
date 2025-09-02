package solutions.appme.nosticard.data.repository

import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val isPremiumUser: Flow<Boolean>
    val availableProducts: Flow<List<BillingProduct>>
    
    suspend fun initializeBilling(): Result<Unit>
    suspend fun purchasePremium(): Result<PurchaseResult>
    suspend fun purchaseStylePack(productId: String): Result<PurchaseResult>
    suspend fun restorePurchases(): Result<List<String>>
    suspend fun getOwnedProducts(): List<String>
}

data class BillingProduct(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val type: ProductType
)

enum class ProductType {
    PREMIUM,
    STYLE_PACK
}

sealed class PurchaseResult {
    object Success : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
    object Cancelled : PurchaseResult()
    object AlreadyOwned : PurchaseResult()
}