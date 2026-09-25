package com.klavs.bindle.data.entity.sealedclasses

import com.klavs.bindle.R

sealed class Gender( val value:String, val titleResource:Int) {
    data object PreferNotToSay : Gender(value = "preferNotToSay", titleResource = R.string.prefer_not_to_say)
    data object Male : Gender(value = "male", titleResource = R.string.male)
    data object Female : Gender(value = "female", titleResource = R.string.female)
}