package net.azurewebsites.noties.ui.editor

import android.net.Uri

interface AttachImagesListener {
	fun addImages(uris: List<Uri>)
}