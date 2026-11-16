package com.example.game.progression

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PlayBillingStatus {
    data object Checking : PlayBillingStatus
    data class Available(val products: List<ProductDetails>) : PlayBillingStatus
    data class Unavailable(val reason: String) : PlayBillingStatus
}

/**
 * Google Play Billing Library 7.x integration for Zenith Golf 2.0 optional store bundles.
 * Queries live Play Console INAPP ProductDetails when Google Play Services is available
 * and reports honest unavailability when offline or SKUs are not yet published.
 */
class PlayBillingHelper(
    context: Context,
    private val onVerifiedCoinsGranted: (Int) -> Unit = {}
) : PurchasesUpdatedListener {

    private val _status = MutableStateFlow<PlayBillingStatus>(PlayBillingStatus.Checking)
    val status: StateFlow<PlayBillingStatus> = _status.asStateFlow()

    private var billingClient: BillingClient? = null

    init {
        runCatching {
            val client = BillingClient.newBuilder(context.applicationContext)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                )
                .build()
            billingClient = client
            startConnection(client)
        }.onFailure { err ->
            _status.value = PlayBillingStatus.Unavailable(
                err.localizedMessage ?: "Google Play Billing unavailable on this device."
            )
        }
    }

    private fun startConnection(client: BillingClient) {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryCatalog(client)
                } else {
                    _status.value = PlayBillingStatus.Unavailable(
                        "Play Store Billing offline (${billingResult.debugMessage.ifBlank { "Requires active Play Store & published SKUs" }})."
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _status.value = PlayBillingStatus.Unavailable("Play Store Billing disconnected. Reconnecting when online.")
            }
        })
    }

    private fun queryCatalog(client: BillingClient) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_COIN_PACK_SMALL)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_VIP_TOUR_PASS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                _status.value = PlayBillingStatus.Available(productDetailsList)
            } else {
                _status.value = PlayBillingStatus.Unavailable(
                    "Play Console INAPP SKUs are not published on this Closed Testing track yet. Earned Tour Coins remain active."
                )
            }
        }
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails): Boolean {
        val client = billingClient ?: return false
        if (!client.isReady) return false
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .apply { if (offerToken != null) setOfferToken(offerToken) }
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = client.launchBillingFlow(activity, flowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(ackParams) { ackResult ->
                        if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            if (purchase.products.contains(SKU_COIN_PACK_SMALL)) {
                                onVerifiedCoinsGranted(1200)
                            }
                        }
                    }
                }
            }
        }
    }

    fun release() {
        runCatching { billingClient?.endConnection() }
    }

    companion object {
        const val SKU_COIN_PACK_SMALL = "zenith_coins_1200"
        const val SKU_VIP_TOUR_PASS = "zenith_vip_tour_pass"
    }
}
