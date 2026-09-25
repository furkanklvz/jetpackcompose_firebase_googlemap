package com.klavs.bindle.uix.viewmodel.communities

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.klavs.bindle.R
import com.klavs.bindle.data.datastore.AppPref
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.RequestForCommunity
import com.klavs.bindle.data.entity.Member
import com.klavs.bindle.data.entity.Post
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.storage.StorageRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommunityPageViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val storageRepo: StorageRepository,
    private val authRepo: AuthRepository,
    auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val appPref: AppPref
) : ViewModel() {


    private val _userRolePriority = MutableStateFlow<Int?>(null)
    val userRolePriority: StateFlow<Int?> = _userRolePriority.asStateFlow()


    val rejectRequestState = mutableStateOf<Resource<String>>(Resource.Idle())
    val acceptRequestState = mutableStateOf<Resource<String>>(Resource.Idle())
    val removeMemberState = mutableStateOf<Resource<String>>(Resource.Idle())
    val promoteMemberState = mutableStateOf<Resource<String>>(Resource.Idle())
    val demoteMemberState = mutableStateOf<Resource<String>>(Resource.Idle())
    val updateCommunityPictureState = mutableStateOf<Resource<Any?>>(Resource.Idle())
    val updateCommunityFieldState = mutableStateOf<Resource<Any?>>(Resource.Idle())
    val changeAdminState = mutableStateOf<Resource<String>>(Resource.Idle())
    val leaveCommunityState = mutableStateOf<Resource<String>>(Resource.Idle())
    val upcomingEventsState = mutableStateOf<Resource<List<Event>>>(Resource.Idle())

    private val _members = MutableStateFlow<Resource<List<Member>>>(Resource.Idle())
    val members: StateFlow<Resource<List<Member>>> = _members.asStateFlow()


    private val _community = MutableStateFlow<Resource<Community>>(Resource.Idle())
    val community: StateFlow<Resource<Community>> = _community.asStateFlow()

    private val _didISendRequest = MutableStateFlow<Boolean?>(null)
    val didISendRequest: StateFlow<Boolean?> = _didISendRequest.asStateFlow()

    private val _numberOfMembers = MutableStateFlow<Int?>(null)
    val numberOfMembers: StateFlow<Int?> = _numberOfMembers.asStateFlow()

    private val _numberOfRequests = MutableStateFlow<Int?>(null)
    val numberOfRequests: StateFlow<Int?> = _numberOfRequests.asStateFlow()

    private val _joiningRequests =
        MutableStateFlow<Resource<List<RequestForCommunity>>>(Resource.Idle())
    val joiningRequests: StateFlow<Resource<List<RequestForCommunity>>> =
        _joiningRequests.asStateFlow()

    private val _numOfEvents = MutableStateFlow<Int?>(null)
    val numOfEvents: StateFlow<Int?> = _numOfEvents.asStateFlow()

    val createPostResource = mutableStateOf<Resource<Post>>(Resource.Idle())

    private var communityJob: Job? = null

    var lastMember: DocumentSnapshot? = null
    var lastRequest: DocumentSnapshot? = null


    var myStatusJob: Job? = null
    private var listenToDidISendRequestJob: Job? = null

    fun stopListeningToDidISendRequest() {
        listenToDidISendRequestJob?.cancel()
        listenToDidISendRequestJob = null
    }

    fun createPost(post: Post, communityId: String, currentUser: FirebaseUser) {
        createPostResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            if (_userRolePriority.value != null) {
                if (post.imageUrl != null) {
                    val imageRef = "postPictures/${UUID.randomUUID()}"
                    val pictureUrl = storageRepo.uploadImage(
                        imageUri = post.imageUrl.toUri(),
                        path = imageRef,
                        maxSize = 720
                    ).data?.toString()
                    val postRef =
                        db.collection("communities").document(communityId).collection("posts")
                    val addPostToFirestoreResource = firestoreRepo.addDocument(
                        collectionRef = postRef,
                        data = post.copy(imageUrl = pictureUrl)
                    )
                    if (addPostToFirestoreResource is Resource.Success && addPostToFirestoreResource.data != null) {
                        createPostResource.value = Resource.Success(
                            post.copy(
                                id = addPostToFirestoreResource.data,
                                userPhotoUrl = currentUser.photoUrl?.toString(),
                                userName = currentUser.displayName,
                                numOfComments = 0,
                                numOfLikes = 0,
                                liked = false,
                                userRolePriority = _userRolePriority.value!!
                            )
                        )
                    } else {
                        createPostResource.value =
                            Resource.Error(messageResource = R.string.sharing_post_error_message)
                    }

                } else {
                    val postRef =
                        db.collection("communities").document(communityId).collection("posts")
                    val addPostToFirestoreResource = firestoreRepo.addDocument(
                        collectionRef = postRef,
                        data = post
                    )
                    if (addPostToFirestoreResource is Resource.Success && addPostToFirestoreResource.data != null) {
                        createPostResource.value = Resource.Success(
                            post.copy(
                                id = addPostToFirestoreResource.data,
                                userPhotoUrl = currentUser.photoUrl?.toString(),
                                userName = currentUser.displayName,
                                numOfComments = 0,
                                numOfLikes = 0,
                                liked = false,
                                userRolePriority = _userRolePriority.value!!
                            )
                        )
                    } else {
                        createPostResource.value =
                            Resource.Error(messageResource = R.string.sharing_post_error_message)
                    }
                }
            } else {
                Log.d("createPost", "myRolePriority.value: ${_userRolePriority.value}")
                createPostResource.value =
                    Resource.Error(messageResource = R.string.you_are_not_a_member_of_the_community_anymore)
            }

        }
    }

    fun sendJoinRequest(communityId: String, myUid: String, newTickets: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val data = RequestForCommunity(
                uid = myUid,
                requestDate = Timestamp.now(),
            )
            val requestsRef =
                db.collection("communities").document(communityId).collection("joiningRequests")
            val result = firestoreRepo.addDocument(
                documentName = data.uid,
                collectionRef = requestsRef,
                data = data
            )
            if (result is Resource.Success) {
                val userRef = db.collection("users").document(myUid)
                firestoreRepo.updateField(
                    documentRef = userRef,
                    fieldName = "tickets",
                    data = newTickets
                )
            }
        }
    }

    fun joinTheCommunity(communityId: String, myUid: String, newTickets: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val membersRef =
                db.collection("communities").document(communityId).collection("members")
            val memberData = Member(
                uid = myUid,
                rolePriority = CommunityRoles.Member.rolePriority
            )
            val memberResource = firestoreRepo.addDocument(
                documentName = myUid,
                collectionRef = membersRef,
                data = memberData
            )
            if (memberResource is Resource.Success) {
                launch {
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

    fun deleteTheCommunity(communityId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val eventRef = db.collection("communities").document(communityId)
            firestoreRepo.deleteDocument(
                documentRef = eventRef
            )
        }
    }

    private fun listenToDidISendRequest(communityId: String, myUid: String) {
        Log.d("communityPage", "listenToDidISendRequest called")
        Log.d("communityPage", "communityId: $communityId")
        listenToDidISendRequestJob?.cancel()
        listenToDidISendRequestJob = viewModelScope.launch(Dispatchers.Main) {
            val requestsRef =
                db.collection("communities").document(communityId).collection("joiningRequests")
            firestoreRepo.memberCheck(
                collectionRef = requestsRef,
                fieldName = "uid",
                value = myUid
            ).collect { resource ->
                _didISendRequest.value = resource.data
            }
        }
    }

    fun getUpcomingEvents(communityId: String) {
        upcomingEventsState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val eventsRef =
                db.collection("events")
                    .whereGreaterThan("date", Timestamp.now())
                    .whereArrayContains("linkedCommunities", communityId)
                    .orderBy("date", Query.Direction.ASCENDING)
            upcomingEventsState.value = firestoreRepo.getEvents(
                query = eventsRef
            )
        }
    }


    fun listenToMyStatus(communityId: String, myUid: String) {
        myStatusJob = viewModelScope.launch(Dispatchers.Main) {
            val memberRef = db.collection("communities").document(communityId)
                .collection("members").document(myUid)
            firestoreRepo.getDocumentWithListener(
                docRef = memberRef
            ).collect { resource ->
                _userRolePriority.value = if (resource is Resource.Success) {
                    if (resource.data != null) {
                        if (resource.data.exists()) {
                            resource.data.data?.get("rolePriority")
                                ?.let { (it as? Long)?.toInt() }
                        } else {
                            listenToDidISendRequest(
                                communityId = communityId,
                                myUid = myUid
                            )
                            null
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }

    }


    fun updateCommunityField(communityId: String, changedFieldName: String, newValue: Any) {
        updateCommunityFieldState.value = Resource.Loading()
        viewModelScope.launch {
            val communityRef = db.collection("communities").document(communityId)
            updateCommunityFieldState.value = firestoreRepo.updateField(
                documentRef = communityRef,
                fieldName = changedFieldName,
                data = newValue
            )
        }
    }

    fun getMembersWithPaging(communityId: String, pageSize: Int) {
        _members.value = Resource.Loading()
        val query = db.collection("communities").document(communityId).collection("members")
            .orderBy("rolePriority", Query.Direction.ASCENDING)
        viewModelScope.launch(Dispatchers.Main) {
            val resource = firestoreRepo.getDocumentsWithPaging(
                query = query,
                pageSize = pageSize.toLong(),
                lastDocument = lastMember
            )
            if (resource is Resource.Success) {
                lastMember = if (resource.data!!.size() == pageSize) {
                    resource.data.lastOrNull()
                } else {
                    null
                }
                val memberList = resource.data.mapNotNull { memberDoc ->
                    val userDoc = firestoreRepo.getUserData((memberDoc["uid"] as? String) ?: "")
                    if (userDoc is Resource.Success) {
                        memberDoc.toObject(Member::class.java).copy(
                            profileImageUrl = userDoc.data!!.profilePictureUrl,
                            userName = userDoc.data.userName
                        )
                    } else {
                        memberDoc.toObject(Member::class.java)
                    }
                }
                _members.value = Resource.Success(data = memberList)
            } else {
                _members.value = Resource.Error(messageResource = resource.messageResource!!)
            }
        }
    }

    fun getRequestsWithPaging(communityId: String, pageSize: Int) {
        _joiningRequests.value = Resource.Loading()
        val query = db.collection("communities").document(communityId).collection("joiningRequests")
            .orderBy("requestDate", Query.Direction.DESCENDING)
        viewModelScope.launch(Dispatchers.Main) {
            val resource = firestoreRepo.getDocumentsWithPaging(
                query = query,
                pageSize = pageSize.toLong(),
                lastDocument = lastRequest
            )
            if (resource is Resource.Success) {
                lastRequest = if (resource.data!!.size() == pageSize) {
                    resource.data.lastOrNull()
                } else {
                    null
                }
                val requestList = resource.data.mapNotNull { requestDoc ->
                    val userDoc = firestoreRepo.getUserData(requestDoc.id)
                    if (userDoc is Resource.Success) {
                        requestDoc.toObject(RequestForCommunity::class.java).copy(
                            userName = userDoc.data!!.userName,
                            profilePictureUrl = userDoc.data.profilePictureUrl
                        )
                    } else {
                        requestDoc.toObject(RequestForCommunity::class.java)
                    }
                }
                _joiningRequests.value = Resource.Success(data = requestList)
            } else {
                _joiningRequests.value =
                    Resource.Error(messageResource = resource.messageResource!!)
            }
        }
    }

    fun getNumberOfRequests(communityId: String) {
        viewModelScope.launch {
            val requestsRef =
                db.collection("communities").document(communityId).collection("joiningRequests")
            _numberOfRequests.value = firestoreRepo.countDocumentsWithoutResource(
                query = requestsRef
            )
        }
    }

    fun getNumOfMembers(communityId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val membersRef =
                db.collection("communities").document(communityId).collection("members")
            _numberOfMembers.value = firestoreRepo.countDocumentsWithoutResource(
                query = membersRef
            )
            if (_numberOfMembers.value != null) {
                val communityRef = db.collection("communities").document(communityId)
                firestoreRepo.updateField(
                    documentRef = communityRef,
                    fieldName = "numOfMembers",
                    data = _numberOfMembers.value
                )
            }
        }
    }

    fun getNumOfEvents(communityId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val eventsRef =
                db.collection("events").whereArrayContains("linkedCommunities", communityId)

            _numOfEvents.value = firestoreRepo.countDocumentsWithoutResource(eventsRef)
            if (_numOfEvents.value != null) {
                val communityRef = db.collection("communities").document(communityId)
                firestoreRepo.updateField(
                    documentRef = communityRef,
                    fieldName = "numOfEvents",
                    data = _numOfEvents.value
                )
            }
        }
    }


    fun listenToCommunity(communityId: String) {
        _community.value = Resource.Loading()
        communityJob = viewModelScope.launch {
            val communityRef = db.collection("communities").document(communityId)
            firestoreRepo.getDocumentWithListener(
                docRef = communityRef
            ).collect { resource ->
                if (resource is Resource.Success) {
                    if (resource.data != null) {
                        if (resource.data.exists()) {
                            val communityObject = resource.data.toObject(Community::class.java)
                                ?.copy(id = communityId)
                            if (communityObject != null) {
                                _community.value =
                                    Resource.Success(
                                        data = communityObject
                                    )
                            } else {
                                _community.value =
                                    Resource.Error(messageResource = R.string.something_went_wrong)
                            }
                        } else {
                            _community.value =
                                Resource.Error(messageResource = R.string.community_has_been_deleted)
                        }
                    } else {
                        _community.value =
                            Resource.Error(messageResource = R.string.something_went_wrong)
                    }

                } else {
                    _community.value =
                        Resource.Error(messageResource = R.string.something_went_wrong)
                }
            }
        }
    }

    fun rejectRequest(communityId: String, uid: String) {
        rejectRequestState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val docRef =
                db.collection("communities").document(communityId).collection("joiningRequests")
                    .document(uid)
            rejectRequestState.value = firestoreRepo.deleteDocument(documentRef = docRef)
            if (rejectRequestState.value is Resource.Success) {
                firestoreRepo.refundTicket(
                    uid = uid,
                    amount = 2
                )
            }
        }
    }

    fun acceptRequest(communityId: String, uid: String) {
        acceptRequestState.value = Resource.Loading()
        viewModelScope.launch {
            acceptRequestState.value = firestoreRepo.acceptJoiningRequestForCommunity(
                uid = uid,
                communityId = communityId
            )
        }
    }

    fun removeMember(communityId: String, uid: String) {
        removeMemberState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            removeMemberState.value = firestoreRepo.removeMember(
                uid = uid,
                communityId = communityId
            )
        }
    }

    fun promoteMember(communityId: String, uid: String) {
        promoteMemberState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            promoteMemberState.value = firestoreRepo.promoteMember(
                uid = uid,
                communityId = communityId
            )
        }
    }

    fun demoteMember(communityId: String, uid: String) {
        demoteMemberState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            demoteMemberState.value = firestoreRepo.demoteMember(
                uid = uid,
                communityId = communityId
            )
        }
    }

    fun updateCommunityPicture(newPictureUri: Uri?, communityId: String) {
        updateCommunityPictureState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val imagePath = "communityPictures/${communityId}"
            val communityRef = db.collection("communities").document(communityId)
            if (newPictureUri != null) {
                val uploadImageState = storageRepo.uploadImage(
                    imageUri = newPictureUri,
                    path = imagePath,
                    maxSize = 384
                )
                if (uploadImageState is Resource.Success) {
                    updateCommunityPictureState.value = firestoreRepo.updateField(
                        documentRef = communityRef,
                        fieldName = "communityPictureUrl",
                        data = uploadImageState.data?.toString()
                    )
                } else {
                    updateCommunityPictureState.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            } else {
                updateCommunityPictureState.value = firestoreRepo.updateField(
                    documentRef = communityRef,
                    fieldName = "communityPictureUrl",
                    data = null
                )
                if (updateCommunityPictureState.value is Resource.Success) {
                    storageRepo.deleteImage(
                        path = imagePath
                    )
                }
            }
        }

    }

    fun changeAdmin(communityId: String, uid: String, myUid: String) {
        changeAdminState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val currentAdminMemberRef =
                db.collection("communities").document(communityId).collection("members")
                    .document(myUid)
            val newAdminMemberRef =
                db.collection("communities").document(communityId).collection("members")
                    .document(uid)
            val batch = db.batch()
            batch.update(
                currentAdminMemberRef,
                "rolePriority",
                CommunityRoles.Moderator.rolePriority
            )
            batch.update(newAdminMemberRef, "rolePriority", CommunityRoles.Admin.rolePriority)
            try {
                batch.commit().await()
                changeAdminState.value = Resource.Success(data = uid)
            } catch (e: Exception) {
                Log.e("error from datasource", "changeAdmin: $e")
                FirebaseCrashlytics.getInstance().recordException(e)
                changeAdminState.value =
                    Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
            }
        }

    }

    fun leaveTheCommunity(communityId: String, myUid: String) {
        leaveCommunityState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val memberRef =
                db.collection("communities").document(communityId).collection("members")
                    .document(myUid)
            val result = firestoreRepo.deleteDocument(
                documentRef = memberRef
            )
            if (result is Resource.Success) {
                appPref.removePinnedCommunity(
                    userId = myUid,
                    communityId = communityId
                )
            }
            leaveCommunityState.value = result
        }

    }


}