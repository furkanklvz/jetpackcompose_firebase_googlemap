package com.klavs.bindle.data.entity.sealedclasses

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.klavs.bindle.R

sealed class HidingDataOptions(val value: Boolean, val  titleResID: Int, val  imageVector: ImageVector) {
    data object HideData: HidingDataOptions(value = true, titleResID =  R.string.hide_data_title, imageVector = Icons.Outlined.VisibilityOff)
    data object EveryoneCanSee: HidingDataOptions(value = false, titleResID =  R.string.everyone_can_see_title, imageVector = Icons.Outlined.Public)
}