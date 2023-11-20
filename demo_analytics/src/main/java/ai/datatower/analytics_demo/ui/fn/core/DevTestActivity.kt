package ai.datatower.analytics_demo.ui.fn.core

import ai.datatower.analytics_demo.DemoNotification
import ai.datatower.analytics_demo.ui.theme.DataTowerSDKCoreTheme
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

class DevTestActivity: ComponentActivity() {
    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, DevTestActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataTowerSDKCoreTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DevTestPage()
                }
            }
        }
    }
}

@Composable
private fun DevTestPage() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.SECOND, 3)

                val id = "channelID"
                val name = "Daily Alerts"
                val des = "Channel Description A Brief"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(id, name, importance)
                channel.description = des
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                manager!!.createNotificationChannel(channel)

                val intent = Intent(context.applicationContext, DemoNotification::class.java)
                intent.putExtra("titleExtra", "Dynamic Title")
                intent.putExtra("textExtra", "Dynamic Text Body")
                intent.action = "ai.datatower.analytics_demo.broadcast.noti_test"
                val pendingIntent = PendingIntent.getBroadcast(
                    context.applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
                alarmManager!!.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Toast.makeText(context.applicationContext, "Scheduled ", Toast.LENGTH_LONG).show()
            }
        }) {
            Text("Schedule notification (3 secs)")
        }
    }
}