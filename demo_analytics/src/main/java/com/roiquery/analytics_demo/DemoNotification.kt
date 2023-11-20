package com.roiquery.analytics_demo

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class DemoNotification : BroadcastReceiver()
{
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("textExtra").toString()
        val title = intent.getStringExtra("titleExtra").toString()
        val notification =
            NotificationCompat.Builder(context, "channelID").setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentText(message).setContentTitle(title).build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(12, notification)
    }
}