package io.github.xamdr.noties.ui.reminders

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants
import io.github.xamdr.noties.ui.helpers.getParcelableExtraCompat

class AlarmNotificationReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		if (intent != null) {
			notify(context, intent)
		}
	}

	@SuppressLint("MissingPermission")
	private fun notify(context: Context, intent: Intent) {
		val notificationManager = NotificationManagerCompat.from(context)
		NotificationHelper.createNotificationChannel(notificationManager)
		val id = intent.getIntExtra(Constants.BUNDLE_NOTIFICATION_ID, 0)
		val note = intent.getParcelableExtraCompat(Constants.BUNDLE_NOTE_NOTIFICATION, Note::class.java)
		val notification = NotificationHelper.buildNotification(context, note)
		notificationManager.notify(id, notification)
	}
}