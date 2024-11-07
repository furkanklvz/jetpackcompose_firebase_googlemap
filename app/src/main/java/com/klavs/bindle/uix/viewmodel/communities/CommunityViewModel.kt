package com.klavs.bindle.uix.viewmodel.communities

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val db: FirebaseFirestore,
    private val storageRepo: StorageRepository,
    private val authRepo: AuthRepository,
    val auth: FirebaseAuth
) : ViewModel() {

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _amIMember = MutableStateFlow<Resource<Boolean>>(Resource.Loading())
    val amIMember: StateFlow<Resource<Boolean>> = _amIMember.asStateFlow()

    private val _didISendRequest = MutableStateFlow<Resource<Boolean>>(Resource.Loading())
    val didISendRequest: StateFlow<Resource<Boolean>> = _didISendRequest.asStateFlow()

    var communitiesJob: Job? = null
    var memberCheckJob: Job? = null
    var requestCheckJob: Job? = null
    var currentUserJob: Job? = null


    val createCommunityState: MutableState<Resource<String>> = mutableStateOf(Resource.Idle())

    private val _communities =
        MutableStateFlow<Resource<List<JoinedCommunities>>>(Resource.Loading())
    val communities: StateFlow<Resource<List<JoinedCommunities>>> = _communities.asStateFlow()

    val searchedCommunityState: MutableState<Resource<Community?>> = mutableStateOf(Resource.Idle())

    private val _numOfMembersOfSearchedCommunity =
        MutableStateFlow<Resource<Int>>(Resource.Loading())
    val numOfMembersOfSearchedCommunity: StateFlow<Resource<Int>> =
        _numOfMembersOfSearchedCommunity.asStateFlow()

    init {
        currentUserJob = viewModelScope.launch(Dispatchers.Main) {
            authRepo.getCurrentUser().collect { firebaseUser ->
                _currentUser.value = firebaseUser
            }
        }
    }

    fun getCommunities() {
        if (currentUser.value != null) {
            communitiesJob = viewModelScope.launch(Dispatchers.Main) {
                val colRef = db.collection("users").document(currentUser.value!!.uid)
                    .collection("joinedCommunities")
                firestoreRepo.getCollectionWithListener(collectionRef = colRef)
                    .collect { resource ->
                        if (resource is Resource.Success) {
                            val communityList =
                                resource.data!!.documents.mapNotNull { joinedCommunityDoc ->
                                    val communityRef =
                                        db.collection("communities").document(joinedCommunityDoc.id)
                                    val communityDoc = firestoreRepo.getDocument(
                                        docRef = communityRef
                                    )
                                    if (communityDoc is Resource.Success) {
                                        joinedCommunityDoc.toObject(JoinedCommunities::class.java)
                                            ?.copy(
                                                id = joinedCommunityDoc.id,
                                                name = communityDoc.data!!.data?.get("name") as String,
                                                communityPictureUrl = communityDoc.data.data?.get("communityPictureUrl") as String?
                                            )
                                    } else {
                                        joinedCommunityDoc.toObject(JoinedCommunities::class.java)
                                            ?.copy(id = joinedCommunityDoc.id)
                                    }
                                }
                            _communities.value = Resource.Success(data = communityList)
                        } else {
                            _communities.value = Resource.Error(message = "An error occurred")
                        }


                    }
            }
        } else {
            _communities.value = Resource.Error(message = "User not logged in")
        }
    }

    fun sendJoinRequest(communityId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            if (currentUser.value != null) {
                val data = JoiningRequestForCommunity(
                    uid = currentUser.value!!.uid,
                    requestDate = System.currentTimeMillis(),
                )
                val requestsRef =
                    db.collection("communities").document(communityId).collection("joiningRequests")
                firestoreRepo.addDocument(
                    documentName = data.uid,
                    collectionRef = requestsRef,
                    data = data
                )
            }
        }
    }

    fun searchCommunityById(communityId: String) {
        searchedCommunityState.value = Resource.Loading()
        val docRef = db.collection("communities").document(communityId)
        if (currentUser.value != null) {
            viewModelScope.launch(Dispatchers.Main) {
                launch {
                    _numOfMembersOfSearchedCommunity.value = Resource.Loading()
                    val membersRef =
                        db.collection("communities").document(communityId).collection("members")
                    firestoreRepo.countDocuments(query = membersRef).collect {
                        _numOfMembersOfSearchedCommunity.value = it
                    }
                }
                val result = firestoreRepo.getDocument(docRef = docRef)
                if (result.data != null) {
                    val communityObject =
                        result.data.toObject(Community::class.java)?.copy(id = result.data.id)
                    val membersRef =
                        db.collection("communities").document(communityId).collection("members")
                    memberCheckJob = launch {
                        firestoreRepo.memberCheck(
                            collectionRef = membersRef,
                            fieldName = "uid",
                            value = currentUser.value!!.uid
                        ).collect { amIMember ->
                            _amIMember.value = amIMember
                        }
                    }

                    val requestsRef = db.collection("communities").document(communityId)
                        .collection("joiningRequests")
                    requestCheckJob = launch {
                        firestoreRepo.memberCheck(
                            collectionRef = requestsRef,
                            fieldName = "uid",
                            value = currentUser.value!!.uid
                        ).collect { didISendRequest ->
                            _didISendRequest.value = didISendRequest
                        }
                    }

                    searchedCommunityState.value = Resource.Success(data = communityObject)
                } else {
                    searchedCommunityState.value = Resource.Success(data = null)
                }
            }
        }
    }


    fun createCommunity(community: Community) {
        createCommunityState.value = Resource.Loading()
        if (currentUser.value != null) {
            viewModelScope.launch(Dispatchers.Main) {
                val communityRef = db.collection("communities")
                val communityResource =
                    firestoreRepo.addDocument(
                        collectionRef = communityRef,
                        data = community
                    )
                if (communityResource is Resource.Success) {
                    val pictureUrl = community.communityPictureUrl?.let {
                        storageRepo.uploadImage(
                            imageUri = it.toUri(),
                            path = "communityPictures/${communityResource.data!!}",
                            maxSize = 384
                        ).data
                    }.toString()
                    firestoreRepo.updateField(
                        documentRef = communityRef.document(communityResource.data!!),
                        fieldName = "communityPictureUrl",
                        data = pictureUrl
                    )
                    val updatedCommunity = community.copy(
                        id = communityResource.data,
                        communityPictureUrl = pictureUrl
                    )
                    val memberRef = db.collection("communities").document(communityResource.data)
                        .collection("members")

                    val adminMemberInfos = Member(
                        uid = currentUser.value!!.uid,
                        userName = currentUser.value!!.displayName!!,
                        profileImageUrl = currentUser.value!!.photoUrl?.toString(),
                        rolePriority = CommunityRoles.Admin.rolePriority
                    )
                    val memberResource = firestoreRepo.addDocument(
                        documentName = currentUser.value!!.uid,
                        collectionRef = memberRef,
                        data = adminMemberInfos
                    )
                    if (memberResource is Resource.Success) {
                        val joinedCommunityRef =
                            db.collection("users").document(currentUser.value!!.uid)
                                .collection("joinedCommunities")
                        val joinedCommunityData = JoinedCommunities(
                            id = communityResource.data,
                            name = updatedCommunity.name,
                            communityPictureUrl = updatedCommunity.communityPictureUrl.toString(),
                            rolePriority = CommunityRoles.Admin.rolePriority
                        )
                        createCommunityState.value =
                            firestoreRepo.addDocument(
                                documentName = joinedCommunityData.id,
                                collectionRef = joinedCommunityRef,
                                data = joinedCommunityData
                            )
                    } else {
                        createCommunityState.value = memberResource
                    }
                } else {
                    createCommunityState.value = communityResource
                }
            }
        } else {
            createCommunityState.value = Resource.Error("User not logged in")
        }

    }

    fun joinTheCommunity(community: Community) {
        if (currentUser.value != null) {
            viewModelScope.launch(Dispatchers.Main) {
                val membersRef =
                    db.collection("communities").document(community.id!!).collection("members")
                val memberData = Member(
                    uid = currentUser.value!!.uid,
                    rolePriority = CommunityRoles.Member.rolePriority
                )
                val memberResource = firestoreRepo.addDocument(
                    documentName = currentUser.value!!.uid,
                    collectionRef = membersRef,
                    data = memberData
                )
                if (memberResource is Resource.Success) {
                    val joinedCommunitiesRef =
                        db.collection("users").document(currentUser.value!!.uid)
                            .collection("joinedCommunities")
                    val joinedCommunitiesData = JoinedCommunities(
                        id = community.id,
                        rolePriority = CommunityRoles.Member.rolePriority
                    )
                    firestoreRepo.addDocument(
                        documentName = community.id,
                        collectionRef = joinedCommunitiesRef,
                        data = joinedCommunitiesData
                    )
                }
            }
        }
    }
}