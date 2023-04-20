package io.github.xamdr.noties.ui.reminders

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

object AlarmManagerHelper {

	private const val REQUEST_CODE = 0

	fun setAlarmManager(context: Context, `when`: Long, note: Note) {
		val notification = buildNotification(context, `when`, note)
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

	private fun buildNotification(context: Context, `when`: Long, note: Note): Notification {
		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}
		val pendingIntent = PendingIntent.getActivity(
			context,
			REQUEST_CODE,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_noties_notification)
			.setContentTitle(context.getString(R.string.app_name))
			.setContentText(note.text)

		if (note.images.isNotEmpty()) {
//			note.images[0].uri?.let {
//				val bitmap = BitmapHelper.getBitmapFromUri(context, it, 200, 400)
//				builder.setLargeIcon(bitmap)
//				builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))
//			}
			note.images[0].uri?.let { setBitmap(context, builder, it) }
		}
		builder.apply {
			setContentIntent(pendingIntent)
			setAutoCancel(true)
			setTicker(context.getString(R.string.notification_text))
			setShowWhen(true)
			setWhen(`when`)
			setDefaults(Notification.DEFAULT_ALL)
			priority = NotificationCompat.PRIORITY_DEFAULT
		}
		return builder.build()
	}

	private fun setBitmap(context: Context, builder: NotificationCompat.Builder, uri: Uri) = runBlocking {
		withContext(Dispatchers.IO) {
			try {
				val input = context.contentResolver.openInputStream(uri)
				BitmapFactory.decodeStream(input)
			}
			catch (e: FileNotFoundException) {
				null
			}
		}?.let { bitmap ->
			builder.setLargeIcon(bitmap)
			builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))
		}
	}
}