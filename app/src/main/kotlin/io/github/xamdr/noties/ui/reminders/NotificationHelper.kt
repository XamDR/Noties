package io.github.xamdr.noties.ui.reminders

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.MainActivity
import io.github.xamdr.noties.ui.helpers.Constants
import java.io.FileNotFoundException
import java.time.Instant

object NotificationHelper {

	private const val CHANNEL_ID = "CHANNEL_ID"
	private const val CHANNEL_NAME = "NOTIES_CHANNEL"
	private const val CHANNEL_DESCRIPTION = "NOTIES"

	fun buildNotification(context: Context, note: Note): Notification {
		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}
		val pendingIntent = PendingIntent.getActivity(
			context,
			Constants.PENDING_INTENT_REQUEST_CODE,
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val builder = NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_noties_notification)
			.setContentTitle(note.title.ifEmpty { context.getString(R.string.app_name) })
			.setContentText(note.text)
		note.previewItem?.uri?.let { setBitmap(context, builder, it) }
		val `when` = note.reminderDate ?: Instant.now().toEpochMilli()
		builder.apply {
			setContentIntent(pendingIntent)
			setAutoCancel(true)
			setTicker(context.getString(R.string.notification_text))
			setShowWhen(true)
			setWhen(`when`)
			setDefaults(NotificationCompat.DEFAULT_ALL)
			priority = NotificationCompat.PRIORITY_DEFAULT
		}
		return builder.build()
	}

	fun createNotificationChannel(notificationManager: NotificationManagerCompat) {
		val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
		val channel = NotificationChannelCompat.Builder(CHANNEL_ID, importance)
			.setName(CHANNEL_NAME)
			.setDescription(CHANNEL_DESCRIPTION)
			.setSound(
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
				AudioAttributes.Builder().build()
			)
			.setVibrationEnabled(true)
			.setLightsEnabled(true)
			.build()
		notificationManager.createNotificationChannel(channel)
	}

	private fun setBitmap(context: Context, builder: NotificationCompat.Builder, uri: Uri) {
		try {
			val input = context.contentResolver.openInputStream(uri)
			BitmapFactory.decodeStream(input)?.let { bitmap ->
				builder.apply {
					setLargeIcon(bitmap)
					setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))
				}
			}
			input?.close()
		}
		catch (_: FileNotFoundException) {}
	}
}