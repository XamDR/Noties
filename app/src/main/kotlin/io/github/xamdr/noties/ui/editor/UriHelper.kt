package io.github.xamdr.noties.ui.editor

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import io.github.xamdr.noties.ui.helpers.getUriExtension
import io.github.xamdr.noties.ui.image.ImageStorageManager
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object UriHelper {

	private const val AUTHORITY = "io.github.xamdr.noties"
	private const val DEFAULT_IMAGE_EXTENSION = "jpeg"
	private const val PATTERN = "yyyyMMdd_HHmmss"

	suspend fun copyUri(context: Context, uri: Uri): Uri {
		val extension = context.getUriExtension(uri) ?: DEFAULT_IMAGE_EXTENSION
		val fileName = buildString {
			append("IMG_")
			append(DateTimeFormatter.ofPattern(PATTERN).format(LocalDateTime.now()))
			append("_${(0..999).random()}.$extension")
		}
		val fullPath = ImageStorageManager.saveImage(context, uri, fileName)
		val file = File(fullPath)
		return FileProvider.getUriForFile(context, AUTHORITY, file)
	}
}