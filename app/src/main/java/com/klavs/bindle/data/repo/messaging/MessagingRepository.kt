package com.klavs.bindle.data.repo.messaging

import com.google.firebase.messaging.RemoteMessage

interface MessagingRepository {
    suspend fun updateToken(uid: String, newToken: String? = null)
    fun onMessageReceived(remoteMessage: RemoteMessage)
}