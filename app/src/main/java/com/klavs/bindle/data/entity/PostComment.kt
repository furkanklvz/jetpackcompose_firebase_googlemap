package com.klavs.bindle.data.entity

data class PostComment(
    val id: String? = null ,
    val senderUid: String = "",
    val senderUserName: String = "",
    val senderProfileImageUrl: String? = null,
    val commentText: String ="",
    val date: Long = 0L,
    val isMyComment: Boolean = false
)
