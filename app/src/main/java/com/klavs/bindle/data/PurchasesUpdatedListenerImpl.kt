package com.klavs.bindle.data

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PurchasesUpdatedListenerImpl : PurchasesUpdatedListener {

    private val _updatePurchaseResource = MutableStateFlow<Resource<Purchase>>(Resource.Idle())
    val updatePurchaseResource = _updatePurchaseResource.asStateFlow()

    fun resetPurchaseResource() {
        _updatePurchaseResource.value = Resource.Idle()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            val purchase = purchases.first()

            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                Log.e("Billing error", "satın alındı: ${purchase.products.first()}")
                _updatePurchaseResource.value =
                    Resource.Success(data = purchase)
            } else {
                Log.e("Billing error", "satın alınamadı: ${purchase.products.first()}")
                FirebaseCrashlytics.getInstance().log("Satın alma başarısız: ${billingResult.debugMessage}")
                _updatePurchaseResource.value =
                    Resource.Error(messageResource = R.string.purchase_canceled)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.e("Billing error", "satın alma iptal edildi")
            _updatePurchaseResource.value =
                Resource.Error(messageResource = R.string.purchase_canceled)
        } else {
            Log.e("Billing error", "satın alma başarısız: ${billingResult.debugMessage}")
            FirebaseCrashlytics.getInstance().log("Satın alma başarısız: ${billingResult.debugMessage}")
            _updatePurchaseResource.value =
                Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }



}