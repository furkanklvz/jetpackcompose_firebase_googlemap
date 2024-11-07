package com.klavs.bindle.data.entity

sealed class CommunityRoles(val rolePriority: Int, val roleName: String) {
    data object Admin : CommunityRoles(0, "Admin")
    data object Moderator : CommunityRoles(1, "Moderator")
    data object Member : CommunityRoles(2, "Member")
}