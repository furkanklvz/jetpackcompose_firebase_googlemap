package com.klavs.bindle.data.datasource.messaging

import com.google.firebase.messaging.RemoteMessage

interface MessagingDataSource {
    fun onMessageReceived(remoteMessage: RemoteMessage)
    suspend fun updateToken(uid: String, newToken: String? = null)
    fun sendChatNotification(
        message: RemoteMessage.Notification,
        eventId: String? = null
    )
    fun sendCommunityRequestNotification(message: RemoteMessage.Notification, communityId: String?)
    fun sendEventRequestNotification(
        message: RemoteMessage.Notification,
        eventId: String? = null
    )
    fun sendEventParticipationNotification(
        message: RemoteMessage.Notification,
        eventId: String? = null
    )
    fun sendCommunityParticipationNotification(
        message: RemoteMessage.Notification,
        communityId: String? = null
    )

    fun sendPostLikedNotification(message: RemoteMessage.Notification, communityId: String?, postId: String?)
    fun sendPostCommentNotification(message: RemoteMessage.Notification, communityId: String?, postId: String?)
    fun sendNewEventInCommunityNotification(message: RemoteMessage.Notification, communityId: String?)
}