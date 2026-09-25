package com.klavs.bindle.uix.viewmodel.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.community.JoinedCommunity
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditEventViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    val changesResource = MutableStateFlow<Resource<String>>(Resource.Idle())
    val joinedCommunitiesResource =
        MutableStateFlow<Resource<List<JoinedCommunity>>>(Resource.Idle())
    val linkedCommunitiesResource =
        MutableStateFlow<Resource<List<JoinedCommunity>>>(Resource.Idle())

    fun getJoinedCommunities(myUid: String) {
        joinedCommunitiesResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val memberDocRef = db.collectionGroup("members").whereEqualTo("uid", myUid)
            val memberDocsResource = firestoreRepo.getCollection(
                query = memberDocRef
            )
            if (memberDocsResource is Resource.Success) {
                joinedCommunitiesResource.value = Resource.Success(data =
                memberDocsResource.data!!.mapNotNull { memberDocSnapshot ->
                    val communityRef = memberDocSnapshot.reference.parent.parent
                    if (communityRef != null) {
                        val communityResult = firestoreRepo.getDocument(
                            docRef = communityRef,
                            source = Source.SERVER
                        )
                        if (communityResult is Resource.Success && communityResult.data!!.exists()) {
                            JoinedCommunity(
                                id = communityResult.data.id,
                                name = communityResult.data.getString("name")?:"",
                                rolePriority = memberDocSnapshot.getLong("rolePriority")?.toInt()?: CommunityRoles.Member.rolePriority,
                                communityPictureUrl = communityResult.data.getString("communityPictureUrl")?:""
                            )
                        } else {
                            null
                        }
                    }else {
                        null
                    }
                })
            } else {
                joinedCommunitiesResource.value = Resource.Error(messageResource = memberDocsResource.messageResource?:R.string.something_went_wrong)
            }
        }
    }

    fun getLinkedCommunities(communityIds: List<String>) {
        if (communityIds.isNotEmpty()) {
            linkedCommunitiesResource.value = Resource.Loading()
            viewModelScope.launch(Dispatchers.Main) {
                val communitiesRef =
                    db.collection("communities").whereIn(FieldPath.documentId(), communityIds)
                val result = firestoreRepo.getCollection(
                    query = communitiesRef
                )
                if (result is Resource.Success && result.data != null) {
                    linkedCommunitiesResource.value =
                        Resource.Success(data = result.data.map { communityDoc ->
                            val community = communityDoc.toObject(Community::class.java)
                            JoinedCommunity(
                                id = communityDoc.id,
                                name = community.name,
                                communityPictureUrl = community.communityPictureUrl
                            )
                        })
                } else {
                    linkedCommunitiesResource.value =
                        Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
                }
            }
        } else {
            linkedCommunitiesResource.value = Resource.Success(data = emptyList())
        }

    }

    fun saveChanges(event: Event, newTickets: Long, uid: String) {
        if (event.id.isNotBlank()) {
            changesResource.value = Resource.Loading()
            val eventRef = db.collection("events")
            viewModelScope.launch(Dispatchers.Main) {
                val result = firestoreRepo.addDocument(
                    documentName = event.id,
                    collectionRef = eventRef,
                    data = event
                )
                if (result is Resource.Success) {
                    val userRef = db.collection("users").document(uid)
                    firestoreRepo.updateField(
                        documentRef = userRef,
                        fieldName = "tickets",
                        data = newTickets
                    )
                }
                changesResource.value = result
            }
        }
    }

}