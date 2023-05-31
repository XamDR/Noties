package io.github.xamdr.noties.ui.helpers.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import io.github.xamdr.noties.ui.helpers.Constants
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object BitmapHelper {

	fun getBitmapFromInternalStorage(context: Context, uri: Uri, width: Int, height: Int): Bitmap? {
		val originalBitmap = getBitmapFromUri(context, uri, width, height)
		return if (originalBitmap != null) {
			rotateImageIfRequired(context, uri, originalBitmap)
		}
		else null
	}

	fun getUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
		val fileName = buildString {
			append("IMG_")
			append(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()))
			append("_${(0..999).random()}.jpg")
		}
		val file = File("${context.filesDir}/${Constants.DIRECTORY_IMAGES}", "/$fileName")
		val bytes = ByteArrayOutputStream()
		val result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
		return if (result) {
			val bitmapData = bytes.toByteArray()
			FileOutputStream(file).apply {
				write(bitmapData)
				flush()
				close()
			}
			FileProvider.getUriForFile(context, Constants.AUTHORITY, file)
		} else null
	}

	private fun getBitmapFromUri(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
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
			Timber.e(e)
			return null
		}
	}

	private fun rotateImageIfRequired(context: Context, uri: Uri, originalBitmap: Bitmap): Bitmap {
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