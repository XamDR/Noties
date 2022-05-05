package net.azurewebsites.noties.ui.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions
import net.azurewebsites.noties.core.NoteEntity
import java.io.FileNotFoundException
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object Converter {
	@JvmStatic fun displayCurrentDateTime(currentDateTime: ZonedDateTime): String {
		val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).format(
			ZonedDateTime.parse(formatter.format(currentDateTime), formatter)
		)
	}
}

fun printDebug(tag: String, msg: Any?) = Log.d(tag, msg.toString())

fun printError(tag: String, msg: Any?) = Log.e(tag, msg.toString())

fun extractUrls(input: String): List<String> {
	val parser = UrlDetector(input, UrlDetectorOptions.Default)
	val urls = parser.detect()
	val result = mutableListOf<String>()

	for (url in urls) {
		result.add(url.fullUrl)
	}
	return result
}

fun readUriContent(context: Context, uri: Uri?): NoteEntity? {
	try {
		if (uri != null && uri.scheme == "content") {
			printDebug("URI", uri)
			val inputStream = context.contentResolver.openInputStream(uri)
			val file = DocumentFile.fromSingleUri(context, uri)
			inputStream?.bufferedReader()?.use {
				return NoteEntity(
					title = file?.name?.substringBeforeLast('.'),
					text = it.readText(),
					updateDate = ZonedDateTime.now(),
					urls = extractUrls(it.readText())
				)
			}
		}
		return null
	}
	catch (e: FileNotFoundException) {
		printError("EXCEPTION", e.message)
//		context.showToast(R.string.error_open_file)
		return null
	}
}

fun writeUriContent(context: Context, uri: Uri?, note: NoteEntity) {
	try {
		if (uri != null) {
			printDebug("URI", uri)
			val outputStream = context.contentResolver.openOutputStream(uri)
			outputStream?.bufferedWriter()?.use { it.write(note.text) }
		}
	}
	catch (e: IOException) {
		printError("EXCEPTION", e.message)
//		context.showToast(R.string.error_save_file)
	}
}

fun Int.toDp(context: Context): Int = (this / context.resources.configuration.densityDpi)