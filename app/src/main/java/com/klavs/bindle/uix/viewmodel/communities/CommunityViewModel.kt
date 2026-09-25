package com.klavs.bindle.uix.viewmodel.communities

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.datastore.AppPref
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.community.JoinedCommunity
import com.klavs.bindle.data.entity.Member
import com.klavs.bindle.data.entity.community.CommunityFirestore
import com.klavs.bindle.data.repo.algolia.AlgoliaRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.storage.StorageRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val db: FirebaseFirestore,
    private val storageRepo: StorageRepository,
    private val appPref: AppPref,
    private val algoliaRepo: AlgoliaRepository
) : ViewModel() {


    private val _pinnedCommunities = MutableStateFlow<List<String>>(emptyList())
    val pinnedCommunities: StateFlow<List<String>> = _pinnedCommunities.asStateFlow()

    private val _searchResults = MutableStateFlow<Resource<List<Community>>>(Resource.Idle())
    val searchResults = _searchResults.asStateFlow()

    var communitiesJob: Job? = null
    var pinnedCommunitiesJob: Job? = null

    val createCommunityState: MutableState<Resource<String>> = mutableStateOf(Resource.Idle())
    val leaveCommunityState = mutableStateOf<Resource<String>>(Resource.Idle())

    private val _communities =
        MutableStateFlow<Resource<List<JoinedCommunity>>>(Resource.Idle())
    val communities: StateFlow<Resource<List<JoinedCommunity>>> = _communities.asStateFlow()


    fun listenToPinnedCommunities(myUid: String) {
        pinnedCommunitiesJob?.cancel()
        pinnedCommunitiesJob = viewModelScope.launch(Dispatchers.IO) {
            appPref.getPinnedCommunities(
                userId = myUid
            ).collect {
                _pinnedCommunities.value = it
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = Resource.Idle()
    }

    fun searchCommunity(searchQuery: String) {
        if (searchQuery.isNotBlank()) {
            _searchResults.value = Resource.Loading()
            viewModelScope.launch(Dispatchers.Main) {
                val result = algoliaRepo.searchCommunity(
                    searchQuery = searchQuery,
                    resultSize = 7
                )
                if (result is Resource.Success) {
                    if (!result.data.isNullOrEmpty()) {
                        _searchResults.value =
                            Resource.Success(data = result.data.map { communityHit ->
                                val communityJson = communityHit.json
                                Community(
                                    id = communityJson["objectID"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                    description = communityJson["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                    name = communityJson["name"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                    communityPictureUrl = communityJson["communityPictureUrl"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                    participationByRequestOnly = communityJson["participationByRequestOnly"]?.jsonPrimitive?.boolean
                                        ?: true,
                                )
                            })

                    } else {
                        _searchResults.value = Resource.Success(data = emptyList())
                    }
                } else {
                    _searchResults.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            }
        }
    }

    fun changePin(pinned: Boolean, communityId: String, myUid: String) {
        viewModelScope.launch(Dispatchers.Main) {
            if (pinned) {
                appPref.savePinnedCommunity(
                    uid = myUid,
                    communityId = communityId
                )
            } else {
                appPref.removePinnedCommunity(
                    userId = myUid,
                    communityId = communityId
                )
            }
        }

    }

    fun listenToCommunities(myUid: String) {
        _communities.value = Resource.Loading()

        communitiesJob?.cancel()
        communitiesJob = viewModelScope.launch(Dispatchers.Main) {
            val memberDocsRef =
                db.collectionGroup("members").whereEqualTo("uid", myUid)
            firestoreRepo.getCollectionWithListener(query = memberDocsRef)
                .collect { memberDocsResource ->
                    if (memberDocsResource is Resource.Success) {
                        _communities.value =
                            Resource.Success(data = memberDocsResource.data!!.mapNotNull { memberSnapshot ->
                                if (memberSnapshot != null) {
                                    if (memberSnapshot.exists()) {
                                        val communityRef = memberSnapshot.reference.parent.parent
                                        if (communityRef != null) {
                                            val communityDocResource =
                                                firestoreRepo.getDocument(docRef = communityRef)
                                            if (communityDocResource is Resource.Success) {
                                                if (communityDocResource.data!!.exists()) {
                                                    val joinedCommunityObject = JoinedCommunity(
                                                        id = communityDocResource.data.id,
                                                        name = communityDocResource.data.getString("name")
                                                            ?: "",
                                                        rolePriority = memberSnapshot.getLong("rolePriority")
                                                            ?.toInt()
                                                            ?: CommunityRoles.Member.rolePriority,
                                                        communityPictureUrl = communityDocResource.data.getString(
                                                            "communityPictureUrl"
                                                        ),
                                                        pinned = _pinnedCommunities.value.contains(communityDocResource.data.id)
                                                    )
                                                    joinedCommunityObject
                                                } else {
                                                    null
                                                }
                                            } else {
                                                null
                                            }
                                        } else {
                                            null
                                        }
                                    } else {
                                        null
                                    }
                                } else {
                                    null
                                }
                            })
                    } else {
                        _communities.value =
                            Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                    }
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


    fun createCommunity(community: CommunityFirestore, myUid: String, newTickets: Long) {
        createCommunityState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val pictureUrl = if (community.communityPictureUrl != null) {
                storageRepo.uploadImage(
                    imageUri = community.communityPictureUrl.toUri(),
                    path = "communityPictures/${UUID.randomUUID()}",
                    maxSize = 384
                ).data?.toString()
            } else {
                null
            }
            val communityRef = db.collection("communities")
            val communityResource =
                firestoreRepo.addDocument(
                    collectionRef = communityRef,
                    data = community.copy(communityPictureUrl = pictureUrl)
                )
            if (communityResource is Resource.Success && communityResource.data != null) {
                launch {
                    val userRef = db.collection("users").document(myUid)
                    firestoreRepo.updateField(
                        documentRef = userRef,
                        fieldName = "tickets",
                        data = newTickets
                    )

                }
                val memberRef = db.collection("communities").document(communityResource.data)
                    .collection("members")

                val adminMemberInfos = Member(
                    uid = myUid,
                    rolePriority = CommunityRoles.Admin.rolePriority
                )
                createCommunityState.value = firestoreRepo.addDocument(
                    documentName = myUid,
                    collectionRef = memberRef,
                    data = adminMemberInfos
                )
            } else {
                createCommunityState.value =
                    Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
            }
        }
    }
}