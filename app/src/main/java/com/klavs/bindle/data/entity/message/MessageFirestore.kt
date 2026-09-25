package com.klavs.bindle.data.entity.message

import com.google.firebase.Timestamp

data class MessageFirestore(
    val id: String? = null,
    val message: String = "",
    val senderUid: String = "",
    val timestamp: Any? = null,
)
