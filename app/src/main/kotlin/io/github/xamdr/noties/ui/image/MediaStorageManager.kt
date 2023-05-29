package io.github.xamdr.noties.ui.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.xamdr.noties.domain.model.MediaItem
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object MediaStorageManager {

	private const val size = 1024
	private const val DIRECTORY_IMAGES = "images"
	private const val DIRECTORY_VIDEOS = "videos"

	suspend fun saveMediaItem(context: Context, uri: Uri, fileName: String): String = withContext(IO) {
		val isImage = MediaHelper.isImage(context, uri)
		val directory = if (isImage) "${context.filesDir}/$DIRECTORY_IMAGES" else "${context.filesDir}/$DIRECTORY_VIDEOS"
		context.contentResolver.openInputStream(uri).use {
			FileOutputStream(File(directory, fileName)).use { fos ->
				val buffer = ByteArray(size)
				var read = it?.read(buffer) ?: -1
				while (read != -1 ) {
					fos.write(buffer, 0, read)
					read = it?.read(buffer) ?: -1
				}
				fos.flush()
			}
		}
		"$directory/$fileName"
	}

	fun getImageFromInternalStorage(context: Context, uri: Uri, width: Int, height: Int): Bitmap? {
		val originalBitmap = BitmapHelper.getBitmapFromUri(context, uri, width, height)
		return if (originalBitmap != null) {
			BitmapHelper.rotateImageIfRequired(context, uri, originalBitmap)
		}
		else null
	}

	fun deleteItems(context: Context, items: List<MediaItem>) {
		for (item in items) {
			val fileName = item.uri?.let { DocumentFile.fromSingleUri(context, it)?.name }
			fileName?.let {
				val result = deleteItem(context, it, MediaHelper.isImage(context, item.uri))
				Timber.d("Result: %s", result)
			}
		}
	}

	private fun deleteItem(context: Context, itemFileName: String, isImage: Boolean): Boolean {
		val directory = if (isImage) "${context.filesDir}/$DIRECTORY_IMAGES" else "${context.filesDir}/$DIRECTORY_VIDEOS"
		val file = File(directory, itemFileName)
		return file.delete()
	}
}