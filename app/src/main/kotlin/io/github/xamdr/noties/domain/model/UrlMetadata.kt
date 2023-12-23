package io.github.xamdr.noties.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.jsoup.Jsoup

typealias Url = String

@Parcelize
data class UrlMetadata(
	val host: String? = null,
	val title: String? = null,
	val image: String? = null
) : Parcelable

suspend fun Url.getMetadata(): UrlMetadata = withContext(IO) {
	try {
		val uri = Uri.parse(this@getMetadata)
		val document = Jsoup.connect(this@getMetadata).get()
		val titleElement = document.select("meta[property=og:title]").first()
		val imageElement = document.select("meta[property=og:image]").first()
		UrlMetadata(
			host = uri.host,
			title = titleElement?.attr("content"),
			image = imageElement?.attr("content")
		)
	}
	catch (e: Exception) {
		UrlMetadata()
	}
}