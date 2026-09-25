package com.klavs.bindle.uix.viewmodel.event

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.PostReport
import com.klavs.bindle.data.entity.UserReport
import com.klavs.bindle.data.entity.message.Message
import com.klavs.bindle.data.entity.message.MessageFirestore
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
class EventChatViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val firestoreRepo: FirestoreRepository,
) : ViewModel() {
    var oldestMessageDoc: DocumentSnapshot? = null

    private val _event = MutableStateFlow<Resource<Event>>(Resource.Idle())
    val event = _event.asStateFlow()

    val numOfParticipant = mutableStateOf<Int?>(null)

    private val _messages = MutableStateFlow<Resource<List<Message>>>(Resource.Idle())
    val messages = _messages.asStateFlow()

    private val _newMessages = MutableStateFlow<Resource<List<Message>>>(Resource.Idle())
    val newMessages = _newMessages.asStateFlow()

    val messageSent = MutableStateFlow<Resource<Message>>(Resource.Idle())

    var messageListener: Job? = null
    private var eventJob: Job? = null

    fun sendReport(
        reportedUser: String,
        uid: String,
        reportType: Int,
        description: String,
        messageId: String,
        messageContent: String
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            val reportCode: String = when (reportType) {
                0 -> "CHILD_ABUSE_OR_ILLEGAL_CONTENT"
                1 -> "DISRUPTIVE_BEHAVIOR_OR_HARASSMENT"
                2 -> "SOMETHING_ELSE"
                else -> "UNDEFINED"
            }
            val reportRef = db.collection("reports")
            firestoreRepo.addDocument(
                collectionRef = reportRef,
                data = UserReport(
                    uid = uid,
                    messageContent = messageContent,
                    description = description,
                    reportCode = reportCode,
                    timestamp = Timestamp.now(),
                    reportedUserId = reportedUser,
                    messageId = messageId
                )
            )
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
                if (resource is Resource.Success && resource.data != null) {
                    if (resource.data.exists()) {
                        val eventObject =
                            resource.data.toObject(Event::class.java)?.copy(id = eventId)
                        if (eventObject != null) {
                            _event.value = Resource.Success(
                                eventObject
                            )
                        } else {
                            _event.value =
                                Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                        }
                    } else {
                        _event.value =
                            Resource.Error(messageResource = R.string.event_has_been_cancelled)
                    }
                } else {
                    _event.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            }
        }
    }


    fun sendMessage(eventId: String, message: Message) {
        messageSent.value = Resource.Loading(data = message.copy(timestamp = Timestamp.now()))
        viewModelScope.launch(Dispatchers.Main) {
            val messageRef = db.collection("events").document(eventId).collection("messages")
            val messageFirestore = MessageFirestore(
                message = message.message,
                senderUid = message.senderUid,
                timestamp = Timestamp.now()
            )
            val state = firestoreRepo.addDocument(
                collectionRef = messageRef,
                data = messageFirestore
            )
            if (state is Resource.Success) {
                messageSent.value = Resource.Success(message.copy(timestamp = Timestamp.now()))
            } else {
                messageSent.value =
                    Resource.Error(
                        R.string.message_could_not_be_sent,
                        data = message.copy(timestamp = Timestamp.now())
                    )
            }
        }
    }

    fun getMessages(eventId: String, pageSize: Int, myUid: String) {
        _messages.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val messagesRef = db.collection("events").document(eventId).collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
            val messagesResource = firestoreRepo.getDocumentsWithPaging(
                query = messagesRef,
                pageSize = pageSize.toLong(),
                lastDocument = oldestMessageDoc
            )
            if (messagesResource is Resource.Success) {
                oldestMessageDoc = if ((messagesResource.data?.size() ?: 0) < pageSize) {
                    null
                } else {
                    messagesResource.data?.lastOrNull()
                }

                if (messageListener == null) {
                    if (messagesResource.data!!.isEmpty) {
                        listenToNewMessages(eventId, myUid, null)
                        Log.e("events", "mesajlar dinlenmeye başladı, hiç mesaj yok")
                    } else {
                        listenToNewMessages(
                            eventId,
                            myUid,
                            messagesResource.data.first().getTimestamp("timestamp")
                        )
                        Log.e("events", "mesajlar dinlenmeye başladı")
                    }

                }
                _messages.value =
                    Resource.Success(data = messagesResource.data?.mapNotNull { messageDoc ->
                        if (messageDoc != null && messageDoc.exists()) {
                            if (messageDoc.getString("senderUid") != myUid) {
                                val userInfos =
                                    firestoreRepo.getUserData(messageDoc.getString("senderUid")?:"")
                                messageDoc.toObject(Message::class.java).copy(
                                    id = messageDoc.id,
                                    senderPhotoUrl = userInfos.data?.profilePictureUrl,
                                    senderUsername = userInfos.data?.userName
                                )
                            } else {
                                messageDoc.toObject(Message::class.java).copy(
                                    id = messageDoc.id
                                )
                            }
                        } else {
                            null
                        }
                    } ?: emptyList())
            } else {
                _messages.value =
                    Resource.Error(messageResource = R.string.messages_could_not_be_loaded)
            }
        }
    }

    fun changeChatRestriction(eventId: String, restriction: Boolean) {
        viewModelScope.launch(Dispatchers.Main) {
            val eventRef = db.collection("events").document(eventId)
            firestoreRepo.updateField(
                documentRef = eventRef,
                fieldName = "chatRestriction",
                data = restriction
            )
        }
    }

    private fun listenToNewMessages(
        eventId: String,
        currentUserUid: String,
        lastMessageTimestamp: Timestamp?
    ) {
        _newMessages.value = Resource.Loading()
        messageListener?.cancel()
        messageListener = viewModelScope.launch(Dispatchers.Main) {
            val newMessagesRef = lastMessageTimestamp?.let {
                db.collection("events").document(eventId).collection("messages")
                    .whereGreaterThan("timestamp", it)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
            } ?: db.collection("events").document(eventId).collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
            firestoreRepo.getCollectionWithListener(
                query = newMessagesRef
            ).collect { newMessagesResource ->
                if (newMessagesResource is Resource.Success) {
                    if (newMessagesResource.data != null) {
                        _newMessages.value =
                            Resource.Success(data = newMessagesResource.data.mapNotNull { messageDoc ->
                                if (messageDoc != null && messageDoc.exists()) {
                                    if (messageDoc.getString("senderUid") != currentUserUid) {
                                        val userInfos =
                                            firestoreRepo.getUserData(messageDoc.getString("senderUid")?:"")
                                        messageDoc.toObject(Message::class.java).copy(
                                            id = messageDoc.id,
                                            senderPhotoUrl = userInfos.data?.profilePictureUrl,
                                            senderUsername = userInfos.data?.userName
                                        )
                                    } else {
                                        messageDoc.toObject(Message::class.java).copy(
                                            id = messageDoc.id
                                        )
                                    }
                                } else {
                                    null
                                }
                            })
                    } else {
                        _newMessages.value = Resource.Success(data = emptyList())
                    }
                } else {
                    _newMessages.value =
                        Resource.Error(messageResource = R.string.messages_cannot_be_recieved)
                }

            }
        }
    }

    fun getNumOfParticipant(eventId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val participantsRef = db.collection("events").document(eventId)
                .collection("participants")
            val count = firestoreRepo.countDocumentsWithoutResource(participantsRef)
            numOfParticipant.value = count
        }
    }
}