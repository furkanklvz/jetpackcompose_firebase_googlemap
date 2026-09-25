package com.klavs.bindle.util.startdestination

import android.content.Intent
import com.klavs.bindle.data.entity.sealedclasses.BottomNavItem

class StartDestinationProviderImpl : StartDestinationProvider {
    override fun determineStartDestination(intent: Intent?): Pair<String, String?> {
        return when (intent?.action) {
            "OPEN_EVENT" -> {
                val eventId = intent.getStringExtra("eventId") ?: ""
                "event_page" to eventId
            }
            "OPEN_CHAT"->{
                val eventId = intent.getStringExtra("eventId") ?: ""
                "event_chat" to "$eventId/${-1}"
            }
            "OPEN_COMMUNITY"->{
                val communityId = intent.getStringExtra("communityId") ?: ""
                "community_page_graph" to communityId
            }
            "OPEN_POST"->{
                val communityId = intent.getStringExtra("communityId") ?: ""
                val postId = intent.getStringExtra("postId") ?: ""
                "post" to "$communityId/${postId}"
            }

            else -> BottomNavItem.Home.route to null

        }
    }
}