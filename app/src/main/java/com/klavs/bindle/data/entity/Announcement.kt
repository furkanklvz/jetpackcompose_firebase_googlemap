package com.klavs.bindle.data.entity

import java.util.Date

data class Announcement(
    val id: String,
    val title: String? = null,
    val message: String,
    val sender: String,
    val date: Date,
)
