package com.klavs.bindle.data.entity

data class DetailedEvent(
    val event: Event,
    val owner: User,
    val participants: List<User> = emptyList(),
    val participantsCount: Int? = null
)
