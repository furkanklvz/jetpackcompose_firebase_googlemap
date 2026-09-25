package com.klavs.bindle.data.repo.googleplaybilling

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.klavs.bindle.data.datasource.googleplaybilling.GooglePlayBillingDataSource
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GooglePlayBillingRepositoryImpl @Inject constructor(
    private val ds: GooglePlayBillingDataSource
) : GooglePlayBillingRepository {
    override fun startConnection(): Flow<Resource<Unit>> =
        ds.startConnection().flowOn(Dispatchers.IO)

    override suspend fun queryProductDetails(): Resource<List<ProductDetails>> =
        withContext(Dispatchers.IO) { ds.queryProductDetails() }

    override fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails
    ): Resource<Unit> = ds.launchBillingFlow(
        activity = activity,
        productDetails = productDetails
    )

    override suspend fun handlePurchase(
        purchase: Purchase,
        uid: String,
        currentTickets: Long
    ): Resource<Purchase> = withContext(Dispatchers.IO) {
        ds.handlePurchase(
            purchase = purchase,
            uid = uid,
            currentTickets = currentTickets
        )
    }

    override suspend fun queryPurchases(uid: String) =
        withContext(Dispatchers.IO){ds.queryPurchases(uid)}

    override suspend fun consumePurchase(purchase: Purchase, uid:String): Resource<Purchase> =
        withContext(Dispatchers.IO){ds.consumePurchase(purchase,uid)}
}