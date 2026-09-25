package com.klavs.bindle.util

import com.klavs.bindle.data.entity.sealedclasses.EventType

object Constants {
    val EVENT_TYPES = listOf(
        EventType.Nature,
        EventType.Sport,
        EventType.Musical,
        EventType.Meeting,
        EventType.Cultural,
        EventType.Education,
        EventType.Organization,
        EventType.Solidarity,
        EventType.Party
    )

    var displayedChatId: String? = null
}