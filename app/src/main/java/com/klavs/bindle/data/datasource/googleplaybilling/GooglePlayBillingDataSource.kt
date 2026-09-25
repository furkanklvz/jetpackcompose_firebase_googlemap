package com.klavs.bindle.data.datasource.googleplaybilling

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.flow.Flow

interface GooglePlayBillingDataSource {
    fun startConnection(): Flow<Resource<Unit>>
    suspend fun queryProductDetails(): Resource<List<ProductDetails>>
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails): Resource<Unit>
    suspend fun handlePurchase(purchase: Purchase, uid:String, currentTickets: Long): Resource<Purchase>
    suspend fun queryPurchases(uid:String): Resource<List<Purchase>>
    suspend fun consumePurchase(purchase: Purchase, uid:String): Resource<Purchase>
}