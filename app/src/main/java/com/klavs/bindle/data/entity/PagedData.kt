package com.klavs.bindle.data.entity

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

data class PagedData(
    val querySnapshot: QuerySnapshot,
    val lastDocument: DocumentSnapshot? = null
)
