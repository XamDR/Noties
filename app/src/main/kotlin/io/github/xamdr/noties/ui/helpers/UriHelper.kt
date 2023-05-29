package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import io.github.xamdr.noties.ui.image.MediaStorageManager
import io.github.xamdr.noties.ui.image.MediaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object UriHelper {

	private const val AUTHORITY = "io.github.xamdr.noties"
	private const val DEFAULT_IMAGE_EXTENSION = "jpeg"
	private const val DEFAULT_VIDEO_EXTENSION = "mp4"
	private const val PATTERN = "yyyyMMdd_HHmmss"

	suspend fun copyUri(context: Context, uri: Uri): Uri {
		val isImage = MediaHelper.isImage(context, uri)
		val mimeType = MediaHelper.getMediaMimeType(context, uri)
		val defaultExtension = if (isImage) DEFAULT_IMAGE_EXTENSION else DEFAULT_VIDEO_EXTENSION
		val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: defaultExtension
		val fileName = buildString {
			append(if (isImage) "IMG_" else "VID_")
			append(DateTimeFormatter.ofPattern(PATTERN).format(LocalDateTime.now()))
			append("_${(0..999).random()}.$extension")
		}
		val fullPath = MediaStorageManager.saveMediaItem(context, uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(context, AUTHORITY, file)
	}

	suspend fun readTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
		val sb = StringBuilder()
		context.contentResolver.openInputStream(uri)?.use {
			it.bufferedReader().useLines { lines ->
				lines.forEach { line -> sb.append(line).append('\n') }
			}
		}
		sb.toString()
	}
}