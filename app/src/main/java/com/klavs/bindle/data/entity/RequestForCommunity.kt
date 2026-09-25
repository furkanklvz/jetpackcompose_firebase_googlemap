package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp

data class RequestForCommunity(
    val uid: String = "",
    val userName: String = "",
    val requestDate: Timestamp = Timestamp.now(),
    val profilePictureUrl: String? = null,
    var accepted: Boolean = false
)
