package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp

data class FeedbackMessage(
    val id: String? = null,
    val email: String? = null,
    val message: String? = null,
    val timestamp: Timestamp? = null,
    val uid: String? = null
)
