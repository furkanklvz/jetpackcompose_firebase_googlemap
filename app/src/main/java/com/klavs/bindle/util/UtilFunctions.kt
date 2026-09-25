package com.klavs.bindle.util

import com.google.android.gms.maps.model.LatLng
import com.klavs.bindle.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class UtilFunctions {

    fun getRewardTitle(rewardType: String): Int {
        return when (rewardType) {
            "ticket" -> R.string.ticket
            else -> R.string.ticket
        }
    }

    fun calculateDistance(latlng1: LatLng, latlng2: LatLng): Double {
        val earthRadius = 6371.0 // Dünya yarıçapı (km)

        val dLat = Math.toRadians(latlng2.latitude - latlng1.latitude)
        val dLon = Math.toRadians(latlng2.longitude - latlng1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(latlng1.latitude)) * cos(Math.toRadians(latlng2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Sonuç kilometre cinsinden
    }

}