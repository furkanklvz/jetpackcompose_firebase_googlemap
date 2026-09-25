package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp

data class PostReport(
    val id: String? = null,
    val uid: String? = null,
    val postId: String? = null,
    val timestamp: Timestamp? = null,
    val postOwnerUid: String? = null,
    val description: String = "",
    val reportCode: String? = null
)
