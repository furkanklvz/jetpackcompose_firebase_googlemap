package com.klavs.bindle.uix.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.DetailedEvent
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventBottomSheetViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val firestoreRepo: FirestoreRepository
) :ViewModel(){

    val eventDetailsState: MutableState<Resource<DetailedEvent>> = mutableStateOf(Resource.Idle())
    private val _linkedCommunities = MutableStateFlow<Resource<List<Community>>>(Resource.Idle())
    val linkedCommunities = _linkedCommunities.asStateFlow()

    private val _amIParticipating = MutableStateFlow<Boolean?>(null)
    val amIParticipating = _amIParticipating.asStateFlow()
    private val _requestSent = MutableStateFlow<Boolean?>(null)
    val requestSent = _requestSent.asStateFlow()

    var listenParticipatingJob: Job? = null
    var listenRequestJob: Job? = null

    fun resetLinkedCommunities(){
        _linkedCommunities.value = Resource.Idle()
    }

    fun getLinkedCommunities(listOfCommunityIds: List<String>) {
        _linkedCommunities.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val communityList = mutableListOf<Community>()
            listOfCommunityIds.forEach { communityId ->
                val communityRef = db.collection("communities").document(communityId)
                val result = firestoreRepo.getDocument(
                    docRef = communityRef
                )
                if (result is Resource.Success) {
                    if (result.data != null && result.data.exists()) {
                        val communityObject = result.data.toObject(Community::class.java)?.copy(id = communityId)
                        if (communityObject != null) {
                            communityList.add(
                                communityObject
                            )
                        }
                    }
                }
            }
            _linkedCommunities.value = Resource.Success(data = communityList)
        }
    }


    fun sendRequest(eventId: String, myUid:String, newTickets:Long) {
            viewModelScope.launch(Dispatchers.Main) {
                val requestsRef = db.collection("events").document(eventId).collection("requests")
                val requestModel = hashMapOf(
                    "uid" to myUid,
                    "timestamp" to Timestamp.now()
                )
                val result = firestoreRepo.addDocument(
                    documentName = myUid,
                    collectionRef = requestsRef,
                    data = requestModel
                )
                if (result is Resource.Success){
                    val userRef = db.collection("users").document(myUid)
                    firestoreRepo.updateField(
                        documentRef = userRef,
                        fieldName = "tickets",
                        data = newTickets
                    )
                }
            }
    }

    fun participateTheEvent(eventId: String, ownerUid: String, myUid:String, newTickets:Long) {
            viewModelScope.launch(Dispatchers.Main) {
                val participantsRef =
                    db.collection("events").document(eventId).collection("participants")
                val participantModel = hashMapOf(
                    "uid" to myUid
                )
                firestoreRepo.addDocument(
                    documentName = myUid,
                    collectionRef = participantsRef,
                    data = participantModel
                )
                launch(Dispatchers.Main) {
                    val eventsRef = db.collection("users").document(myUid)
                        .collection("events")
                    val result = firestoreRepo.addDocument(
                        collectionRef = eventsRef,
                        documentName = eventId,
                        data = hashMapOf(
                            "id" to eventId,
                            "date" to null,
                            "ownerUid" to ownerUid
                        )
                    )
                    if (result is Resource.Success){
                        val userRef = db.collection("users").document(myUid)
                        firestoreRepo.updateField(
                            documentRef = userRef,
                            fieldName = "tickets",
                            data = newTickets
                        )
                    }
                }
            }
    }

    fun getEventDetails(event: Event, myUid:String?) {
        eventDetailsState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val participantsRef =
                db.collection("events").document(event.id).collection("participants")
            if (myUid != null) {
                listenParticipatingJob?.cancel()
                listenParticipatingJob = launch(Dispatchers.Main) {
                    if (event.ownerUid == myUid) {
                        _amIParticipating.value = true
                    } else {
                        _amIParticipating.value = null
                        firestoreRepo.memberCheck(
                            collectionRef = participantsRef,
                            fieldName = "uid",
                            value = myUid
                        ).collect { resource ->
                            if (resource is Resource.Success) {
                                _amIParticipating.value = resource.data!!
                            } else {
                                _amIParticipating.value = null
                            }
                        }
                    }
                }
                listenRequestJob?.cancel()
                val requestsRef = db.collection("events").document(event.id).collection("requests")
                listenRequestJob = launch(Dispatchers.Main) {
                    _requestSent.value = null
                    firestoreRepo.memberCheck(
                        collectionRef = requestsRef,
                        fieldName = "uid",
                        value = myUid
                    ).collect { resource ->
                        if (resource is Resource.Success) {
                            _requestSent.value = resource.data!!
                        } else {
                            _requestSent.value = null
                        }
                    }
                }
            }
            val participantsCount =
                firestoreRepo.countDocumentsWithoutResource(query = participantsRef)
            val eventOwnerState = firestoreRepo.getUserData(event.ownerUid)
            if (eventOwnerState is Resource.Success) {
                eventDetailsState.value = Resource.Success(
                    data = DetailedEvent(
                        event = event,
                        owner = eventOwnerState.data!!,
                        participantsCount = participantsCount
                    )
                )
            } else {
                eventDetailsState.value = Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
            }
        }
    }

}