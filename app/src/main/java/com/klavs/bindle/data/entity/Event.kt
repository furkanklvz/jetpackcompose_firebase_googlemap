package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val title: String= "",
    val type: String= "",
    val description: String= "",
    val ownerUid: String= "",
    val date: Timestamp = Timestamp.now(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val privateEvent: Boolean = false,
    val addressDescription: String? = null,
    val participantLimit: Int?=null,
    val linkedCommunities: List<String> = emptyList(),
    val privateInfo: Boolean = true,
    val onlyByRequest: Boolean = true,
    val participantsCount: Int? = null,
    val notificationsSent: Boolean = false,
    val amIParticipating: Boolean? = null,
    val chatRestriction: Boolean = false,
)