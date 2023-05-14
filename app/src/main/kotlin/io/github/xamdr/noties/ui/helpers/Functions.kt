package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.net.Uri
import android.util.Patterns
import io.github.xamdr.noties.data.entity.note.DatabaseNoteEntity
import timber.log.Timber
import java.io.IOException

fun extractUrls(input: String) = Regex(Patterns.WEB_URL.pattern())
	.findAll(input)
	.map { it.value }
	.toList()

//fun readUriContent(context: Context, uri: Uri?): DatabaseNoteEntity? {
//	try {
//		if (uri != null && uri.scheme == "content") {
//			printDebug("URI", uri)
//			val inputStream = context.contentResolver.openInputStream(uri)
//			val file = DocumentFile.fromSingleUri(context, uri)
//			inputStream?.bufferedReader()?.use {
//				return DatabaseNoteEntity(
//					title = file?.simpleName ?: String.Empty,
//					text = it.readText(),
//					modificationDate = ZonedDateTime.now(),
//					urls = extractUrls(it.readText()),
//					notebookId = 1
//				)
//			}
//		}
//		return null
//	}
//	catch (e: FileNotFoundException) {
//		printError("EXCEPTION", e.message)
//		return null
//	}
//}

fun writeUriContent(context: Context, uri: Uri?, note: DatabaseNoteEntity) {
	try {
		if (uri != null) {
			Timber.d("URI: %s", uri)
			val outputStream = context.contentResolver.openOutputStream(uri)
			outputStream?.bufferedWriter()?.use { it.write(note.text) }
		}
	}
	catch (e: IOException) {
		Timber.e(e)
//		context.showToast(R.string.error_save_file)
	}
}

fun Int.toDp(context: Context): Int = (this / context.resources.configuration.densityDpi)