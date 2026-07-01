package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.database.LanzarusDatabase
import com.example.data.repository.LanzarusRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LanzarusViewModel
import com.example.ui.viewmodel.LanzarusViewModelFactory
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Notification Channels for Android 8.0+ Oreo
        NotificationHelper.createNotificationChannels(applicationContext)

        val database = LanzarusDatabase.getDatabase(applicationContext)
        val repository = LanzarusRepository(
            userDao = database.userDao(),
            transactionDao = database.transactionDao(),
            contentDao = database.contentDao(),
            investmentDao = database.investmentDao(),
            chatDao = database.chatDao()
        )
        val factory = LanzarusViewModelFactory(application, repository)
        val viewModel: LanzarusViewModel by viewModels { factory }

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
