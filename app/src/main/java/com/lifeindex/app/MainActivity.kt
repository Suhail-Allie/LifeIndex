package com.lifeindex.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lifeindex.app.data.remote.RetrofitClient
import com.lifeindex.app.navigation.LifeIndexNavGraph
import com.lifeindex.app.ui.theme.LifeIndexTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(
            "LifeIndex",
            "Starting LifeIndex application"
        )

        RetrofitClient.initialize(applicationContext)

        Log.d(
            "LifeIndex",
            "Retrofit client initialized"
        )

        setContent {
            LifeIndexTheme {
                LifeIndexNavGraph()
            }
        }
    }
}