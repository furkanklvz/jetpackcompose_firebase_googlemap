package com.klavs.bindle.uix.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.PurchasesUpdatedListenerImpl
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.googleplaybilling.GooglePlayBillingRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class NavHostViewModel @Inject constructor(
    auth: FirebaseAuth,
    private val authRepo: AuthRepository,
    private val firestoreRepo: FirestoreRepository,
    private val purchasesUpdateListener: PurchasesUpdatedListenerImpl,
    private val db: FirebaseFirestore,
    private val billingRepo: GooglePlayBillingRepository
) : ViewModel() {
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<Resource<List<Event>>>(Resource.Idle())
    val upcomingEvents = _upcomingEvents.asStateFlow()

    private val _userResourceFlow = MutableStateFlow<Resource<User>>(Resource.Idle())
    val userResourceFlow = _userResourceFlow.asStateFlow()

    var listenToUserJob: Job? = null

    val communitiesSearchBarExpanded = MutableStateFlow<Boolean>(false)
    private val _purchase = MutableStateFlow<Resource<Purchase>>(Resource.Idle())
    val purchase = _purchase.asStateFlow()


    var listenToPurchaseJob: Job? = null

    fun startToListenUserDocument(uid: String) {
        _userResourceFlow.value = Resource.Loading()

        listenToUserJob?.cancel()
        listenToUserJob = viewModelScope.launch(Dispatchers.Main) {
            firestoreRepo.getUserDataFlow(
                uid = uid
            ).collect {
                _userResourceFlow.value = it
                if (_userResourceFlow.value.data != null) {
                    if (Locale.getDefault().language != _userResourceFlow.value.data?.language) {
                        changeUserLanguage(uid, Locale.getDefault().language)
                    }
                    if (listenToPurchaseJob == null || listenToPurchaseJob?.isCancelled == true) {
                        queryPurchases(
                            uid = uid
                        )
                        listenToPurchaseState(uid)
                    }
                }
            }
        }
    }

    private fun queryPurchases(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            billingRepo.startConnection().collect { resource ->
                if (resource is Resource.Success) {
                    val queryPurchasesResource = billingRepo.queryPurchases(uid)
                    if (queryPurchasesResource is Resource.Success) {
                        queryPurchasesResource.data!!.forEachIndexed { index, purchase ->
                            if (_userResourceFlow.value.data != null) {
                                val handlePurchaseResource = billingRepo.handlePurchase(
                                    purchase = purchase,
                                    uid = uid,
                                    currentTickets = _userResourceFlow.value.data!!.tickets
                                )
                                if (handlePurchaseResource is Resource.Success && handlePurchaseResource.data != null) {
                                    billingRepo.consumePurchase(handlePurchaseResource.data, uid)
                                }
                                if (index == queryPurchasesResource.data.size - 1) {
                                    _purchase.value = handlePurchaseResource
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private fun listenToPurchaseState(uid: String) {
        Log.d("billing error", "listenToPurchaseState: called")
        purchasesUpdateListener.resetPurchaseResource()
        listenToPurchaseJob?.cancel()
        if (_userResourceFlow.value.data != null) {
            listenToPurchaseJob = viewModelScope.launch(Dispatchers.IO) {
                Log.d("billing error", "updatePurchaseResource dinlenmeye başlandı")
                purchasesUpdateListener.updatePurchaseResource.collect { resource ->
                    if (resource is Resource.Success && resource.data != null) {
                        Log.d("billing error", "satın alma başarılı, handle purchase çağırılıyor")
                        _purchase.value = billingRepo.handlePurchase(
                            purchase = resource.data,
                            uid = uid,
                            currentTickets = _userResourceFlow.value.data!!.tickets
                        )
                        if (_purchase.value is Resource.Success && _purchase.value.data != null) {
                            billingRepo.consumePurchase(_purchase.value.data!!, uid)
                        }
                    } else if (resource is Resource.Error) {
                        _purchase.value = resource
                    }
                }
            }
        }
    }

    private fun changeUserLanguage(uid: String, language: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val userRef = db.collection("users").document(uid)
            firestoreRepo.updateField(
                documentRef = userRef,
                fieldName = "language",
                data = language
            )
        }
    }

    init {
        viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun acceptTermsAndPrivacyPolicy(uid: String) {
        viewModelScope.launch {
            val userRef = db.collection("users").document(uid)
            firestoreRepo.updateField(
                documentRef = userRef,
                fieldName = "acceptedTermsAndPrivacyPolicy",
                data = true
            )
        }
    }

    fun listenToUpcomingEvents(myUid: String) {
        _upcomingEvents.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val eventIdsRef =
                db.collectionGroup("participants")
                    .whereEqualTo("uid", myUid)
                    .whereEqualTo("date", null)
            firestoreRepo.getCollectionWithListener(
                query = eventIdsRef
            ).collect { resource ->
                if (resource is Resource.Success) {
                    val eventRefs = resource.data!!.mapNotNull {
                        it.reference.parent.parent
                    }
                    val events = mutableListOf<Event>()
                    eventRefs.forEach { eventRef ->
                        val eventResource = firestoreRepo.getDocument(
                            docRef = eventRef
                        )
                        if (eventResource is Resource.Success) {
                            if (eventResource.data?.exists() == true) {
                                val eventObject = eventResource.data.toObject(Event::class.java)
                                    ?.copy(id = eventRef.id)
                                if (eventObject != null) {
                                    events.add(
                                        eventObject
                                    )
                                }
                            } else {
                                val participantRef =
                                    eventRef.collection("participants").document(myUid)
                                firestoreRepo.deleteDocument(
                                    documentRef = participantRef
                                )
                            }
                        }
                    }
                    launch(Dispatchers.Main) {
                        events.forEach { event ->
                            if (event.date < Timestamp.now()) {
                                val participantRef = db.collection("events")
                                    .document(event.id)
                                    .collection("participants").document(myUid)
                                firestoreRepo.updateField(
                                    documentRef = participantRef,
                                    fieldName = "date",
                                    data = event.date
                                )
                            }
                        }
                    }
                    _upcomingEvents.value = Resource.Success(events)

                } else {
                    _upcomingEvents.value = Resource.Error(
                        messageResource = resource.messageResource ?: R.string.something_went_wrong
                    )
                }
            }
        }
    }

}