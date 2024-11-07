package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp

data class User(
    var uid: String? = null,
    val userName: String="",
    val realName: String="",
    val email: String="",
    val gender: String="",
    val birthDate: Long?=null,
    val phoneNumber: String="",
    val profilePictureUrl: String? = null
)
