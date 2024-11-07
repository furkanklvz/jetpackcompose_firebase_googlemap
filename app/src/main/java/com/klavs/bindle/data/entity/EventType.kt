package com.klavs.bindle.data.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Nature
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

sealed class EventType(val value: String, val label: String, val icon: @Composable () -> Unit) {
    data object Sport : EventType(
        "sport",
        "Sport",
        { Icon(imageVector = Icons.Outlined.SportsTennis, contentDescription = "sport") }
    )
    data object Cultural: EventType(
        "cultural",
        "Cultural",
        { Icon(imageVector = Icons.Outlined.TheaterComedy, contentDescription = "cultural") }
    )
    data object Musical: EventType(
        "musical",
        "Musical",
        { Icon(imageVector = Icons.Outlined.MusicNote, contentDescription = "musical") }
    )
    data object Meeting: EventType(
        "meeting",
        "Meeting",
        { Icon(imageVector = Icons.Outlined.Groups, contentDescription = "meeting") }
    )
    data object Organization: EventType(
        "organization",
        "Organization",
        { Icon(imageVector = Icons.Outlined.Cake, contentDescription = "Organization") }
    )
    data object Nature: EventType(
        "nature",
        "Nature",
        { Icon(imageVector = Icons.Outlined.Nature, contentDescription = "nature") }
    )
    data object Education: EventType(
        "education",
        "Education",
        { Icon(imageVector = Icons.Outlined.Calculate, contentDescription = "Education") }
    )
}