package com.klavs.bindle.data.entity

import com.google.firebase.Timestamp

data class Purchase(
    val orderID: String? = null,
    val timestamp: Timestamp? = null,
    val productId: String? = null,
    val uid: String? = null,
    val consumed: Boolean? = null,
    val purchaseToken: String? = null
)
