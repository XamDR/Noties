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

	fun canScheduleExactAlarms(context: Context): Boolean {
		val alarmManager = context.getSystemService<AlarmManager>()
		return when {
			alarmManager != null -> {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					alarmManager.canScheduleExactAlarms()
				}
				else true
			}
			else -> false
		}
	}

	fun setAlarm(context: Context, note: Note, isExactAlarmEnabled: Boolean) {
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (isExactAlarmEnabled && alarmManager.canScheduleExactAlarms()) {
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
}