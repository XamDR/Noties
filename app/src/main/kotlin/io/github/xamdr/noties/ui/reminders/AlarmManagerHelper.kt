package io.github.xamdr.noties.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants

object AlarmManagerHelper {

	fun setAlarm(context: Context, note: Note) {
		val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
			putExtra(Constants.BUNDLE_NOTIFICATION_ID, (0..999).random())
			putExtra(Constants.BUNDLE_NOTE_NOTIFICATION, note)
		}
		val pendingIntent = PendingIntent.getBroadcast(
			context,
			Constants.PENDING_INTENT_REQUEST_CODE,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val alarmManager = context.getSystemService<AlarmManager>() ?: return
		val triggerAtMillis = note.reminderDate ?: return
		AlarmManagerCompat.setExactAndAllowWhileIdle(
			alarmManager,
			AlarmManager.RTC_WAKEUP,
			triggerAtMillis,
			pendingIntent
		)
	}
}