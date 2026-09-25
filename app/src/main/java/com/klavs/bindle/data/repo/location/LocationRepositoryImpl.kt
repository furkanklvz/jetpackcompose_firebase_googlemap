package com.klavs.bindle.data.repo.location

import android.location.Address
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.klavs.bindle.R
import com.klavs.bindle.data.datasource.location.LocationDataSource
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(private val ds: LocationDataSource) : LocationRepository {
    override suspend fun getCurrentLocation(): Resource<Location> =
        try {
            withTimeout(15000L) { // 15 saniyelik zaman aşımı
                withContext(Dispatchers.IO) { ds.getCurrentLocation() }
            }
        } catch (e: TimeoutCancellationException) {
            Resource.Error(R.string.location_not_detected)
        } catch (e: Exception) {
            Resource.Error(R.string.location_not_detected)
        }

    override suspend fun getAddressFromLocation(location: LatLng): Resource<Address> =
        withContext(Dispatchers.IO){ds.getAddressFromLocation(location)}
}