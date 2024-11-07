package com.klavs.bindle.data.repo.location

import android.location.Address
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Resource<Location>
    suspend fun getAddressFromLocation(location: LatLng): Resource<Address>
}