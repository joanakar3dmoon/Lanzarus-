package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_FEEDS = "lanzarus_feeds_channel"
    const val CHANNEL_ALERTS = "lanzarus_alerts_channel"

    private const val CHANNEL_FEEDS_NAME = "Feeds de Contenido Lanzarus"
    private const val CHANNEL_FEEDS_DESC = "Notificaciones sobre finanzas, tendencias y mercados curados."

    private const val CHANNEL_ALERTS_NAME = "Alertas de Inversión y Bots"
    private const val CHANNEL_ALERTS_DESC = "Alertas sobre transacciones, rebalanceos y cambios drásticos en el portafolio de trading."

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Feeds Channel
            val feedsChannel = NotificationChannel(
                CHANNEL_FEEDS,
                CHANNEL_FEEDS_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_FEEDS_DESC
                enableLights(true)
                lightColor = android.graphics.Color.CYAN
            }

            // Alerts Channel (High importance for Heads-Up notification)
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                CHANNEL_ALERTS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_ALERTS_DESC
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
            }

            manager.createNotificationChannel(feedsChannel)
            manager.createNotificationChannel(alertsChannel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        try {
            // Intent to open MainActivity when clicked
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                pendingIntentFlags
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder) // Standard robust system icon
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(
                    if (channelId == CHANNEL_ALERTS) NotificationCompat.PRIORITY_HIGH
                    else NotificationCompat.PRIORITY_DEFAULT
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            // Note: Since we handle permission checks in UI, we can safely invoke notify
            // But we wrap in a check to suppress Lint warnings
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
