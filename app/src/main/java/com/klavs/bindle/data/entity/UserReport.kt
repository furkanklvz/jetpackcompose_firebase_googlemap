package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset

data class UserReport(
    val id: String = "",
    val uid: String? = null,
    val messageId: String = "",
    val messageContent: String? = null,
    val timestamp: Timestamp = Timestamp(LocalDateTime.of(1980, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)),
    val reportedUserId: String? = null,
    val description: String? = null,
    val reportCode: String? = null
)
