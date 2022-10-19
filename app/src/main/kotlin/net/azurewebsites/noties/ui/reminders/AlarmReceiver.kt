package net.azurewebsites.noties.ui.reminders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.content.getSystemService
import net.azurewebsites.noties.ui.MainActivity

class AlarmReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent != null) {
			notify(context, intent)
		}
	}

	private fun notify(context: Context, intent: Intent) {
		val notificationManager = context.getSystemService<NotificationManager>() ?: return
		val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
		createNotificationChannel(notificationManager)
		val id = intent.getIntExtra(NOTIFICATION_ID, 0)
		notificationManager.notify(id, notification)
	}

	private fun createNotificationChannel(notificationManager: NotificationManager) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = CHANNEL_NAME
			val descriptionText = CHANNEL_DESCRIPTION
			val importance = NotificationManager.IMPORTANCE_DEFAULT
			val channel = NotificationChannel(MainActivity.CHANNEL_ID, name, importance).apply {
				description = descriptionText
				lockscreenVisibility = Notification.VISIBILITY_PUBLIC
				setSound(
					RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
					AudioAttributes.Builder().build()
				)
				enableVibration(true)
				enableLights(true)
			}
			notificationManager.createNotificationChannel(channel)
		}
	}

	companion object {
		const val NOTIFICATION = "NOTIFICATION"
		const val NOTIFICATION_ID = "NOTIFICATION_ID"
		private const val CHANNEL_NAME = "NOTIES_CHANNEL"
		private const val CHANNEL_DESCRIPTION = "NOTIES"
	}
}