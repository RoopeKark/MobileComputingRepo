package com.example.mobilecomputingproject

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings.Global
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class NotificationSender(private val context: Context) {

    companion object {
        const val CHANNEL_ID            = "my_channel"
        const val CHANNEL_NAME          = "My Channel"
        const val CHANNEL_DESCRIPTION   = "Channel description"
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = CHANNEL_DESCRIPTION
        try {
            notificationManager.createNotificationChannel(notificationChannel)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("NotificationSender", "Error creating notification channel: ${e.message}")
        }


    }

    fun sendNotification (
        notificationId: Int = 0,
        title: String,
        message: String,
        intent: PendingIntent? = null,
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .setAutoCancel(true)
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("NotificationSender", "Error sending notification: ${e.message}")
        }
    }

    private var coroutineJob: Job? = null

    @SuppressLint("MissingPermission")
    fun updateNotification(notificationId: Int, title: String, message: String, intent: PendingIntent?) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setContentTitle("IT IS BRIGHT")
            setContentText("wow")
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setOnlyAlertOnce(true)
            setContentIntent(intent)
            setAutoCancel(true)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        var brightness: Float

        coroutineJob?.cancel()

        coroutineJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                brightness = GlobalState.sensor_value.floatValue
                delay(3500)
                if (brightness > 1000f) {
                    builder.setContentText(brightness.toString())
                    builder.setOnlyAlertOnce(true)
                    notificationManager.notify(notificationId, builder.build())
                } else {
                    builder.setContentText("It was bright")
                    builder.setContentIntent(intent)
                    notificationManager.notify(notificationId, builder.build())
                    coroutineJob?.cancel()
                }
            }
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}