package com.klavs.bindle.data.entity



data class Community(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val communityPictureUrl: String? = null,
    val requestIsRequireForJoining: Boolean = false,
    val onlyAdminsCanCreateEvent: Boolean = false,
    val onlyAdminsCanCreatePost: Boolean = false,
)
