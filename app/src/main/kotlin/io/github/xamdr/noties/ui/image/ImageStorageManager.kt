package io.github.xamdr.noties.ui.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import io.github.xamdr.noties.domain.model.Image
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object ImageStorageManager {

	private const val size = 1024
	private const val DIRECTORY = "images"

	suspend fun saveImage(context: Context, uri: Uri, fileName: String): String = withContext(IO) {
		val directory = "${context.filesDir}/$DIRECTORY"
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

	fun deleteImages(context: Context, images: List<Image>) {
		for (image in images) {
			val fileName = DocumentFile.fromSingleUri(context, image.uri!!)?.name
			fileName?.let {
				val result = deleteImage(context, it)
				Timber.d("Result: %s", result)
			}
		}
	}

	private fun deleteImage(context: Context, imageFileName: String): Boolean {
		val directory = "${context.filesDir}/$DIRECTORY"
		val file = File(directory, imageFileName)
		return file.delete()
	}
}