package io.github.xamdr.noties.ui.reminders

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.xamdr.noties.R
import io.github.xamdr.noties.data.entity.media.MediaType
import io.github.xamdr.noties.domain.model.Note
import io.github.xamdr.noties.ui.MainActivity
import io.github.xamdr.noties.ui.editor.tasks.SpannableConverter
import timber.log.Timber
import java.io.FileNotFoundException
import java.time.Instant

object NotificationHelper {

	private const val CHANNEL_ID = "NOTIES_CHANNEL"

	fun buildNotification(context: Context, note: Note): Notification {
		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		}
		val pendingIntent = PendingIntent.getActivity(
			context,
			note.id.toInt(),
			intent,
			PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
		)
		val `when` = note.reminderDate ?: Instant.now().toEpochMilli()
		val contentText = if (note.isTaskList) SpannableConverter.convertToSpannable(note.text) else note.text
		return NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_noties_notification)
			.setContentTitle(note.title.ifEmpty { context.getString(R.string.app_name) })
			.setContentText(contentText)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.setTicker(context.getString(R.string.notification_text))
			.setShowWhen(true)
			.setWhen(`when`)
			.setDefaults(NotificationCompat.DEFAULT_ALL)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.apply {
				note.previewItem?.let { mediaItem ->
					when (mediaItem.mediaType) {
						MediaType.Audio -> {}
						MediaType.Image -> {
							val bitmap = getBimap(context, mediaItem.uri)
							setLargeIcon(bitmap)
							setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
						}
						MediaType.Video -> {
							val bitmap = getBimap(context, mediaItem.metadata.thumbnail)
							setLargeIcon(bitmap)
							setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
						}
					}
				}
			}
			.build()
	}

	fun createNotificationChannel(context: Context, notificationManager: NotificationManagerCompat) {
		val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
		val channel = NotificationChannelCompat.Builder(CHANNEL_ID, importance)
			.setName(context.getString(R.string.app_name))
			.setDescription(context.getString(R.string.noties_channel_description))
			.setSound(
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
				AudioAttributes.Builder().build()
			)
			.setVibrationEnabled(true)
			.setLightsEnabled(true)
			.build()
		notificationManager.createNotificationChannel(channel)
	}

	private fun getBimap(context: Context, uri: Uri?): Bitmap? {
		return try {
			if (uri != null) {
				val input = context.contentResolver.openInputStream(uri)
				BitmapFactory.decodeStream(input)
			}
			else null
		}
		catch (e: FileNotFoundException) {
			Timber.d(e)
			null
		}
	}
}