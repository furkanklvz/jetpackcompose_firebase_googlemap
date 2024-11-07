package com.klavs.bindle.data.entity

data class Member(
    val profileImageUrl: String? = null,
    val rolePriority: Int = CommunityRoles.Member.rolePriority,
    val uid: String = "",
    val userName: String = ""
)
