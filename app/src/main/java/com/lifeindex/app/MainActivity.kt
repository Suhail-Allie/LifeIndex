package com.lifeindex.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.lifeindex.app.data.local.AppLanguageManager
import com.lifeindex.app.data.remote.RetrofitClient
import com.lifeindex.app.navigation.LifeIndexNavGraph
import com.lifeindex.app.notifications.NotificationHelper
import com.lifeindex.app.data.sync.TrackerSyncScheduler
import com.lifeindex.app.ui.theme.LifeIndexTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguageManager.wrap(newBase))
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            granted -> Log.d("LifeIndexFCM", "Notification permission granted: $granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("LifeIndex", "Starting LifeIndex application")
        RetrofitClient.initialize(applicationContext)
        TrackerSyncScheduler.schedulePeriodic(applicationContext)
        Log.d("LifeIndex", "Retrofit client initialized")

        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            LifeIndexTheme {
                LifeIndexNavGraph()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}
