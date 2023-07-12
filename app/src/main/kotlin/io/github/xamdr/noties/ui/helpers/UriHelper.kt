package io.github.xamdr.noties.ui.helpers

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UriHelper {

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