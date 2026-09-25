package com.klavs.bindle.uix.viewmodel

import android.location.Address
import android.location.Location
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.klavs.bindle.R
import com.klavs.bindle.data.datastore.AppPref
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.entity.community.JoinedCommunity
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.repo.algolia.AlgoliaRepository
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.location.LocationRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val appPref: AppPref,
    private val locationRepo: LocationRepository,
    private val authRepo: AuthRepository,
    private val firestoreRepo: FirestoreRepository,
    private val algoliaRepo: AlgoliaRepository,
    private val db: FirebaseFirestore,
    auth: FirebaseAuth
) : ViewModel() {

    val currentLocationState: MutableState<Resource<Location>> = mutableStateOf(Resource.Idle())
    val addressState: MutableState<Resource<Address>> = mutableStateOf(Resource.Idle())
    val createEventState: MutableState<Resource<Event>> = mutableStateOf(Resource.Idle())


    private val _searchResults = MutableStateFlow<Resource<List<Event>>>(Resource.Idle())
    val searchResults = _searchResults.asStateFlow()


    private val _eventsInRegion = MutableStateFlow<Resource<List<Event>>>(Resource.Idle())
    val eventsInRegion = _eventsInRegion.asStateFlow()

    private val _joinedCommunities =
        MutableStateFlow<Resource<List<JoinedCommunity>>>(Resource.Idle())
    val joinedCommunities = _joinedCommunities.asStateFlow()


    private val _themeState = MutableStateFlow(AppPref.DEFAULT_THEME)
    val themeState = _themeState.asStateFlow()


    init {
        viewModelScope.launch {
            appPref.getSelectedTheme().collect {
                _themeState.value = it
            }
        }
    }

    fun resetSearchResultResource() {
        _eventsInRegion.value = Resource.Idle()
    }

    fun clearSearchResults() {
        _searchResults.value = Resource.Idle()
    }

    fun searchEvent(searchQuery: String) {
        if (searchQuery.isNotBlank()) {
            _searchResults.value = Resource.Loading()
            viewModelScope.launch(Dispatchers.Main) {
                val result = algoliaRepo.searchEvent(
                    searchQuery = searchQuery,
                    resultSize = 7
                )
                if (result is Resource.Success) {
                    Log.d("eventSearch", "searched events size: ${result.data?.size ?: 0}")
                    if (!result.data.isNullOrEmpty()) {
                        _searchResults.value = Resource.Success(data = result.data.map { eventHit ->
                            val eventJson = eventHit.json
                            Event(
                                id = eventJson["objectID"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                title = eventJson["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                description = eventJson["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                addressDescription = eventJson["addressDescription"]?.jsonPrimitive?.contentOrNull,
                                latitude = eventJson["latitude"]?.jsonPrimitive?.doubleOrNull
                                    ?: 0.0,
                                longitude = eventJson["longitude"]?.jsonPrimitive?.doubleOrNull
                                    ?: 0.0,
                                type = eventJson["type"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                                onlyByRequest = eventJson["onlyByRequest"]?.jsonPrimitive?.booleanOrNull
                                    ?: false,
                                privateInfo = eventJson["privateDate"]?.jsonPrimitive?.booleanOrNull
                                    ?: false,
                                linkedCommunities = eventJson["linkedCommunities"]?.jsonArray
                                    ?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList(),
                                participantLimit = eventJson["participantLimit"]?.jsonPrimitive?.intOrNull,
                                date = eventJson["date"]?.jsonPrimitive?.longOrNull?.let {
                                    Timestamp(
                                        Date(it)
                                    )
                                }
                                    ?: Timestamp.now(),
                                ownerUid = eventJson["ownerUid"]?.jsonPrimitive?.contentOrNull.orEmpty()
                            )
                        })

                    } else {
                        _searchResults.value = Resource.Success(data = emptyList())
                    }
                } else {
                    _searchResults.value =
                        Resource.Error(messageResource = R.string.something_went_wrong)
                }
            }
        }
    }


    fun getCurrentLocation() {
        currentLocationState.value = Resource.Loading()
        viewModelScope.launch {
            currentLocationState.value = locationRepo.getCurrentLocation()
        }
    }

    fun getAddressFromLocation(location: LatLng) {
        addressState.value = Resource.Loading()
        viewModelScope.launch {
            addressState.value = locationRepo.getAddressFromLocation(location)
        }
    }


    fun getEventsInRegion(
        selectedCategories: List<String>,
        startDate: Long?,
        endDate: Long?,
        onlyPublicEvents: Boolean,
        bounds: LatLngBounds,
        listSize: Int
    ) {
        _eventsInRegion.value = Resource.Loading()
        Log.e("map", "bounds: $bounds")
        viewModelScope.launch {
            val southwest = bounds.southwest
            val northeast = bounds.northeast
            var query = db.collection("events")
                .whereGreaterThan("date", Timestamp.now())
                .whereEqualTo("privateEvent", false)
                .whereGreaterThan("latitude", southwest.latitude)
                .whereGreaterThan("longitude", southwest.longitude)
                .whereLessThan("latitude", northeast.latitude)
                .whereLessThan("longitude", northeast.longitude)
            if (selectedCategories.isNotEmpty()) {
                query = query.whereIn("type", selectedCategories)
                Log.e("map", "selected categories: ${selectedCategories}")
            }
            if (startDate != null) {
                query = query.whereGreaterThanOrEqualTo("date", Timestamp(Date(startDate)))
            }
            if (endDate != null) {
                query = query.whereLessThanOrEqualTo("date", Timestamp(Date(endDate)))
            }
            if (endDate != null || startDate != null) {
                query = query.whereEqualTo("privateInfo", false)
            }
            if (onlyPublicEvents) {
                query = query.whereEqualTo("onlyByRequest", false)
            }
            val eventsResource = firestoreRepo.getEvents(query = query, listSize = listSize)
            if (eventsResource is Resource.Success && eventsResource.data != null) {
                _eventsInRegion.value = eventsResource
            } else {
                _eventsInRegion.value =
                    Resource.Error(messageResource = R.string.something_went_wrong)
            }
            Log.e("map", "found events: ${_eventsInRegion.value.data?.map { it.title }}")
        }
    }


    fun createEvent(eventModel: Event, myUid: String, newTickets: Long) {
        createEventState.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            createEventState.value = firestoreRepo.createEvent(
                event = eventModel.copy(
                    ownerUid = myUid,
                    notificationsSent = false
                ),
                newTickets = newTickets
            )
        }
    }


    fun getJoinedCommunities(myUid: String) {
        _joinedCommunities.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val memberDocRef =
                db.collectionGroup("members").whereEqualTo("uid", myUid)
            val memberDocsResource = firestoreRepo.getCollection(
                query = memberDocRef
            )
            if (memberDocsResource is Resource.Success) {
                _joinedCommunities.value = Resource.Success(data =
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
                                name = communityResult.data.getString("name") ?: "",
                                rolePriority = memberDocSnapshot.getLong("rolePriority")?.toInt()
                                    ?: CommunityRoles.Member.rolePriority,
                                communityPictureUrl = communityResult.data.getString("communityPictureUrl")
                                    ?: ""
                            )
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                })
            } else {
                _joinedCommunities.value = Resource.Error(
                    messageResource = memberDocsResource.messageResource
                        ?: R.string.something_went_wrong
                )
            }
        }
    }


}