package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp
import com.klavs.bindle.data.entity.sealedclasses.Gender

data class User(
    var uid: String = "",
    val userName: String="",
    val realName: String?=null,
    val email: String="",
    val gender: String? = Gender.PreferNotToSay.value,
    val birthDate: Timestamp? = null,
    val phoneNumber: String?="",
    val profilePictureUrl: String? = null,
    val password : String? = null,   // only for register
    val tickets: Long = -1,
    val language: String = "en",
    val acceptedTermsAndPrivacyPolicy: Boolean = true,
)
