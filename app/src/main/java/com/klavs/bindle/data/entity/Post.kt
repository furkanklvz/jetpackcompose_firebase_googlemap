package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import java.io.Serializable

data class Post(
    var id: String = "",
    val uid: String = "",
    val userName: String? = null,
    val userPhotoUrl: String? = null,
    val userRolePriority: Int = CommunityRoles.Member.rolePriority,
    val content: String = "",
    val date: Timestamp = Timestamp.now(),
    val imageUrl: String? = null,
    val commentsOn: Boolean = true,
    var liked: Boolean? = null,
    var numOfLikes: Int? = null,
    var numOfComments: Int? = null
): Serializable
