package com.klavs.bindle.uix.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.PurchasesUpdatedListenerImpl
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.googleplaybilling.GooglePlayBillingRepository
import com.klavs.bindle.resource.Resource
import com.klavs.bindle.resource.RewardedAdResource
import com.klavs.bindle.util.UtilFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketDialogViewModel @Inject constructor(
    private val billingRepo: GooglePlayBillingRepository,
    private val adRequest: AdRequest,
    private val db: FirebaseFirestore,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _productsResource =
        MutableStateFlow<Resource<List<ProductDetails>>>(Resource.Idle())
    val productsResource = _productsResource.asStateFlow()



    var listenToPurchaseJob: Job? = null


    private val _rewardedAd = MutableStateFlow<Resource<RewardedAd>>(Resource.Idle())
    val rewardedAd = _rewardedAd.asStateFlow()

    private val _rewardedAdContentStateResource = MutableStateFlow<RewardedAdResource?>(null)
    val rewardedAdContentStateResource = _rewardedAdContentStateResource.asStateFlow()

    private val _rewardResource = MutableStateFlow<Resource<Pair<Int, Int>>>(Resource.Idle())
    val rewardResource = _rewardResource.asStateFlow()

    fun reward(uid: String, currentTickets: Long, rewardAmount: Int, rewardType: String) {
        _rewardedAdContentStateResource.value = null
        _rewardResource.value = Resource.Loading()
        viewModelScope.launch {
            val userRef = db.collection("users").document(uid)
            val updateState = firestoreRepo.updateField(
                documentRef = userRef,
                fieldName = "tickets",
                data = currentTickets + 1
            )
            if (updateState is Resource.Success) {
                _rewardResource.value = Resource.Success(
                    data = Pair(
                        rewardAmount,
                        UtilFunctions().getRewardTitle(rewardType)
                    )
                )
            } else {
                _rewardResource.value =
                    Resource.Error(messageResource = R.string.rewarding_error_message)
            }
        }

    }

    fun loadRewardedAd(context: Context) {
        _rewardedAd.value = Resource.Loading()
        RewardedAd.load(
            context,
            context.getString(R.string.rewarded_ad_id),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    FirebaseCrashlytics.getInstance()
                        .log("Rewarded ad failed to load: ${adError.message}")
                    Log.e("RewardedAd", "Rewarded ad failed to load: ${adError.message}")
                    _rewardedAd.value =
                        Resource.Error(messageResource = R.string.ad_cannot_be_loaded)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.e("RewardedAd", "Rewarded ad was loaded")
                    _rewardedAd.value = Resource.Success(data = ad)
                }
            })
    }

    fun showRewardedAd(rewardedAd: RewardedAd, activity: Activity) {
        rewardedAd.show(activity) { rewardItem ->
            // Handle the reward.
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            _rewardedAdContentStateResource.value = RewardedAdResource.OnUserEarnedReward(
                rewardAmount = rewardAmount,
                rewardType = rewardType
            )

            Log.d("RewardedAd", "User earned the reward: $rewardAmount $rewardType")
        }


        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                _rewardedAdContentStateResource.value = RewardedAdResource.onAdClicked()
                Log.d("RewardedAd", "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                if (_rewardedAdContentStateResource.value !is RewardedAdResource.OnUserEarnedReward
                    && _rewardedAdContentStateResource.value != null
                ) {
                    _rewardedAdContentStateResource.value =
                        RewardedAdResource.onAdDismissedFullScreenContent()
                    Log.d("RewardedAd", "Ad dismissed fullscreen content.")
                }
                _rewardedAd.value = Resource.Idle()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when ad fails to show.
                _rewardedAdContentStateResource.value =
                    RewardedAdResource.onAdFailedToShowFullScreenContent()
                Log.e("RewardedAd", "Ad failed to show fullscreen content: ${adError.message}")
                _rewardedAd.value = Resource.Idle()
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                _rewardedAdContentStateResource.value = RewardedAdResource.onAdImpression()
                Log.d("RewardedAd", "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                _rewardedAdContentStateResource.value =
                    RewardedAdResource.onAdShowedFullScreenContent()
                Log.d("RewardedAd", "Ad showed fullscreen content.")
            }
        }
    }

    fun startConnection() {
        _productsResource.value = Resource.Loading()
        viewModelScope.launch {
            billingRepo.startConnection().collect { result ->
                if (result is Resource.Success) {
                    _productsResource.value = billingRepo.queryProductDetails()
                } else {
                    _productsResource.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            }
        }
    }

    fun purchase(
        productDetails: ProductDetails,
        activity: Activity
    ) {
        viewModelScope.launch(Dispatchers.Main) {
           billingRepo.launchBillingFlow(
                activity = activity,
                productDetails = productDetails
            )
        }
    }

    fun resetRewardedAdContentStateResource() {
        _rewardedAdContentStateResource.value = null
    }

    fun resetRewardResource() {
        _rewardResource.value = Resource.Idle()
    }

    fun reset() {
        _rewardedAd.value = Resource.Idle()
        _rewardedAdContentStateResource.value = null
        _rewardResource.value = Resource.Idle()
        _productsResource.value = Resource.Idle()

        listenToPurchaseJob?.cancel()
        listenToPurchaseJob = null
    }
}