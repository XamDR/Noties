package io.github.xamdr.noties.data.repository

import android.net.Uri
import io.github.xamdr.noties.data.entity.url.DatabaseUrlEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import timber.log.Timber
import javax.inject.Singleton

@Singleton
object JsoupHelper {

	suspend fun getMetadata(src: String): DatabaseUrlEntity = withContext(IO) {
		try {
			val url = Uri.parse(src)
			val document = Jsoup.connect(src).get()
			val titleElement = document.select("meta[property=og:title]").first()
			val imageElement = document.select("meta[property=og:image]").first()
			DatabaseUrlEntity(
				source = src,
				host = url.host,
				title = titleElement?.attr("content"),
				image = imageElement?.attr("content")
			)
		}
		catch (e: Exception) {
			Timber.e(e)
			DatabaseUrlEntity(source = src)
		}
	}
}