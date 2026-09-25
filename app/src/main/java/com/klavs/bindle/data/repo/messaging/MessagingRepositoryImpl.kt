package com.klavs.bindle.data.repo.messaging

import com.google.firebase.messaging.RemoteMessage
import com.klavs.bindle.data.datasource.messaging.MessagingDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MessagingRepositoryImpl @Inject constructor(private val ds: MessagingDataSource) :
    MessagingRepository {
    override suspend fun updateToken(uid: String, newToken: String?) = withContext(Dispatchers.IO) {
        ds.updateToken(
            uid = uid,
            newToken = newToken
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) =
        ds.onMessageReceived(
            remoteMessage = remoteMessage
        )
}