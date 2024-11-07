package com.klavs.bindle.data.entity

data class Post(
    var id: String = "",
    val senderUid: String = "",
    val senderUserName: String? = null,
    val senderImageUrl: String? = null,
    val content: String = "",
    val date: Long = 0L,
    val imageUrl: String? = null,
    val commentsOn: Boolean = true,
    var liked: Boolean? = null,
    var numOfLikes: Int? = null,
    var numOfComments: Int? = null
)
