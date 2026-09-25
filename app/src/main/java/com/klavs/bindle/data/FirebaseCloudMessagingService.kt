package com.klavs.bindle.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.klavs.bindle.data.datasource.messaging.MessagingDataSource
import com.klavs.bindle.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseCloudMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var messagingDataSource: MessagingDataSource

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (Constants.displayedChatId != message.data["eventId"] || !message.data.containsKey("eventId")) {
            messagingDataSource.onMessageReceived(
                remoteMessage = message
            )
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "New token: $token")
        val uid = auth.currentUser?.uid
        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                messagingDataSource.updateToken(
                    uid = uid,
                    newToken = token
                )
            }
        }
    }
}