package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset


data class RequestForEvent(
    val uid: String = "",
    val timestamp: Timestamp = Timestamp(LocalDateTime.of(1980, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)),
    val userName: String? = null,
    val photoUrl: String? = null,
    val accepted: Boolean? = null
)