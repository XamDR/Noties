package io.github.xamdr.noties.ui.helpers.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import io.github.xamdr.noties.domain.model.MediaItemMetadata
import io.github.xamdr.noties.ui.helpers.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MediaHelper {

	private const val DEFAULT_IMAGE_EXTENSION = "jpeg"
	private const val DEFAULT_VIDEO_EXTENSION = "mp4"
	private const val PREFIX_IMAGE = "image"
	private const val PREFIX_VIDEO = "video"

	suspend fun copyUri(context: Context, uri: Uri): Uri {
		val isImage = isImage(context, uri)
		val mimeType = getMediaMimeType(context, uri)
		val defaultExtension = if (isImage) DEFAULT_IMAGE_EXTENSION else DEFAULT_VIDEO_EXTENSION
		val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: defaultExtension
		val fileName = buildString {
			append(if (isImage) "IMG_" else "VID_")
			append(DateTimeFormatter.ofPattern(Constants.MEDIA_ITEM_PATTERN).format(LocalDateTime.now()))
			append("_${(0..999).random()}.$extension")
		}
		val fullPath = MediaStorageManager.saveMediaItem(context, uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(context, Constants.AUTHORITY, file)
	}

	suspend fun getMediaItemMetadata(context: Context, src: Uri): MediaItemMetadata = withContext(Dispatchers.IO) {
		val retriever = MediaMetadataRetriever()
		try {
			retriever.setDataSource(context, src)
			val thumbnail = retriever.frameAtTime
			val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
			val duration = durationString?.toLong() ?: 0
			if (thumbnail != null) {
				val uri = BitmapHelper.getUriFromBitmap(context, thumbnail)
				MediaItemMetadata(uri, duration)
			}
			else MediaItemMetadata(null, duration)
		}
		catch (e: RuntimeException) {
			Timber.e(e)
			MediaItemMetadata()
		}
	}

	fun formatDuration(duration: Long): String {
		val second = (duration / 1000).toInt()
		val seconds = second % 60
		val minutes = second / 60 % 60
		val hours = second / (60 * 60) % 24
		return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
			else String.format("%02d:%02d", minutes, seconds)
	}

	fun getMediaMimeType(context: Context, uri: Uri): String? = context.contentResolver.getType(uri)

	fun isImage(context: Context, uri: Uri): Boolean {
		val mimeType = getMediaMimeType(context, uri) ?: return false
		return mimeType.startsWith(PREFIX_IMAGE)
	}

	fun isVideo(context: Context, uri: Uri): Boolean {
		val mimeType = getMediaMimeType(context, uri) ?: return false
		return mimeType.startsWith(PREFIX_VIDEO)
	}
}