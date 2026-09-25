package com.klavs.bindle.data.datasource.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.klavs.bindle.R
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

class MessagingDataSourceImpl @Inject constructor(
    private val context: Context, private val messaging: FirebaseMessaging,
    private val db: FirebaseFirestore
) : MessagingDataSource {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        when (remoteMessage.data["type"]) {
            "chat" -> {
                remoteMessage.notification?.let { message ->
                    sendChatNotification(
                        message = message,
                        eventId = remoteMessage.data["eventId"]
                    )
                }
            }

            "event_request" -> {
                remoteMessage.notification?.let { message ->
                    sendEventRequestNotification(
                        message = message,
                        eventId = remoteMessage.data["eventId"]
                    )
                }
            }

            "event_participation" -> {
                remoteMessage.notification?.let { message ->
                    sendEventParticipationNotification(
                        message = message,
                        eventId = remoteMessage.data["eventId"]
                    )
                }
            }
            "community_participation"->{
                remoteMessage.notification?.let { message ->
                    sendCommunityParticipationNotification(
                        message = message,
                        communityId = remoteMessage.data["communityId"]
                    )
                }
            }
            "community_request" -> {
                remoteMessage.notification?.let { message ->
                    sendCommunityRequestNotification(
                        message = message,
                        communityId = remoteMessage.data["communityId"]
                    )
                }
            }
            "post_liked" -> {
                remoteMessage.notification?.let { message ->
                    sendPostLikedNotification(
                        message = message,
                        communityId = remoteMessage.data["communityId"],
                        postId = remoteMessage.data["postId"]
                    )
                }
            }
            "post_comment" -> {
                remoteMessage.notification?.let { message ->
                    sendPostCommentNotification(
                        message = message,
                        communityId = remoteMessage.data["communityId"],
                        postId = remoteMessage.data["postId"]
                    )
                }
            }
            "new_event_in_community" -> {
                remoteMessage.notification?.let { message ->
                    sendNewEventInCommunityNotification(
                        message = message,
                        communityId = remoteMessage.data["communityId"]
                    )
                }
            }

        }


    }

    override suspend fun updateToken(uid: String, newToken: String?) {
        try {
            val token = newToken ?: messaging.token.await()
            val userDoc = db.collection("users").document(uid).get(Source.SERVER).await()
            if (userDoc.exists()) {
                val tokenInFirestore = userDoc.getString("fcmToken")
                if (tokenInFirestore != token) {
                    db.collection("users").document(uid).update("fcmToken", token).await()
                    FirebaseCrashlytics.getInstance().log("fcmToken updated: $token")
                    Log.e("fcm", "fcmToken updated: $token")
                } else {
                    FirebaseCrashlytics.getInstance().log("fcmToken is same: $token")
                    Log.e("fcm", "fcmToken is same: $token")
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", "Failed to update token: ${e.message}")
        }
    }

    override fun sendCommunityRequestNotification(
        message: RemoteMessage.Notification,
        communityId: String?
    ) {

        val channelId = "community_request_notification_channel_id"
        val channelName = context.getString(R.string.community_request_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)

        if (communityId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://community_page/$communityId"))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }


        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun sendChatNotification(
        message: RemoteMessage.Notification,
        eventId: String?
    ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://event_chat/$eventId/${-1}"))
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "event_message_notification_channel_id"
        val channelName = context.getString(R.string.event_message_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        eventId?.let {
            notificationBuilder.setGroup(it)
            notificationBuilder.setGroupSummary(true)
        }

        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun sendEventRequestNotification(
        message: RemoteMessage.Notification,
        eventId: String?
    ) {

        val channelId = "event_request_notification_channel_id"
        val channelName = context.getString(R.string.event_request_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)

        if (eventId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://event_page/$eventId"))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }


        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun sendEventParticipationNotification(
        message: RemoteMessage.Notification,
        eventId: String?
    ) {


        val channelId = "event_request_notification_channel_id"
        val channelName = context.getString(R.string.event_request_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)

        if (eventId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://event_page/$eventId"))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }


        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }
    override fun sendCommunityParticipationNotification(
        message: RemoteMessage.Notification,
        communityId: String?
    ) {


        val channelId = "community_participation_notification_channel_id"
        val channelName = context.getString(R.string.community_participation_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)

        if (communityId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://community_page/$communityId"))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }


        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun sendPostLikedNotification(
        message: RemoteMessage.Notification,
        communityId: String?,
        postId: String?
    ) {


        val channelId = "post_liked_notification_channel_id"
        val channelName = context.getString(R.string.post_liked_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)


        if (communityId != null && postId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://post/$communityId/$postId"))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }


        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun sendPostCommentNotification(
        message: RemoteMessage.Notification,
        communityId: String?,
        postId: String?
    ) {


        val channelId = "post_comment_notification_channel_id"
        val channelName = context.getString(R.string.post_comment_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)


        if (communityId != null && postId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://post/$communityId/$postId"))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }


        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun sendNewEventInCommunityNotification(
        message: RemoteMessage.Notification,
        communityId: String?
    ) {
        val channelId = "new_event_in_community_notification_channel_id"
        val channelName = context.getString(R.string.new_event_in_community_notification_channel_name)


        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)


        if (communityId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bindle://community_page/$communityId"))
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setContentIntent(pendingIntent)
        }


        val manager = context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        manager?.createNotificationChannel(channel)

        manager?.notify(Random.nextInt(), notificationBuilder.build())
    }
}