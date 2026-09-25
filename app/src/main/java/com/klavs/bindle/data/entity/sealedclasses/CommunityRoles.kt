package com.klavs.bindle.data.entity.sealedclasses

import com.klavs.bindle.R

sealed class CommunityRoles(val rolePriority: Int, val roleNameResource: Int) {
    data object Admin : CommunityRoles(0, R.string.admin)
    data object Moderator : CommunityRoles(1, R.string.moderator)
    data object Member : CommunityRoles(2, R.string.member)
    data object NotMember : CommunityRoles(3, R.string.non_member)
}