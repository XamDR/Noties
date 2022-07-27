package net.azurewebsites.noties.ui.reminders

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import net.azurewebsites.noties.R
import net.azurewebsites.noties.ui.MainActivity

object NotificationHelper {

	const val REQUEST_CODE = 0

	fun buildNotification(context: Context, `when`: Long): Notification {
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