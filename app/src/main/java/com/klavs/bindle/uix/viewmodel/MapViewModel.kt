package com.klavs.bindle.uix.viewmodel

import android.location.Address
import android.location.Location
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.klavs.bindle.data.datastore.AppPref
import com.klavs.bindle.data.repo.location.LocationRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val appPref: AppPref,
    private val locationRepo: LocationRepository
) : ViewModel() {

    val currentLocationState: MutableState<Resource<Location>> = mutableStateOf(Resource.Idle())
    val addressState: MutableState<Resource<Address>> = mutableStateOf(Resource.Idle())


    private val _themeState = MutableStateFlow(AppPref.DEFAULT_THEME)
    val themeState = _themeState.asStateFlow()

    init {
        viewModelScope.launch {
            appPref.getSelectedTheme().collect {
                _themeState.value = it
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


}