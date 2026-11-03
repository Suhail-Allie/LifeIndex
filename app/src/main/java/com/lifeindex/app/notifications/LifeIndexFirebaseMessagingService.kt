package com.lifeindex.app.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LifeIndexFirebaseMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        serviceScope.launch {
            try {
                FcmTokenRegistrar.registerForSavedSession(applicationContext, token)
                Log.d("LifeIndexFCM", "Refreshed FCM token registration")
            } catch (error: Exception) {
                Log.w("LifeIndexFCM", "Could not refresh FCM token registration", error)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "LifeIndex"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new update."

        NotificationHelper.show(applicationContext, title, body)
    }
}
