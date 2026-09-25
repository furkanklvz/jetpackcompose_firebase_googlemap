package com.klavs.bindle.data.entity.sealedclasses

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Diversity2
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Nature
import androidx.compose.material.icons.rounded.SportsTennis
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.graphics.vector.ImageVector
import com.klavs.bindle.R

sealed class EventType @OptIn(ExperimentalMaterial3ExpressiveApi::class) constructor(
    val value: String,
    val labelResource: Int,
    val icon: ImageVector
) {
    data object Sport : EventType(
        "sport",
        R.string.sport,
        Icons.Rounded.SportsTennis
    )

    data object Cultural : EventType(
        "cultural",
        R.string.cultural,
        Icons.Rounded.TheaterComedy
    )

    data object Musical : EventType(
        "musical",
        R.string.musical,
        Icons.Rounded.MusicNote
    )

    data object Meeting : EventType(
        "meeting",
        R.string.meeting,
        Icons.Rounded.Diversity2
    )

    data object Organization : EventType(
        "organization",
        R.string.organization,
        Icons.Rounded.Cake
    )

    data object Nature : EventType(
        "nature",
        R.string.nature,
        Icons.Rounded.Nature
    )

    data object Education : EventType(
        "education",
        R.string.education,
        Icons.Rounded.Calculate
    )
    data object Solidarity : EventType(
        "solidarity",
        R.string.solidarity,
        Icons.Rounded.Handshake
    )
    data object Party : EventType(
        "party",
        R.string.party,
        Icons.Rounded.Celebration
    )
}