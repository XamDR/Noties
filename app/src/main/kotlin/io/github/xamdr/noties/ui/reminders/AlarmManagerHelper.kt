package io.github.xamdr.noties.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.helpers.Constants

object AlarmManagerHelper {

	fun setAlarm(context: Context, note: Note) {
		val intent = Intent(context, AlarmNotificationReceiver::class.java).apply {
			putExtra(Constants.BUNDLE_NOTIFICATION_ID, note.id.toInt())
			putExtra(Constants.BUNDLE_NOTE_NOTIFICATION, note)
		}
		val pendingIntent = PendingIntent.getBroadcast(
			context,
			note.id.toInt(),
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val alarmManager = context.getSystemService<AlarmManager>() ?: return
		val triggerAtMillis = note.reminderDate ?: return
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (alarmManager.canScheduleExactAlarms()) {
				AlarmManagerCompat.setExactAndAllowWhileIdle(
					alarmManager,
					AlarmManager.RTC_WAKEUP,
					triggerAtMillis,
					pendingIntent
				)
			}
			else {
				AlarmManagerCompat.setAndAllowWhileIdle(
					alarmManager,
					AlarmManager.RTC_WAKEUP,
					triggerAtMillis,
					pendingIntent
				)
			}
		}
		else {
			AlarmManagerCompat.setExactAndAllowWhileIdle(
				alarmManager,
				AlarmManager.RTC_WAKEUP,
				triggerAtMillis,
				pendingIntent
			)
		}
	}

	fun cancelAlarm(context: Context, noteId: Long) {
		val alarmManager = context.getSystemService<AlarmManager>() ?: return
		val intent = Intent(context, AlarmNotificationReceiver::class.java)
		val pendingIntent = PendingIntent.getBroadcast(
			context,
			noteId.toInt(),
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		alarmManager.cancel(pendingIntent)
	}
}