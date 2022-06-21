package net.azurewebsites.noties.ui.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import net.azurewebsites.noties.ui.helpers.printError
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BitmapHelper {

	private const val authority = "net.azurewebsites.noties"

	fun getBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
		val bitmap: Bitmap?
		try {
			var inputStream = context.contentResolver.openInputStream(uri)
			val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
			BitmapFactory.decodeStream(inputStream, null, options)
			inputStream?.close()
			inputStream = context.contentResolver.openInputStream(uri)
			options.apply {
				inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
				inJustDecodeBounds = false
			}
			bitmap = BitmapFactory.decodeStream(inputStream, null, options)
			inputStream?.close()
			return bitmap
		}
		catch (e: FileNotFoundException) {
			printError("EXCEPTION", e.message)
			return null
		}
	}

	fun rotateImageIfRequired(context: Context, uri: Uri, originalBitmap: Bitmap): Bitmap {
		val matrix = Matrix()
		val rotationDegree = getRotationDegree(context, uri)
		return if (rotationDegree != 0f) {
			matrix.postRotate(rotationDegree)
			val rotatedBitmap = Bitmap.createBitmap(
				originalBitmap, 0, 0,
				originalBitmap.width, originalBitmap.height, matrix, true
			)
			originalBitmap.recycle()
			rotatedBitmap
		}
		else originalBitmap
	}

	@Suppress("DEPRECATION")
	fun savePicture(context: Context): Uri? {
		val fileName = buildString {
			append("IMG_")
			append(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()))
			append("_${(0..999).random()}.jpg")
		}
		val directory = "${Environment.DIRECTORY_PICTURES}/Noties"

		val imageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val contentValues = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
				put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
				put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
			}
			context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
		}
		else {
			val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
			val appPicturesDir = File("${picturesDir}/Noties")
			if (!appPicturesDir.exists()) appPicturesDir.mkdir()
			val imageFile = File(appPicturesDir, "/$fileName")
			FileProvider.getUriForFile(context, authority, imageFile)
		}
		return imageUri
	}

	private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
		// Raw height and width of image
		val (height: Int, width: Int) = options.run { outHeight to outWidth }
		var inSampleSize = 1

		if (height > reqHeight || width > reqWidth) {
			val halfHeight: Int = height / 2
			val halfWidth: Int = width / 2

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
				inSampleSize *= 2
			}
		}
		return inSampleSize
	}

	private fun getRotationDegree(context: Context, uri: Uri): Float {
		context.contentResolver.openInputStream(uri)?.use {
			val exifInterface = ExifInterface(it)
			val orientation = exifInterface.getAttributeInt(
				ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_UNDEFINED
			)
			return when (orientation) {
				ExifInterface.ORIENTATION_ROTATE_90 -> 90f
				ExifInterface.ORIENTATION_ROTATE_180 -> 180f
				ExifInterface.ORIENTATION_ROTATE_270 -> 270f
				ExifInterface.ORIENTATION_NORMAL -> 0f
				ExifInterface.ORIENTATION_UNDEFINED -> 0f
				else -> -1f
			}
		}
		return -1f
	}
}