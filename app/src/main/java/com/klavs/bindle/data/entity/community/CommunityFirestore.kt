package com.klavs.bindle.data.entity.community

import com.google.firebase.Timestamp

data class CommunityFirestore(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val communityPictureUrl: String? = null,
    val participationByRequestOnly: Boolean = true,
    val eventCreationRestriction: Boolean = false,
    val postSharingRestriction: Boolean = true,
    val numOfMembers: Int? = null,
    val numOfEvents: Int? = null,
    val creatorUid:String = "",
    val creationTimestamp: Timestamp? = null,
)
