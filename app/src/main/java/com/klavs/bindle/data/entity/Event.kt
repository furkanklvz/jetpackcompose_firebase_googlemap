package com.klavs.bindle.data.entity

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val id: String = "",
    val title: String= "",
    val type: String= "",
    val description: String= "",
    val owner: String= "",
    val date: LocalDate,
    val time: LocalTime,
    val location: LatLng,
    val address_description: String? = null,
    val participants: List<String>? = null,
    val participant_limit: Int?=null,
    val announcements: List<Announcement>? = null,
){
    fun isExpired(): Boolean = date.isBefore(LocalDate.now())
}


