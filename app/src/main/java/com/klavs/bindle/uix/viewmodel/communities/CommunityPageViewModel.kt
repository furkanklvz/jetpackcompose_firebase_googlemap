package com.klavs.bindle.uix.viewmodel.communities

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.klavs.bindle.data.entity.Community
import com.klavs.bindle.data.entity.CommunityRoles
import com.klavs.bindle.data.entity.JoinedCommunities
import com.klavs.bindle.data.entity.JoiningRequestForCommunity
import com.klavs.bindle.data.entity.Member
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
import javax.inject.Inject

@HiltViewModel
class CommunityPageViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val storageRepo: StorageRepository,
    private val authRepo: AuthRepository,
    auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userRolePriority = MutableStateFlow(CommunityRoles.Member.rolePriority)
    val userRolePriority: StateFlow<Int> = _userRolePriority.asStateFlow()

    private val _amIMember = MutableStateFlow(true)
    val amIMember: StateFlow<Boolean> = _amIMember.asStateFlow()

    var currentUserJob: Job? = null
    var myStatusJob: Job? = null


    init {
        currentUserJob = viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect { firebaseUser ->
                _currentUser.value = firebaseUser
            }
        }
    }

    fun listenToMyStatus(communityId: String) {
        if (currentUser.value != null) {
            myStatusJob = viewModelScope.launch(Dispatchers.Main) {
                val joinedCommunitiesRef = db.collection("users").document(currentUser.value!!.uid)
                    .collection("joinedCommunities").document(communityId)
                firestoreRepo.getDocumentWithListener(
                    docRef = joinedCommunitiesRef
                ).collect { resource ->
                    _amIMember.value = if (resource is Resource.Success) {
                        if (resource.data != null) {
                            resource.data.exists()
                        } else {
                            false
                        }
                    } else {
                        true
                    }
                    _userRolePriority.value = if (resource is Resource.Success) {
                        if (resource.data != null) {
                            if (resource.data.exists()) {
                                resource.data.data?.get("rolePriority")
                                    ?.let { (it as Long).toInt() }
                                    ?: CommunityRoles.Member.rolePriority
                            } else {
                                CommunityRoles.Member.rolePriority
                            }
                        } else {
                            CommunityRoles.Member.rolePriority
                        }
                    } else {
                        CommunityRoles.Member.rolePriority
                    }
                }
            }
        }
    }

    val deleteRequestState = mutableStateOf<Resource<String>>(Resource.Idle())
    val acceptRequestState = mutableStateOf<Resource<String>>(Resource.Idle())
    val removeMemberState = mutableStateOf<Resource<String>>(Resource.Idle())
    val promoteMemberState = mutableStateOf<Resource<String>>(Resource.Idle())
    val demoteMemberState = mutableStateOf<Resource<String>>(Resource.Idle())
    val updateCommunityPictureState = mutableStateOf<Resource<Any>>(Resource.Idle())


    private val _members = MutableStateFlow<Resource<List<Member>>>(Resource.Idle())
    val members: StateFlow<Resource<List<Member>>> = _members.asStateFlow()

    private val _community = MutableStateFlow<Resource<Community>>(Resource.Idle())
    val community: StateFlow<Resource<Community>> = _community.asStateFlow()

    private val _numberOfMembers = MutableStateFlow<Resource<Int>>(Resource.Idle())
    val numberOfMembers: StateFlow<Resource<Int>> = _numberOfMembers.asStateFlow()

    private val _numberOfRequests = MutableStateFlow<Resource<Int>>(Resource.Idle())
    val numberOfRequests: StateFlow<Resource<Int>> = _numberOfRequests.asStateFlow()

    private val _joiningRequests =
        MutableStateFlow<Resource<List<JoiningRequestForCommunity>>>(Resource.Idle())
    val joiningRequests: StateFlow<Resource<List<JoiningRequestForCommunity>>> =
        _joiningRequests.asStateFlow()

    private val _numOfActiveEvents = MutableStateFlow<Resource<Int>>(Resource.Idle())
    val numOfActiveEvents: StateFlow<Resource<Int>> = _numOfActiveEvents.asStateFlow()

    var communityJob: Job? = null

    var lastMember: DocumentSnapshot? = null
    var lastRequest: DocumentSnapshot? = null


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
                lastMember = resource.data!!.lastDocument
                val memberList = resource.data.querySnapshot.mapNotNull { memberDoc ->
                    val userDoc = firestoreRepo.getUserData(memberDoc["uid"] as String)
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
                _members.value = Resource.Error(message = resource.message!!)
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
                lastRequest = resource.data!!.lastDocument
                val requestList = resource.data.querySnapshot.mapNotNull { requestDoc ->
                    val userDoc = firestoreRepo.getUserData(requestDoc.id)
                    if (userDoc is Resource.Success) {
                        requestDoc.toObject(JoiningRequestForCommunity::class.java).copy(
                            userName = userDoc.data!!.userName,
                            profilePictureUrl = userDoc.data.profilePictureUrl
                        )
                    } else {
                        requestDoc.toObject(JoiningRequestForCommunity::class.java)
                    }
                }
                _joiningRequests.value = Resource.Success(data = requestList)
            } else {
                _joiningRequests.value = Resource.Error(message = resource.message!!)
            }
        }
    }

    fun getNumberOfRequests(communityId: String) {
        viewModelScope.launch {
            _numberOfRequests.value = Resource.Loading()
            val requestsRef =
                db.collection("communities").document(communityId).collection("joiningRequests")
            firestoreRepo.countDocuments(requestsRef).collect { resource ->
                _numberOfRequests.value = resource
            }
        }
    }

    fun getNumOfMembers(communityId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _numberOfMembers.value = Resource.Loading()
            val membersRef =
                db.collection("communities").document(communityId).collection("members")
            firestoreRepo.countDocuments(membersRef).collect { resource ->
                _numberOfMembers.value = resource
            }
        }
    }

    fun getNumOfActiveEvents(communityId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _numOfActiveEvents.value = Resource.Loading()
            val eventsRef =
                db.collection("communities").document(communityId).collection("events")
                    .whereGreaterThan("eventDate", System.currentTimeMillis())
            firestoreRepo.countDocuments(query = eventsRef).collect { resource ->
                _numOfActiveEvents.value = resource
            }
        }
    }


    fun getCommunity(communityId: String) {
        _community.value = Resource.Loading()
        communityJob = viewModelScope.launch {
            getNumOfMembers(communityId)
            getNumOfActiveEvents(communityId)
            val communityRef = db.collection("communities").document(communityId)
            firestoreRepo.getDocumentWithListener(
                docRef = communityRef
            ).collect { resource ->
                if (resource is Resource.Success) {
                    _community.value =
                        Resource.Success(
                            data = resource.data!!.toObject(Community::class.java)!!
                                .copy(id = communityId)
                        )
                } else {
                    _community.value = Resource.Error(message = resource.message!!)
                }
            }
        }
    }

    fun deleteRequest(communityId: String, uid: String) {
        deleteRequestState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val docRef =
                db.collection("communities").document(communityId).collection("joiningRequests")
                    .document(uid)
            val result = firestoreRepo.deleteDocument(documentRef = docRef)
            deleteRequestState.value = result
        }
    }

    fun acceptRequest(community: JoinedCommunities, requestObject: JoiningRequestForCommunity) {
        acceptRequestState.value = Resource.Loading()
        viewModelScope.launch {
            val member = Member(
                uid = requestObject.uid!!,
                rolePriority = 2
            )
            acceptRequestState.value = firestoreRepo.acceptJoiningRequestForCommunity(
                member = member,
                community = community
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
                        data = uploadImageState.data!!.toString()
                    )
                } else {
                    updateCommunityPictureState.value =
                        Resource.Error(message = uploadImageState.message!!)
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


}