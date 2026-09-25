package com.klavs.bindle.uix.viewmodel.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.RequestForEvent
import com.klavs.bindle.data.entity.User
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val firestoreRepo: FirestoreRepository,
) : ViewModel() {


    val pastEvents = MutableStateFlow<Resource<List<Event>>>(Resource.Idle())

    private val _event = MutableStateFlow<Resource<Event>>(Resource.Idle())
    val event = _event.asStateFlow()

    private val _eventOwner = MutableStateFlow<Resource<User>>(Resource.Idle())
    val eventOwner = _eventOwner.asStateFlow()

    private val _linkedCommunities = MutableStateFlow<Resource<List<Community>>>(Resource.Idle())
    val linkedCommunities = _linkedCommunities.asStateFlow()

    private val _requests = MutableStateFlow<Resource<List<RequestForEvent>>>(Resource.Idle())
    val requests = _requests.asStateFlow()

    private val _participants = MutableStateFlow<Resource<List<User>>>(Resource.Idle())
    val participants = _participants.asStateFlow()

    val countTheParticipants = MutableStateFlow<Int?>(null)
    private var eventJob: Job? = null

    var lastPastEventParticipantDoc: DocumentSnapshot? = null

    val numberOfRequests = MutableStateFlow<Int?>(null)
    val acceptResource = MutableStateFlow<Resource<String>>(Resource.Idle())
    val rejectResource = MutableStateFlow<Resource<String>>(Resource.Idle())
    val removeParticipantResource = MutableStateFlow<Resource<String>>(Resource.Idle())

    val lastRequestDoc = MutableStateFlow<DocumentSnapshot?>(null)
    val lastParticipantDoc = MutableStateFlow<DocumentSnapshot?>(null)

    fun getNumberOfRequests(eventId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val requestsRef = db.collection("events").document(eventId)
                .collection("requests")
            val count = firestoreRepo.countDocumentsWithoutResource(requestsRef)
            numberOfRequests.value = count
        }
    }

    fun cancelTheEvent(eventId: String, uid: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val eventRef = db.collection("events").document(eventId)
            val batch = db.batch()
            batch.delete(eventRef)
            val eventParticipantRef =
                db.collection("events").document(eventId).collection("participants")
                    .document(uid)
            batch.delete(eventParticipantRef)
            batch.commit().await()
        }
    }

    fun removeParticipant(eventId: String, participantUid: String) {
        removeParticipantResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val participantRef =
                db.collection("events").document(eventId).collection("participants")
                    .document(participantUid)
            removeParticipantResource.value = firestoreRepo.deleteDocument(participantRef)
        }

    }

    fun acceptRequest(eventId: String, uid: String, ownerUid: String) {
        acceptResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            acceptResource.value = firestoreRepo.acceptJoiningRequestForEvent(
                uid = uid,
                eventId = eventId,
                ownerUid = ownerUid
            )
        }
    }

    fun rejectRequest(eventId: String, uid: String) {
        rejectResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val requestRef =
                db.collection("events").document(eventId).collection("requests").document(uid)
            rejectResource.value = firestoreRepo.deleteDocument(
                documentRef = requestRef
            )
            if (rejectResource.value is Resource.Success) {
                firestoreRepo.refundTicket(
                    uid = uid,
                    amount = 2
                )
            }

        }
    }

    fun getRequests(eventId: String, pageSize: Long) {
        _requests.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val requestsRef = db.collection("events").document(eventId).collection("requests")
            val result = firestoreRepo.getDocumentsWithPaging(
                query = requestsRef,
                pageSize = pageSize,
                lastDocument = lastRequestDoc.value
            )
            if (result is Resource.Success) {
                if (result.data != null) {
                    lastRequestDoc.value = if (result.data.size() < pageSize) {
                        null
                    } else {
                        result.data.lastOrNull()
                    }

                    _requests.value = Resource.Success(data = result.data.map { requestDoc ->
                        val userInfos = firestoreRepo.getUserData(requestDoc.id)
                        requestDoc.toObject(RequestForEvent::class.java).copy(
                            userName = userInfos.data?.userName,
                            photoUrl = userInfos.data?.profilePictureUrl
                        )
                    })
                } else {
                    _requests.value = Resource.Success(data = emptyList())
                }
            } else {
                _requests.value =
                    Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
            }
        }
    }


    fun getEventOwner(uid: String) {
        _eventOwner.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            _eventOwner.value = firestoreRepo.getUserData(uid)
        }
    }

    fun getCountTheParticipants(eventId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val participantsRef = db.collection("events").document(eventId)
                .collection("participants")
            val count = firestoreRepo.countDocumentsWithoutResource(participantsRef)
            countTheParticipants.value = count
        }
    }

    fun getLinkedCommunities(communityIds: List<String>) {
        if (communityIds.isNotEmpty()) {
            _linkedCommunities.value = Resource.Loading()
            viewModelScope.launch(Dispatchers.Main) {
                val communitiesRef =
                    db.collection("communities").whereIn(FieldPath.documentId(), communityIds)
                val result = firestoreRepo.getCollection(
                    query = communitiesRef
                )
                if (result is Resource.Success) {
                    _linkedCommunities.value =
                        Resource.Success(data = result.data?.map { communityDoc ->
                            communityDoc.toObject(Community::class.java).copy(id = communityDoc.id)
                        } ?: emptyList())
                } else {
                    _linkedCommunities.value =
                        Resource.Error(messageResource = R.string.something_went_wrong)
                }
            }
        } else {
            _linkedCommunities.value =
                Resource.Success(data = emptyList())
        }
    }

    fun listenToEvent(eventId: String) {
        _event.value = Resource.Loading()
        val eventRef = db.collection("events").document(eventId)
        eventJob?.cancel()
        eventJob = viewModelScope.launch(Dispatchers.Main) {
            firestoreRepo.getDocumentWithListener(
                docRef = eventRef
            ).collect { resource ->
                if (resource is Resource.Success) {
                    if (resource.data != null) {
                        if (resource.data.exists()) {
                            _event.value = resource.data.let {
                                val eventObject = it.toObject(Event::class.java)?.copy(id = eventId)
                                if (eventObject != null) {
                                    Resource.Success(
                                        data = eventObject
                                    )
                                } else {
                                    Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                                }
                            }
                        } else {
                            _event.value = Resource.Error(R.string.event_has_been_cancelled)
                        }
                    } else {
                        _event.value = Resource.Error(R.string.something_went_wrong_try_again_later)
                    }
                }
            }
        }
    }

    fun getPastEvents(pageSize: Int, myUid: String) {
        pastEvents.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val participantRefs =
                db.collectionGroup("participants")
                    .whereEqualTo("uid", myUid)
                    .whereNotEqualTo("date", null)
                    .orderBy("date", Query.Direction.DESCENDING)
            val participantDocsResource = firestoreRepo.getDocumentsWithPaging(
                query = participantRefs,
                pageSize = pageSize.toLong(),
                lastDocument = lastPastEventParticipantDoc
            )
            if (participantDocsResource is Resource.Success) {
                if (participantDocsResource.data != null) {

                    val eventIds = participantDocsResource.data.map {
                        it.reference.parent.parent?.id
                    }
                    if (eventIds.isNotEmpty()) {
                        val eventsRef =
                            db.collection("events").whereIn(FieldPath.documentId(), eventIds)
                        val eventsResource = firestoreRepo.getCollection(
                            query = eventsRef
                        )
                        if (eventsResource is Resource.Success) {
                            lastPastEventParticipantDoc =
                                if (participantDocsResource.data.size() < pageSize) {
                                    null
                                } else {
                                    participantDocsResource.data.lastOrNull()
                                }
                            pastEvents.value = Resource.Success(data = eventsResource.data!!.map {
                                it.toObject(Event::class.java).copy(id = it.id)
                            })
                        } else {
                            pastEvents.value =
                                Resource.Error(messageResource = R.string.something_went_wrong)
                        }
                    } else {
                        pastEvents.value = Resource.Success(data = emptyList())
                    }

                }
            } else {
                pastEvents.value =
                    Resource.Error(messageResource = R.string.something_went_wrong)
            }
        }

    }


    fun getParticipants(eventId: String, pageSize: Long, eventOwnerId: String) {
        _participants.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val participantsRef =
                db.collection("events").document(eventId).collection("participants")
                    .whereNotEqualTo(
                        FieldPath.documentId(), eventOwnerId
                    )
            val result = firestoreRepo.getDocumentsWithPaging(
                query = participantsRef,
                pageSize = pageSize,
                lastDocument = lastParticipantDoc.value
            )
            if (result is Resource.Success) {
                if (result.data != null) {
                    lastParticipantDoc.value = if (result.data.size() < pageSize) {
                        null
                    } else {
                        result.data.lastOrNull()
                    }
                    if (!result.data.isEmpty) {
                        _participants.value =
                            Resource.Success(data = result.data.mapNotNull { participantDoc ->
                                if (participantDoc != null && participantDoc.exists()) {
                                    val userData = firestoreRepo.getUserData(participantDoc.id)
                                    User(
                                        uid = participantDoc.id,
                                        userName = userData.data?.userName ?: "",
                                        profilePictureUrl = userData.data?.profilePictureUrl
                                    )
                                } else {
                                    null
                                }
                            })
                    } else {
                        _participants.value = Resource.Success(data = emptyList())
                    }
                } else {
                    _participants.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            } else {
                _participants.value =
                    Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
            }
        }
    }

}