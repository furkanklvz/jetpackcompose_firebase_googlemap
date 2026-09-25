package com.klavs.bindle.uix.viewmodel

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.entity.community.Community
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.location.LocationRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val db: FirebaseFirestore,
    private val firestoreRepo: FirestoreRepository,
    private val locationRepo: LocationRepository
) : ViewModel() {

    private val _eventsNearMe = MutableStateFlow<Resource<List<Event>>>(Resource.Idle())
    val eventsNearMe = _eventsNearMe.asStateFlow()

    private val _popularCommunitiesResource = MutableStateFlow<Resource<List<Community>>>(Resource.Idle())
    val popularCommunitiesResource = _popularCommunitiesResource.asStateFlow()

    private val _currentLocation = MutableStateFlow<Resource<Location>>(Resource.Idle())
    val currentLocation = _currentLocation.asStateFlow()

    var lastPopularCommunityDocSnapshot = MutableStateFlow<DocumentSnapshot?>(null)

    val popularCommunityList = MutableStateFlow<MutableList<Community>>(mutableListOf())

    fun getEventsNearMe(latLng: LatLng){
        _eventsNearMe.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            _eventsNearMe.value = firestoreRepo.getEventsNearMe(
                latLng = latLng
            )
        }
    }

    fun getCurrentLocation(){
        _currentLocation.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            _currentLocation.value = locationRepo.getCurrentLocation()
        }
    }

    fun getPopularCommunities(limit: Int){
        _popularCommunitiesResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val result = firestoreRepo.getSuggestedCommunities(
                limit = limit,
                lastDoc = lastPopularCommunityDocSnapshot.value
            )
            if (result is Resource.Success && result.data != null){
                popularCommunityList.value.addAll(result.data.first)
                _popularCommunitiesResource.value = Resource.Success(data = result.data.first)
                lastPopularCommunityDocSnapshot.value = if (result.data.first.size < limit) null else result.data.second
            }else{
                _popularCommunitiesResource.value = Resource.Error(messageResource = result.messageResource?: R.string.something_went_wrong)
            }
            Log.d("home", "getSuggestedCommunities: ${_popularCommunitiesResource.value}")
        }
    }

}