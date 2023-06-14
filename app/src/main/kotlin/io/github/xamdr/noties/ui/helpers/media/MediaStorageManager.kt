package io.github.xamdr.noties.ui.helpers.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import io.github.xamdr.noties.domain.model.MediaItem
import io.github.xamdr.noties.ui.helpers.Constants
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MediaStorageManager {

	private const val size = 1024
	private const val DEFAULT_IMAGE_MIME_TYPE = "image/jpg"
	private const val DEFAULT_VIDEO_MIME_TYPE = "video/mp4"
	private const val DEFAULT_DIRECTORY = "Noties"

	suspend fun saveMediaItem(context: Context, uri: Uri, fileName: String): String = withContext(IO) {
		val isImage = MediaHelper.isImage(context, uri)
		val directory = if (isImage) "${context.filesDir}/${Constants.DIRECTORY_IMAGES}"
			else "${context.filesDir}/${Constants.DIRECTORY_VIDEOS}"
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

	@Suppress("DEPRECATION")
	fun savePicture(context: Context): Uri? {
		val fileName = buildString {
			append("IMG_")
			append(DateTimeFormatter.ofPattern(Constants.MEDIA_ITEM_PATTERN).format(LocalDateTime.now()))
			append("_${(0..999).random()}.jpg")
		}
		val directory = "${Environment.DIRECTORY_PICTURES}/$DEFAULT_DIRECTORY"

		val imageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val contentValues = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
				put(MediaStore.MediaColumns.MIME_TYPE, DEFAULT_IMAGE_MIME_TYPE)
				put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
			}
			context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
		}
		else {
			val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
			val appPicturesDir = File("${picturesDir}/$DEFAULT_DIRECTORY")
			if (!appPicturesDir.exists()) appPicturesDir.mkdir()
			val imageFile = File(appPicturesDir, "/$fileName")
			FileProvider.getUriForFile(context, Constants.AUTHORITY, imageFile)
		}
		return imageUri
	}

	suspend fun downloadPicture(context: Context, originalUri: Uri) {
		withContext(IO) {
			val documentFile = DocumentFile.fromSingleUri(context, originalUri)
			val directory = "${Environment.DIRECTORY_PICTURES}/$DEFAULT_DIRECTORY"
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				val contentValues = ContentValues().apply {
					put(MediaStore.MediaColumns.DISPLAY_NAME, documentFile?.name)
					put(MediaStore.MediaColumns.MIME_TYPE, MediaHelper.getMediaMimeType(context, originalUri))
					put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
				}
				context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
			}
			else {
				val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
				val appPicturesDir = File("${picturesDir}/$DEFAULT_DIRECTORY")
				if (!appPicturesDir.exists()) appPicturesDir.mkdir()
				val imageFile = File(appPicturesDir, "/${documentFile?.name}")
				FileProvider.getUriForFile(context, Constants.AUTHORITY, imageFile)
			}?.also { newUri ->
				val inputStream = context.contentResolver.openInputStream(originalUri)
				val outputStream = context.contentResolver.openOutputStream(newUri)
				inputStream?.use { input ->
					outputStream?.use { output ->
						input.copyTo(output)
					}
				}
			}
		}
	}

	@Suppress("DEPRECATION")
	fun saveVideo(context: Context): Uri? {
		val fileName = buildString {
			append("VID_")
			append(DateTimeFormatter.ofPattern(Constants.MEDIA_ITEM_PATTERN).format(LocalDateTime.now()))
			append("_${(0..999).random()}.mp4")
		}
		val directory = "${Environment.DIRECTORY_MOVIES}/$DEFAULT_DIRECTORY"

		val videoUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			val contentValues = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
				put(MediaStore.MediaColumns.MIME_TYPE, DEFAULT_VIDEO_MIME_TYPE)
				put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
			}
			context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
		}
		else {
			val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
			val appMoviesDir = File("${moviesDir}/$DEFAULT_DIRECTORY")
			if (!appMoviesDir.exists()) appMoviesDir.mkdir()
			val videoFile = File(appMoviesDir, "/$fileName")
			FileProvider.getUriForFile(context, Constants.AUTHORITY, videoFile)
		}
		return videoUri
	}
	
	suspend fun downloadVideo(context: Context, originalUri: Uri) {
		withContext(IO) {
			val documentFile = DocumentFile.fromSingleUri(context, originalUri)
			val directory = "${Environment.DIRECTORY_PICTURES}/$DEFAULT_DIRECTORY"
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				val contentValues = ContentValues().apply {
					put(MediaStore.MediaColumns.DISPLAY_NAME, documentFile?.name)
					put(MediaStore.MediaColumns.MIME_TYPE, MediaHelper.getMediaMimeType(context, originalUri))
					put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
				}
				context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
			}
			else {
				val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
				val appMoviesDir = File("${moviesDir}/$DEFAULT_DIRECTORY")
				if (!appMoviesDir.exists()) appMoviesDir.mkdir()
				val videoFile = File(appMoviesDir, "/${documentFile?.name}")
				FileProvider.getUriForFile(context, Constants.AUTHORITY, videoFile)
			}?.also { newUri ->
				val inputStream = context.contentResolver.openInputStream(originalUri)
				val outputStream = context.contentResolver.openOutputStream(newUri)
				inputStream?.use { input ->
					outputStream?.use { output ->
						input.copyTo(output)
					}
				}
			}
		}
	}

	fun deleteItems(context: Context, items: List<MediaItem>) {
		for (item in items) {
			val fileName = item.uri?.let { DocumentFile.fromSingleUri(context, it)?.name }
			val thumbnailFileName = item.metadata.thumbnail?.let { DocumentFile.fromSingleUri(context, it)?.name }
			fileName?.let {
				val result = deleteItem(context, it, MediaHelper.isImage(context, item.uri))
				Timber.d("Item deleted: %s", result)
			}
			thumbnailFileName?.let {
				val result = deleteItem(context, it, true)
				Timber.d("Thumbnail deleted: %s", result)
			}
		}
	}

	private fun deleteItem(context: Context, itemFileName: String, isImage: Boolean): Boolean {
		val directory = if (isImage) "${context.filesDir}/${Constants.DIRECTORY_IMAGES}"
			else "${context.filesDir}/${Constants.DIRECTORY_VIDEOS}"
		val file = File(directory, itemFileName)
		return file.delete()
	}
}