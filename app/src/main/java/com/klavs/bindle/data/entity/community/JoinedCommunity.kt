package com.klavs.bindle.data.entity.community

import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles


data class JoinedCommunity(
    val id: String = "",
    val name: String = "",
    val communityPictureUrl: String? = null,
    val rolePriority: Int = CommunityRoles.Member.rolePriority,
    var pinned: Boolean = false,
    val eventCreationRestriction:Boolean? = null
)

