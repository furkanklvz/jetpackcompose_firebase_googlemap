package com.klavs.bindle.data.datasource.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

class LocationDataSourceImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val context: Context
) :
    LocationDataSource {
    override suspend fun getCurrentLocation(): Resource<Location> {
        return try {
            val accuracy = Priority.PRIORITY_BALANCED_POWER_ACCURACY

            @SuppressLint("MissingPermission")
            val location = locationClient.getCurrentLocation(accuracy, null).await()
            Resource.Success(data = location)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "unknown error")
        }
    }

    override suspend fun getAddressFromLocation(location: LatLng): Resource<Address> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val result = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            return if (result != null && result.isNotEmpty()) {
                Resource.Success(data = result[0])
            } else {
                Resource.Error(message = "location not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "unknown error")
        }
    }
}

