package net.azurewebsites.noties.ui.urls

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import net.azurewebsites.noties.ui.helpers.printError
import org.jsoup.Jsoup

object JsoupHelper {

	@Suppress("BlockingMethodInNonBlockingContext")
	suspend fun extractTitle(url: String) = withContext(IO) {
		try {
			val document = Jsoup.connect(url).get()
			document.title()
		}
		catch (e: Exception) {
			printError("ERROR", e.message)
			null
		}
	}
}