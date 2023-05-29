package io.github.xamdr.noties.ui.image

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import io.github.xamdr.noties.domain.model.MediaItemMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaHelper {

	private const val PREFIX_IMAGE = "image"
	private const val PREFIX_VIDEO = "video"

	suspend fun getMediaItemMetadata(context: Context, src: Uri): MediaItemMetadata = withContext(Dispatchers.IO) {
		val retriever = MediaMetadataRetriever()
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