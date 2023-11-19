package io.github.xamdr.noties.ui.reminders

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableExtraCompat

class AlarmNotificationReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent != null) {
			when (intent.action) {
				NotificationHelper.ACTION_CANCEL -> cancel(context, intent)
				else -> notify(context, intent)
			}
		}
	}

	private fun notify(context: Context, intent: Intent) {
		val notificationManager = NotificationManagerCompat.from(context)
		NotificationHelper.createNotificationChannel(context, notificationManager)
		val id = intent.getIntExtra(Constants.BUNDLE_NOTIFICATION_ID, 0)
		val note = intent.getParcelableExtraCompat(Constants.BUNDLE_NOTE_NOTIFICATION, Note::class.java)
		val notification = NotificationHelper.buildNotification(context, note)
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
			notificationManager.notify(id, notification)
		}
	}

	private fun cancel(context: Context, intent: Intent) {
		val notificationManager = NotificationManagerCompat.from(context)
		val id = intent.getIntExtra(Constants.BUNDLE_NOTIFICATION_ID, 0)
		notificationManager.cancel(id)
	}
}