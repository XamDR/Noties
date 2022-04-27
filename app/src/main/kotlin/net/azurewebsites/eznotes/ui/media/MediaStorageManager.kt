package net.azurewebsites.eznotes.ui.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import net.azurewebsites.eznotes.ui.image.BitmapHelper
import java.io.File
import java.io.FileOutputStream

object MediaStorageManager {

	private const val size = 1024

	@Suppress("BlockingMethodInNonBlockingContext")
	suspend fun saveToInternalStorage(context: Context, uri: Uri, fileName: String): String = withContext(IO) {
		val directory = "${context.filesDir}/images"
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

	fun deleteImageFromInternalStorage(context: Context, imageFileName: String): Boolean {
		val directory = "${context.filesDir}/images"
		val file = File(directory, imageFileName)
		return file.delete()
	}
}