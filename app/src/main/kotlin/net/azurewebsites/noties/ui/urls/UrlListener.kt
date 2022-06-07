package net.azurewebsites.noties.ui.urls

interface UrlListener {
	fun showUrlsDialog(urls: List<String>)
}