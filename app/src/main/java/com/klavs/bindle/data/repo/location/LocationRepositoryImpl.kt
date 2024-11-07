package com.klavs.bindle.data.repo.location

import android.location.Address
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.klavs.bindle.data.datasource.location.LocationDataSource
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(private val ds: LocationDataSource) : LocationRepository {
    override suspend fun getCurrentLocation(): Resource<Location> =
        withContext(Dispatchers.IO){ds.getCurrentLocation()}

    override suspend fun getAddressFromLocation(location: LatLng): Resource<Address> =
        withContext(Dispatchers.IO){ds.getAddressFromLocation(location)}
}