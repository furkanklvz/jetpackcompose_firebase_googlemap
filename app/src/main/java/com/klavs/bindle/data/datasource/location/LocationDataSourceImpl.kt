package com.klavs.bindle.data.datasource.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.klavs.bindle.R
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

class LocationDataSourceImpl @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val context: Context
) :
    LocationDataSource {
    override suspend fun getCurrentLocation(): Resource<Location> {
        Log.e("location", "konum aranmaya başladı")
        return try {
            val accuracy = Priority.PRIORITY_HIGH_ACCURACY
            @SuppressLint("MissingPermission")
            val location = locationClient.getCurrentLocation(accuracy, null).await()
            if (location != null) {
                Log.e("location", "konum algılandı")
                Resource.Success(data = location)
            }else{
                Log.e("location", "konum algılanamadı")
                Resource.Error(messageResource = R.string.location_not_detected)
            }
        } catch (e: Exception) {
            Log.e("error from datasource", e.localizedMessage ?: "unknown error")
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.location_not_detected)
        }
    }

    override suspend fun getAddressFromLocation(location: LatLng): Resource<Address> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val result = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            return if (!result.isNullOrEmpty()) {
                Resource.Success(data = result[0])
            } else {
                Resource.Error(messageResource = R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            Log.e("error from datasource", e.localizedMessage ?: "unknown error")
            FirebaseCrashlytics.getInstance().recordException(e)
            Resource.Error(R.string.something_went_wrong)
        }
    }
}

