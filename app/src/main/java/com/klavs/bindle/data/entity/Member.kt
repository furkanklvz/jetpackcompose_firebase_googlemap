package com.klavs.bindle.data.entity

import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles

data class Member(
    val profileImageUrl: String? = null,
    val rolePriority: Int = CommunityRoles.Member.rolePriority,
    val uid: String = "",
    val userName: String? = null
)
