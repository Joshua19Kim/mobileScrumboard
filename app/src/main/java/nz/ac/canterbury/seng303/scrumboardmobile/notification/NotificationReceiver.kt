package nz.ac.canterbury.seng303.scrumboardmobile.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import nz.ac.canterbury.seng303.scrumboardmobile.R

const val notificationId = 1
const val channelId = "scrumBoardMobile"

@SuppressLint("RestrictedApi")
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("NOTIFICATION_RECEIVER", "Received notification")
        val extras = intent?.getBundleExtra(Intent.EXTRA_TEXT)
        if (extras != null) {
            val notification = Notification.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(extras.getString("notificationTitle"))
                .setContentText(extras.getString("notificationDescription"))
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }

    }
}