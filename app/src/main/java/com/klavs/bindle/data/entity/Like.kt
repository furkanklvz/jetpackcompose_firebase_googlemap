package com.klavs.bindle.data.entity

data class Like(
    val uid: String ="",
    val likedUserName: String = "",
    val likedUserPictureUrl: String?=null,
    val postId: String = "",
)
