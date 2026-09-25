package com.klavs.bindle.data.datasource.googleplaybilling

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GooglePlayBillingDataSourceImpl @Inject constructor(
    private val billingClient: BillingClient,
    private val context: Context,
    private val firestoreRepo: FirestoreRepository,
    private val db: FirebaseFirestore
) :
    GooglePlayBillingDataSource {

    override fun startConnection(): Flow<Resource<Unit>> = callbackFlow {
        if (billingClient.connectionState == BillingClient.ConnectionState.CONNECTED
            && billingClient.isReady
        ) {
            Log.e("Billing error", "billing client zaten bağlanmıştı")
            trySend(Resource.Success(Unit))
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.e("Billing error", "billing client bağlandı")
                        trySend(Resource.Success(Unit))
                    } else {
                        Log.e(
                            "Billing error",
                            "billing client bağlanamadı: ${billingResult.debugMessage}"
                        )
                        FirebaseCrashlytics.getInstance()
                            .log("startConnection hatası: ${billingResult.debugMessage}")
                        trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.e(
                        "Billing error",
                        "startConnection hatası: Disconnected"
                    )
                    FirebaseCrashlytics.getInstance().log("startConnection hatası: Disconnected")
                    trySend(Resource.Error(messageResource = R.string.something_went_wrong))
                }
            })
        }
        awaitClose {}
    }

    override suspend fun queryProductDetails(): Resource<List<ProductDetails>> {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(context.getString(R.string.ten_tickets))
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(context.getString(R.string.twenty_tickets))
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(context.getString(R.string.thirty_tickets))
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        val productDetailsResult = billingClient.queryProductDetails(params.build())

        return if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && productDetailsResult.productDetailsList != null
        ) {
            Log.e(
                "Billing error",
                "ürünler listelendi: ${productDetailsResult.productDetailsList?.size}"
            )
            Resource.Success(productDetailsResult.productDetailsList!!)
        } else {
            FirebaseCrashlytics.getInstance()
                .log("Ürün listemele hatası: ${productDetailsResult.billingResult.debugMessage}")
            Log.e(
                "Billing error",
                "Ürün listemele hatası: ${productDetailsResult.billingResult.debugMessage}"
            )
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override fun launchBillingFlow(
        activity: Activity,
        productDetails: ProductDetails
    ): Resource<Unit> {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.e("Billing error", "client başlatıldı")
            Resource.Success(Unit)
        } else {
            FirebaseCrashlytics.getInstance()
                .log("client başlatma hatası: ${billingResult.debugMessage}")
            Log.e("Billing error", "client başlatma hatası: ${billingResult.debugMessage}")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }


    override suspend fun handlePurchase(
        purchase: Purchase,
        uid: String,
        currentTickets: Long
    ): Resource<Purchase> {
        return try {
            when (purchase.products.first()) {
                context.getString(R.string.ten_tickets) -> processPurchase(
                    uid,
                    purchase,
                    currentTickets + 10
                )

                context.getString(R.string.twenty_tickets) -> processPurchase(
                    uid,
                    purchase,
                    currentTickets + 20
                )

                context.getString(R.string.thirty_tickets) -> processPurchase(
                    uid,
                    purchase,
                    currentTickets + 30
                )

                else -> {
                    Log.e(
                        "Billing error",
                        "hata, purchase.products.first(): ${purchase.products.first()}"
                    )
                    Resource.Error(messageResource = R.string.something_went_wrong)
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Billing error", "satın alındı ama ödüllendirilemedi: ${e.message}")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }


    private suspend fun processPurchase(
        uid: String,
        purchase: Purchase,
        ticketCount: Long
    ): Resource<Purchase> {
        val userRef = db.collection("users").document(uid)
        val dbState = firestoreRepo.updateField(
            documentRef = userRef,
            fieldName = "tickets",
            data = ticketCount
        )
        val purchaseObject = com.klavs.bindle.data.entity.Purchase(
            orderID = purchase.orderId,
            timestamp = Timestamp.now(),
            productId = purchase.products.first(),
            uid = uid,
            consumed = false,
            purchaseToken = purchase.purchaseToken
        )
        firestoreRepo.addDocument(
            documentName = purchase.purchaseToken,
            collectionRef = userRef.collection("purchases"),
            data = purchaseObject
        )
        return if (dbState is Resource.Success) {
            Log.e("Billing error", "satın alındı ve ödüllendirildi")
            Resource.Success(data = purchase)
        } else {
            Log.e("Billing error", "satın alındı fakat ödüllendirilemedi")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }


    override suspend fun queryPurchases(
        uid: String
    ): Resource<List<Purchase>> =
        suspendCoroutine { continuation ->
            val queryPurchasesParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchasedList =
                        purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    Log.e("Billing error", "Satın almalar sorgulandı: ${purchasedList.size}")
                    continuation.resume(Resource.Success(data = purchasedList))
                } else {
                    Log.e(
                        "Billing error",
                        "Satın almalar sorgulanamadı: ${billingResult.debugMessage}"
                    )
                    continuation.resume(Resource.Error(messageResource = R.string.something_went_wrong))
                }
            }
        }

    override suspend fun consumePurchase(purchase: Purchase, uid: String): Resource<Purchase> {
        return try {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            val consumeState = billingClient.consumePurchase(consumeParams)
            if (consumeState.billingResult.responseCode == BillingClient.BillingResponseCode.OK && consumeState.purchaseToken == purchase.purchaseToken) {
                Log.d("Billing error", "Satın alma tüketme başarılı: ${consumeState.purchaseToken}")
                firestoreRepo.updateField(
                    documentRef = db.collection("users").document(uid).collection("purchases")
                        .document(consumeState.purchaseToken!!),
                    fieldName = "consumed",
                    data = true
                )
                Log.e(
                    "Billing error",
                    "firestore consumed değeri güncellendi"
                )
                Resource.Success(data = purchase)
            } else {
                FirebaseCrashlytics.getInstance()
                    .log("Satın alma tüketme başarısız: ${consumeState.billingResult.debugMessage}")
                Log.e(
                    "Billing error",
                    "Satın alma tüketme başarısız: ${consumeState.billingResult.debugMessage}"
                )
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .recordException(e)
            Log.e("Billing error", "Satın alma tüketme başarısız: $e")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }


    }
}