package com.klavs.bindle.data.entity

data class JoiningRequestForCommunity(
    val uid: String? = null,
    val userName: String = "",
    val requestDate: Long = 0L,
    val profilePictureUrl: String? = null,
    var accepted: Boolean = false
)
