package com.klavs.bindle.data.entity.message

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val message: String = "",
    val senderUid: String = "",
    val senderUsername: String? = null,
    val senderPhotoUrl: String? = null,
    val timestamp: Any? = null,
    val sent: Boolean? = null // null: not sent, false: sending, true: sent
)
