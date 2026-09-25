package com.klavs.bindle.data.entity.sealedclasses

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.material.icons.outlined.Public
import androidx.compose.ui.graphics.vector.ImageVector
import com.klavs.bindle.R

sealed class ParticipationOptionsForEvent(val value: Boolean, val  titleResID: Int, val  imageVector: ImageVector) {
    data object OnlyByRequest: ParticipationOptionsForEvent(value = true, titleResID =  R.string.only_by_request, imageVector = Icons.Outlined.LockPerson)
    data object OpenToAll: ParticipationOptionsForEvent(value = false, titleResID =  R.string.open_to_all, imageVector = Icons.Outlined.Public)
}