package net.azurewebsites.eznotes.ui.media

import android.net.Uri
import android.view.View
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener

class MediaContentReceiverListener(private val contentReceived: (uri: Uri, source: Int) -> Unit) : OnReceiveContentListener {

	override fun onReceiveContent(view: View, payload: ContentInfoCompat): ContentInfoCompat? {
		val pair = payload.partition { it.uri != null }
		if (pair.first != null) {
			val clip = pair.first.clip
			for (i in 0 until clip.itemCount) {
				val uri = clip.getItemAt(i).uri
				contentReceived.invoke(uri, pair.first.source)
			}
		}
		return pair.second
	}

	companion object {
		val MIME_TYPES = arrayOf("image/*")
	}
}