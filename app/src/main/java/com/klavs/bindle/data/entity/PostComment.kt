package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp

data class PostComment(
    val id: String = "",
    val senderUid: String = "",
    val senderUserName: String? = null,
    val senderProfileImageUrl: String? = null,
    val senderRolePriority: Int? = null,
    val commentText: String ="",
    val date: Timestamp = Timestamp.now(),
    val isMyComment: Boolean? = null
)
