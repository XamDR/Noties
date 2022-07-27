package net.azurewebsites.noties.ui.reminders

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.MainActivity

object AlarmManagerHelper {

	private const val REQUEST_CODE = 0

	fun setAlarmManager(context: Context, `when`: Long) {
		val notification = buildNotification(context, `when`)
		val intent = Intent(context, AlarmReceiver::class.java).apply {
			putExtra(AlarmReceiver.NOTIFICATION_ID, 1)
			putExtra(AlarmReceiver.NOTIFICATION, notification)
		}
		val pendingIntent = PendingIntent.getBroadcast(
			context,
			REQUEST_CODE,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val alarmManager = context.getSystemService<AlarmManager>() ?: return
		AlarmManagerCompat.setExactAndAllowWhileIdle(
			alarmManager,
			AlarmManager.RTC_WAKEUP,
			`when`,
			pendingIntent
		)
	}

	private fun buildNotification(context: Context, `when`: Long): Notification {
		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}
		val pendingIntent = PendingIntent.getActivity(
			context,
			REQUEST_CODE,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		return NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_android)
			.setContentTitle(context.getString(R.string.app_name))
			.setContentText(context.getString(R.string.notification_text))
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.setTicker(context.getString(R.string.notification_text))
			.setShowWhen(true)
			.setWhen(`when`)
			.setDefaults(Notification.DEFAULT_ALL)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
	}
}