package com.klavs.bindle.data.entity.community

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset


data class Community(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val communityPictureUrl: String? = null,
    val participationByRequestOnly: Boolean = true,
    val eventCreationRestriction: Boolean = false,
    val postSharingRestriction: Boolean = true,
    val creatorUid:String = "",
    val creationTimestamp: Timestamp = Timestamp(LocalDateTime.of(1980,1,1,0,0).toInstant(ZoneOffset.UTC)),
    val numOfMembers: Int? = null,
    val numOfEvents: Int? = null,
    val amIMember: Boolean? = null
)
